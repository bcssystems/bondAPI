package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.*;
import com.bcsystems.bonds.domain.en.*;
import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.*;
import com.bcsystems.bonds.service.CreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditoServiceImpl implements CreditoService {

    private final CreditoRepository creditoRepository;
    private final MovimientoCreditoRepository movimientoCreditoRepository;
    private final AbonoRepository abonoRepository;
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final com.bcsystems.bonds.service.ConfiguracionService configuracionService;

    @Override
    public List<CreditoResponse> listarCreditosPorCliente(Integer idCliente) {
        return creditoRepository.findByClienteIdClienteOrderByFechaCreacionDesc(idCliente)
                .stream().map(this::toCreditoResponse).toList();
    }

    @Override
    public List<MovimientoCreditoResponse> listarMovimientosPorCliente(Integer idCliente) {
        return movimientoCreditoRepository.findByCreditoClienteIdClienteOrderByFechaDesc(idCliente)
                .stream().map(this::toMovimientoResponse).toList();
    }

    @Override
    @Transactional
    public AbonoResponse registrarAbono(AbonoRequest request) {
        Credito credito = creditoRepository.findById(request.idCredito())
                .orElseThrow(() -> new NotFoundException("Credito no encontrado"));

        if (credito.getEstado() != EstadoCredito.ACTIVO) {
            throw new InvalidEntryException("El credito no esta activo");
        }

        if (request.monto() <= 0) {
            throw new InvalidEntryException("El monto debe ser mayor a cero");
        }

        if (request.monto() > credito.getSaldoPendiente()) {
            throw new InvalidEntryException("El monto excede el saldo pendiente ($" + String.format("%.2f", credito.getSaldoPendiente()) + ")");
        }

        Persona usuario = obtenerPersonaActual();
        TipoAbono tipo = "LIQUIDACION".equals(request.tipo()) ? TipoAbono.LIQUIDACION : TipoAbono.PARCIAL;

        double saldoAnterior = credito.getSaldoPendiente();
        double saldoNuevo = saldoAnterior - request.monto();

        Abono abono = Abono.builder()
                .credito(credito)
                .monto(request.monto())
                .tipo(tipo)
                .fecha(LocalDateTime.now())
                .usuario(usuario)
                .build();
        abono = abonoRepository.save(abono);

        TipoMovimientoCredito tipoMov = tipo == TipoAbono.LIQUIDACION ? TipoMovimientoCredito.LIQUIDACION : TipoMovimientoCredito.ABONO;

        MovimientoCredito mov = MovimientoCredito.builder()
                .credito(credito)
                .tipo(tipoMov)
                .monto(request.monto())
                .saldoAnterior(saldoAnterior)
                .saldoNuevo(saldoNuevo)
                .descripcion(tipo == TipoAbono.LIQUIDACION ? "Liquidacion total" : "Abono parcial")
                .fecha(LocalDateTime.now())
                .usuario(usuario)
                .build();
        movimientoCreditoRepository.save(mov);

        credito.setSaldoPendiente(saldoNuevo);
        if (tipo == TipoAbono.LIQUIDACION || saldoNuevo <= 0) {
            credito.setEstado(EstadoCredito.PAGADO);
        }
        creditoRepository.save(credito);

        // Update cliente saldoActual
        Cliente cliente = credito.getCliente();
        cliente.setSaldoActual(cliente.getSaldoActual() - request.monto());
        clienteRepository.save(cliente);

        return new AbonoResponse(
                abono.getIdAbono(), abono.getCredito().getIdCredito(),
                abono.getMonto(), abono.getTipo().name(),
                abono.getFecha(), usuario.getUsuario());
    }

    @Override
    @Transactional
    public List<AbonoResponse> abonarATodas(AbonoGeneralRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        List<Credito> activos = creditoRepository.findByClienteIdClienteAndEstadoOrderByFechaCreacionDesc(
                request.idCliente(), EstadoCredito.ACTIVO);

        if (activos.isEmpty()) {
            throw new InvalidEntryException("El cliente no tiene creditos activos");
        }

        double totalDeuda = activos.stream().mapToDouble(Credito::getSaldoPendiente).sum();
        if (request.monto() > totalDeuda) {
            throw new InvalidEntryException("El monto ($" + String.format("%.2f", request.monto())
                    + ") excede la deuda total ($" + String.format("%.2f", totalDeuda) + ")");
        }

        Persona usuario = obtenerPersonaActual();
        List<AbonoResponse> resultados = new ArrayList<>();

        for (Credito credito : activos) {
            double proporcion = credito.getSaldoPendiente() / totalDeuda;
            double montoAbono = Math.round(request.monto() * proporcion * 100.0) / 100.0;
            if (montoAbono <= 0) continue;
            if (montoAbono > credito.getSaldoPendiente()) {
                montoAbono = credito.getSaldoPendiente();
            }

            double saldoAnterior = credito.getSaldoPendiente();
            double saldoNuevo = saldoAnterior - montoAbono;

            TipoAbono tipo = saldoNuevo <= 0 ? TipoAbono.LIQUIDACION : TipoAbono.PARCIAL;

            Abono abono = Abono.builder()
                    .credito(credito)
                    .monto(montoAbono)
                    .tipo(tipo)
                    .fecha(LocalDateTime.now())
                    .usuario(usuario)
                    .build();
            abono = abonoRepository.save(abono);

            TipoMovimientoCredito tipoMov = tipo == TipoAbono.LIQUIDACION ? TipoMovimientoCredito.LIQUIDACION : TipoMovimientoCredito.ABONO;

            MovimientoCredito mov = MovimientoCredito.builder()
                    .credito(credito)
                    .tipo(tipoMov)
                    .monto(montoAbono)
                    .saldoAnterior(saldoAnterior)
                    .saldoNuevo(saldoNuevo)
                    .descripcion("Abono general - distribucion proporcional")
                    .fecha(LocalDateTime.now())
                    .usuario(usuario)
                    .build();
            movimientoCreditoRepository.save(mov);

            credito.setSaldoPendiente(saldoNuevo);
            if (tipo == TipoAbono.LIQUIDACION || saldoNuevo <= 0) {
                credito.setEstado(EstadoCredito.PAGADO);
            }
            creditoRepository.save(credito);

            resultados.add(new AbonoResponse(
                    abono.getIdAbono(), abono.getCredito().getIdCredito(),
                    abono.getMonto(), abono.getTipo().name(),
                    abono.getFecha(), usuario.getUsuario()));
        }

        // Update cliente saldoActual
        cliente.setSaldoActual(cliente.getSaldoActual() - request.monto());
        clienteRepository.save(cliente);

        return resultados;
    }

    @Override
    public EstadoCuentaResponse estadoCuenta(Integer idCredito) {
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new NotFoundException("Credito no encontrado"));

        List<AbonoResponse> abonos = abonoRepository.findByCreditoIdCreditoOrderByFechaDesc(idCredito).stream()
                .map(a -> new AbonoResponse(
                        a.getIdAbono(), a.getCredito().getIdCredito(),
                        a.getMonto(), a.getTipo().name(),
                        a.getFecha(), a.getUsuario().getUsuario()))
                .toList();

        List<MovimientoCreditoResponse> movimientos = movimientoCreditoRepository
                .findByCreditoIdCreditoOrderByFechaDesc(idCredito).stream()
                .map(this::toMovimientoResponse).toList();

        Cliente c = credito.getCliente();
        ClienteResponse clienteResponse = new ClienteResponse(
                c.getIdCliente(), c.getNombre(), c.getApellidoPaterno(), c.getApellidoMaterno(),
                c.getTelefono(), c.getCodigoPais(), c.getWhatsapp(), c.getEmpresa(),
                c.getRegimenFiscal(), c.getCp(), c.getDireccion(),
                c.getCalle(), c.getNumExt(), c.getNumInt(), c.getColonia(),
                c.getMunicipio(), c.getEstado(),
                c.getRfc(), c.getRepresentanteLegal(), c.getDireccionEntrega(),
                c.getActivo(), c.getFechaRegistro(),
                c.getTieneCredito(), c.getLimiteCredito(), c.getSaldoActual(),
                c.getEnListaNegra(), c.getFechaListaNegra(), c.getMotivoListaNegra());

        String titular = configuracionService.getValor("titularPagare", "PRISCILA ARONG KIM LOPEZ");
        double tasaMora = configuracionService.getValorDouble("tasaInteresMoraPagare", 5.0);

        return new EstadoCuentaResponse(
                toCreditoResponse(credito), clienteResponse,
                abonos, movimientos, titular, tasaMora);
    }

    private Persona obtenerPersonaActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return personaRepository.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private CreditoResponse toCreditoResponse(Credito c) {
        return new CreditoResponse(
                c.getIdCredito(), c.getVenta().getIdVenta(),
                c.getVenta().getIdVenta(),
                c.getFolio(),
                c.getCliente().getIdCliente(),
                c.getCliente().getNombre() + " " + c.getCliente().getApellidoPaterno(),
                c.getMontoOriginal(), c.getSaldoPendiente(),
                c.getPlazoMeses(), c.getPorcentajeInteres(),
                c.getFechaVencimiento(), c.getEstado(), c.getFechaCreacion());
    }

    private MovimientoCreditoResponse toMovimientoResponse(MovimientoCredito m) {
        return new MovimientoCreditoResponse(
                m.getIdMovimiento(), m.getCredito().getIdCredito(),
                m.getTipo(), m.getMonto(),
                m.getSaldoAnterior(), m.getSaldoNuevo(),
                m.getDescripcion(), m.getFecha(),
                m.getUsuario().getUsuario());
    }
}
