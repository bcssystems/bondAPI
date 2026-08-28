package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.*;
import com.bcsystems.bonds.domain.en.*;
import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.*;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;
    private final SucursalRepository sucursalRepository;
    private final CorteCajaRepository corteCajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final VentaRepository ventaRepository;
    private final PersonaRepository personaRepository;
    private final AuditoriaService auditoriaService;
    private final VentaPagoRepository ventaPagoRepository;
    private final CorteDetallePagoRepository corteDetallePagoRepository;
    private final TipoPagoRepository tipoPagoRepository;

    @Override
    public List<CajaResponse> listar() {
        return cajaRepository.findByActivaTrueOrderByNombre().stream()
                .map(this::toResponse).toList();
    }

    @Override
    public List<CajaResponse> listarPorSucursal(Integer idSucursal) {
        return cajaRepository.findBySucursalIdSucursalAndActivaTrue(idSucursal).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public CajaResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Override
    @Transactional
    public CajaResponse crear(CajaRequest request) {
        Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        TipoCaja tipo = request.tipo() != null ? TipoCaja.valueOf(request.tipo()) : TipoCaja.NORMAL;
        validarCajaUnica(sucursal, tipo, null);

        Caja caja = Caja.builder()
                .nombre(request.nombre())
                .tipo(tipo)
                .sucursal(sucursal)
                .estado(CajaEstado.CERRADA)
                .saldoActual(0.0)
                .activa(true)
                .build();
        caja = cajaRepository.save(caja);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("Caja", caja.getIdCaja(), AccionAuditoria.CREACION.name(),
                usuario, "Caja creada: " + caja.getNombre());

        return toResponse(caja);
    }

    @Override
    @Transactional
    public CajaResponse actualizar(Integer id, CajaRequest request) {
        Caja caja = buscarOExcepcion(id);
        Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        TipoCaja tipo = request.tipo() != null ? TipoCaja.valueOf(request.tipo()) : TipoCaja.NORMAL;
        validarCajaUnica(sucursal, tipo, id);
        caja.setNombre(request.nombre());
        caja.setSucursal(sucursal);
        caja.setTipo(tipo);
        caja = cajaRepository.save(caja);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("Caja", id, AccionAuditoria.ACTUALIZACION.name(),
                usuario, "Caja actualizada: " + caja.getNombre());

        return toResponse(caja);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Caja caja = buscarOExcepcion(id);
        caja.setActiva(false);
        cajaRepository.save(caja);
    }

    @Override
    @Transactional
    public CajaResponse abrirCaja(Integer id, AperturaCajaRequest request) {
        Caja caja = buscarOExcepcion(id);
        if (caja.getEstado() == CajaEstado.ABIERTA) {
            throw new InvalidEntryException("La caja ya está abierta");
        }
        if (request.saldoInicial() < 0) {
            throw new InvalidEntryException("El saldo inicial no puede ser negativo");
        }
        caja.setEstado(CajaEstado.ABIERTA);
        caja.setSaldoActual(request.saldoInicial());
        caja.setFechaApertura(LocalDateTime.now());
        caja.setFechaCierre(null);
        caja = cajaRepository.save(caja);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("Caja", id, AccionAuditoria.CREACION.name(),
                usuario, "Caja abierta con saldo inicial: $" + request.saldoInicial());

        return toResponse(caja);
    }

    @Override
    @Transactional
    public CajaResponse cerrarCaja(Integer id) {
        Caja caja = buscarOExcepcion(id);
        if (caja.getEstado() == CajaEstado.CERRADA) {
            throw new InvalidEntryException("La caja ya está cerrada");
        }
        caja.setEstado(CajaEstado.CERRADA);
        caja.setFechaCierre(LocalDateTime.now());
        caja = cajaRepository.save(caja);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("Caja", id, AccionAuditoria.ACTUALIZACION.name(),
                usuario, "Caja cerrada");

        return toResponse(caja);
    }

    @Override
    @Transactional
    public CajaResponse ingresarEfectivo(Integer id, MovimientoCajaRequest request) {
        Caja caja = buscarOExcepcion(id);
        validarCajaAbierta(caja);

        MovimientoCaja mov = MovimientoCaja.builder()
                .caja(caja)
                .tipo(TipoMovimientoCaja.INGRESO)
                .monto(request.monto())
                .motivo(request.motivo())
                .usuario(obtenerPersonaActual())
                .fecha(LocalDateTime.now())
                .build();
        movimientoCajaRepository.save(mov);

        caja.setSaldoActual(caja.getSaldoActual() + request.monto());
        caja = cajaRepository.save(caja);

        return toResponse(caja);
    }

    @Override
    @Transactional
    public CajaResponse retirarEfectivo(Integer id, MovimientoCajaRequest request) {
        Caja caja = buscarOExcepcion(id);
        validarCajaAbierta(caja);

        if (caja.getSaldoActual() < request.monto()) {
            throw new InvalidEntryException("Saldo insuficiente en caja");
        }

        MovimientoCaja mov = MovimientoCaja.builder()
                .caja(caja)
                .tipo(TipoMovimientoCaja.EGRESO)
                .monto(request.monto())
                .motivo(request.motivo())
                .usuario(obtenerPersonaActual())
                .fecha(LocalDateTime.now())
                .build();
        movimientoCajaRepository.save(mov);

        caja.setSaldoActual(caja.getSaldoActual() - request.monto());
        caja = cajaRepository.save(caja);

        return toResponse(caja);
    }

    @Override
    public List<MovimientoCajaResponse> movimientos(Integer id) {
        return movimientoCajaRepository.findByCajaIdCajaOrderByFechaDesc(id).stream()
                .map(m -> new MovimientoCajaResponse(
                        m.getIdMovimientoCaja(), m.getCaja().getIdCaja(),
                        m.getCaja().getNombre(), m.getTipo().name(),
                        m.getMonto(), m.getMotivo(), m.getUsuario().getUsuario(), m.getFecha()))
                .toList();
    }

    @Override
    public CorteResponse previewCorte(Integer id) {
        Caja caja = buscarOExcepcion(id);
        LocalDateTime apertura = caja.getFechaApertura();
        LocalDateTime ahora = LocalDateTime.now();

        List<Venta> ventas = ventaRepository
                .findByCajaIdCajaAndFechaBetweenOrderByFechaDesc(id, apertura, ahora);
        List<MovimientoCaja> movs = movimientoCajaRepository
                .findByCajaIdCajaAndFechaBetweenOrderByFechaDesc(id, apertura, ahora);

        double totalVentas = ventas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
                .mapToDouble(Venta::getTotal).sum();
        double totalContado = ventas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA && v.getTipoVenta() == TipoVenta.CONTADO)
                .mapToDouble(Venta::getTotal).sum();
        double totalCredito = ventas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA && v.getTipoVenta() == TipoVenta.CREDITO)
                .mapToDouble(Venta::getTotal).sum();
        double totalIngresos = movs.stream()
                .filter(m -> m.getTipo() == TipoMovimientoCaja.INGRESO)
                .mapToDouble(MovimientoCaja::getMonto).sum();
        double totalEgresos = movs.stream()
                .filter(m -> m.getTipo() == TipoMovimientoCaja.EGRESO)
                .mapToDouble(MovimientoCaja::getMonto).sum();

        double saldoInicial = caja.getSaldoActual() - totalIngresos + totalEgresos - totalContado;
        double saldoEsperado = saldoInicial + totalVentas + totalIngresos - totalEgresos;

        List<VentaPago> pagosEnRango = ventaPagoRepository.findByCajaAndFechaRange(id, apertura, ahora);
        List<CorteDetallePagoDto> detallePagos = pagosEnRango.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        vp -> vp.getTipoPago().getIdTipoPago(),
                        java.util.stream.Collectors.summingDouble(VentaPago::getMonto)))
                .entrySet().stream()
                .map(e -> {
                    TipoPago tp = pagosEnRango.stream()
                            .filter(vp -> vp.getTipoPago().getIdTipoPago().equals(e.getKey()))
                            .findFirst().get().getTipoPago();
                    return new CorteDetallePagoDto(e.getKey(), tp.getNombre(), e.getValue(), null);
                })
                .toList();

        double totalReal = 0.0;
        double diferencia = 0.0;

        return new CorteResponse(null, id, caja.getNombre(),
                caja.getSucursal().getIdSucursal(), caja.getSucursal().getNombre(),
                saldoInicial,
                totalVentas, totalContado, totalCredito,
                totalIngresos, totalEgresos, caja.getSaldoActual(),
                saldoEsperado,
                apertura, ahora, obtenerUsuarioActual(), detallePagos,
                totalReal, diferencia);
    }

    @Override
    @Transactional
    public CorteResponse realizarCorte(Integer id) {
        Caja caja = buscarOExcepcion(id);
        validarCajaAbierta(caja);

        CorteResponse preview = previewCorte(id);
        String usuario = obtenerUsuarioActual();

        CorteCaja corte = CorteCaja.builder()
                .caja(caja)
                .saldoInicial(preview.saldoInicial())
                .totalVentas(preview.totalVentas())
                .totalVentasContado(preview.totalVentasContado())
                .totalVentasCredito(preview.totalVentasCredito())
                .totalIngresos(preview.totalIngresos())
                .totalEgresos(preview.totalEgresos())
                .saldoFinalContado(preview.saldoFinalContado())
                .fechaApertura(preview.fechaApertura())
                .fechaCierre(LocalDateTime.now())
                .usuario(obtenerPersonaActual())
                .build();
        corteCajaRepository.save(corte);

        caja.setEstado(CajaEstado.CERRADA);
        caja.setFechaCierre(LocalDateTime.now());
        cajaRepository.save(caja);

        if (preview.detallePagos() != null) {
            for (CorteDetallePagoDto dto : preview.detallePagos()) {
                TipoPago tp = tipoPagoRepository.findById(dto.idTipoPago())
                        .orElse(null);
                if (tp != null) {
                    CorteDetallePago det = CorteDetallePago.builder()
                            .corte(corte)
                            .tipoPago(tp)
                            .monto(dto.monto())
                            .build();
                    corteDetallePagoRepository.save(det);
                }
            }
        }

        auditoriaService.registrar("CorteCaja", corte.getIdCorte(), AccionAuditoria.CREACION.name(),
                usuario, "Corte realizado - Total ventas: $" + preview.totalVentas());

        return new CorteResponse(
                corte.getIdCorte(), id, caja.getNombre(),
                caja.getSucursal().getIdSucursal(), caja.getSucursal().getNombre(),
                preview.saldoInicial(), preview.totalVentas(),
                preview.totalVentasContado(), preview.totalVentasCredito(),
                preview.totalIngresos(), preview.totalEgresos(),
                preview.saldoFinalContado(), preview.saldoEsperado(),
                preview.fechaApertura(),
                corte.getFechaCierre(), usuario, preview.detallePagos(),
                preview.totalReal(), preview.diferencia());
    }

    @Override
    @Transactional
    public CorteResponse actualizarDetallePagos(Integer idCorte,
                                                  List<CorteDetallePagoUpdateRequest.ItemDetallePago> pagos) {
        Persona persona = obtenerPersonaActual();
        if (persona.getRol() != Rol.ADMINISTRADOR && persona.getRol() != Rol.SISTEMAS) {
            throw new InvalidEntryException("Solo los administradores pueden editar los conteos de un corte");
        }

        CorteCaja corte = corteCajaRepository.findById(idCorte)
                .orElseThrow(() -> new NotFoundException("Corte no encontrado con id: " + idCorte));

        List<CorteDetallePago> detalles = corteDetallePagoRepository.findByCorteIdCorte(idCorte);

        for (CorteDetallePagoUpdateRequest.ItemDetallePago item : pagos) {
            detalles.stream()
                    .filter(d -> d.getTipoPago().getIdTipoPago().equals(item.idTipoPago()))
                    .findFirst()
                    .ifPresent(d -> d.setMontoReal(item.montoReal()));
        }
        corteDetallePagoRepository.saveAll(detalles);

        auditoriaService.registrar("CorteCaja", idCorte, AccionAuditoria.ACTUALIZACION.name(),
                persona.getUsuario(), "Conteos reales actualizados en corte #" + idCorte);

        return toResponseCorte(corte);
    }

    private CorteResponse toResponseCorte(CorteCaja corte) {
        List<CorteDetallePago> detalles = corteDetallePagoRepository.findByCorteIdCorte(corte.getIdCorte());
        List<CorteDetallePagoDto> detallePagos = detalles.stream()
                .map(d -> new CorteDetallePagoDto(
                        d.getTipoPago().getIdTipoPago(),
                        d.getTipoPago().getNombre(),
                        d.getMonto(),
                        d.getMontoReal()))
                .toList();
        double totalReal = detallePagos.stream()
                .filter(d -> d.montoReal() != null)
                .mapToDouble(CorteDetallePagoDto::montoReal)
                .sum();
        double sistema = detallePagos.stream().mapToDouble(CorteDetallePagoDto::monto).sum();
        double diferencia = totalReal > 0 ? totalReal - sistema : 0.0;
        return new CorteResponse(
                corte.getIdCorte(), corte.getCaja().getIdCaja(), corte.getCaja().getNombre(),
                corte.getCaja().getSucursal().getIdSucursal(), corte.getCaja().getSucursal().getNombre(),
                corte.getSaldoInicial(), corte.getTotalVentas(),
                corte.getTotalVentasContado(), corte.getTotalVentasCredito(),
                corte.getTotalIngresos(), corte.getTotalEgresos(),
                corte.getSaldoFinalContado(), null,
                corte.getFechaApertura(), corte.getFechaCierre(),
                corte.getUsuario().getUsuario(), detallePagos,
                totalReal > 0 ? totalReal : null,
                totalReal > 0 ? diferencia : null);
    }

    private Caja buscarOExcepcion(Integer id) {
        return cajaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada con id: " + id));
    }

    private void validarCajaAbierta(Caja caja) {
        if (caja.getEstado() != CajaEstado.ABIERTA) {
            throw new InvalidEntryException("La caja debe estar abierta para realizar esta operación");
        }
    }

    private void validarCajaUnica(Sucursal sucursal, TipoCaja tipo, Integer idExcluir) {
        boolean existe = cajaRepository.findBySucursalIdSucursalAndActivaTrue(sucursal.getIdSucursal()).stream()
                .anyMatch(c -> c.getTipo() == tipo
                        && (idExcluir == null || !c.getIdCaja().equals(idExcluir)));
        if (existe) {
            throw new InvalidEntryException("Ya existe una caja " + tipo.name().toLowerCase()
                    + " activa en la sucursal " + sucursal.getNombre());
        }
    }

    private String obtenerUsuarioActual() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Persona obtenerPersonaActual() {
        String username = obtenerUsuarioActual();
        return personaRepository.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private CajaResponse toResponse(Caja caja) {
        return new CajaResponse(
                caja.getIdCaja(), caja.getNombre(),
                caja.getTipo() != null ? caja.getTipo().name() : "NORMAL",
                caja.getSucursal().getIdSucursal(),
                caja.getSucursal().getNombre(),
                caja.getEstado().name(),
                caja.getSaldoActual(),
                caja.getFechaApertura(),
                caja.getFechaCierre(),
                caja.getActiva());
    }
}
