package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.*;
import com.bcsystems.bonds.domain.en.*;
import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.*;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final CajaRepository cajaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final PersonaRepository personaRepository;
    private final AuditoriaService auditoriaService;
    private final VentaPagoRepository ventaPagoRepository;
    private final TipoPagoRepository tipoPagoRepository;
    private final CreditoRepository creditoRepository;
    private final MovimientoCreditoRepository movimientoCreditoRepository;
    private final ReservaProductoRepository reservaProductoRepository;
    private final CarritoItemRapidoRepository carritoItemRapidoRepository;
    private final PrecioClienteRepository precioClienteRepository;

    @Override
    @Transactional
    public VentaResponse crear(VentaRequest request) {
        Caja caja = cajaRepository.findById(request.idCaja())
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        if (caja.getEstado() != CajaEstado.ABIERTA) {
            throw new InvalidEntryException("La caja debe estar abierta");
        }
        if (caja.getTipo() == TipoCaja.CHICA || (caja.getTipo() != null && !TipoCaja.NORMAL.name().equals(caja.getTipo().name()))) {
            throw new InvalidEntryException("La caja chica no puede registrar ventas");
        }

        Persona usuario = obtenerPersonaActual();
        Cliente cliente = request.idCliente() != null
                ? clienteRepository.findById(request.idCliente()).orElse(null) : null;

        if (cliente != null && Boolean.TRUE.equals(cliente.getEnListaNegra())) {
            throw new InvalidEntryException("El cliente está en lista negra y no puede realizar compras a crédito");
        }

        Venta venta = Venta.builder()
                .caja(caja)
                .cliente(cliente)
                .usuario(usuario)
                .tipoVenta(TipoVenta.valueOf(request.tipoVenta()))
                .precioSeleccionado(request.precioSeleccionado())
                .subtotal(request.subtotal())
                .descuento(request.descuento())
                .total(request.total())
                .nota(request.nota())
                .estado(EstadoVenta.COMPLETADA)
                .fecha(LocalDateTime.now())
                .build();
        venta = ventaRepository.save(venta);

        Sucursal sucursal = caja.getSucursal();

        List<VentaDetalle> detalles = new ArrayList<>();
        double subtotalEfectivo = 0.0;
        for (VentaDetalleRequest dto : request.detalles()) {
            VentaDetalle detalle = VentaDetalle.builder()
                    .venta(venta)
                    .producto(dto.idProducto() != null
                            ? productoRepository.findById(dto.idProducto()).orElse(null) : null)
                    .descripcion(dto.descripcion())
                    .cantidad(dto.cantidad())
                    .precioUnitario(dto.precioUnitario())
                    .subtotal(dto.subtotal())
                    .atributosText(dto.atributosText())
                    .build();

            if (detalle.getProducto() != null && cliente != null) {
                Double precioEspecial = precioClienteRepository
                        .findByClienteIdClienteAndProductoIdProducto(cliente.getIdCliente(), detalle.getProducto().getIdProducto())
                        .map(PrecioCliente::getPrecio)
                        .orElse(null);
                if (precioEspecial != null && precioEspecial > 0) {
                    detalle.setPrecioUnitario(precioEspecial);
                    detalle.setSubtotal(precioEspecial * detalle.getCantidad());
                    subtotalEfectivo += detalle.getSubtotal();
                } else {
                    subtotalEfectivo += dto.subtotal();
                }
            } else {
                subtotalEfectivo += dto.subtotal();
            }
            detalles.add(ventaDetalleRepository.save(detalle));

            if (dto.idProducto() != null && dto.idProducto() > 0) {
                Producto p = detalle.getProducto();
                ReservaProducto reserva = reservaProductoRepository
                        .findByCajaIdCajaAndIdProducto(request.idCaja(), dto.idProducto())
                        .orElseThrow(() -> new InvalidEntryException("El producto " + p.getNombre()
                                + " no est\u00e1 reservado para esta caja. Agr\u00e9galo nuevamente al carrito."));
                if (reserva.getExpiraEn().isBefore(LocalDateTime.now())) {
                    throw new InvalidEntryException("La reserva del producto " + p.getNombre()
                            + " ha expirado. Agr\u00e9galo nuevamente al carrito.");
                }
                if (reserva.getCantidad() < dto.cantidad()) {
                    throw new InvalidEntryException("La cantidad reservada de " + p.getNombre()
                            + " es insuficiente (reservado: " + reserva.getCantidad()
                            + ", solicitado: " + dto.cantidad() + ")");
                }
                InventarioSucursal inv = inventarioSucursalRepository
                        .findByProductoIdProductoAndSucursalIdSucursal(dto.idProducto(), sucursal.getIdSucursal())
                        .orElse(null);
                if (inv != null && inv.getStock() < dto.cantidad()) {
                    throw new InvalidEntryException("Stock insuficiente en " + sucursal.getNombre()
                            + " para: " + p.getNombre() + " (disponible: " + inv.getStock()
                            + ", solicitado: " + dto.cantidad() + ")");
                }
                p.setStockActual(p.getStockActual() - dto.cantidad());
                productoRepository.save(p);
                if (inv != null) {
                    inv.setStock(inv.getStock() - dto.cantidad());
                    inventarioSucursalRepository.save(inv);
                }
            }
        }

        if (request.pagos() != null && !request.pagos().isEmpty()) {
            double sumaPagos = request.pagos().stream().mapToDouble(VentaPagoRequest::monto).sum();
            if (sumaPagos + 0.01 < request.total()) {
                throw new InvalidEntryException("La suma de los pagos ($" + String.format("%.2f", sumaPagos)
                        + ") es menor al total de la venta ($" + String.format("%.2f", request.total()) + ")");
            }
            for (VentaPagoRequest pagoReq : request.pagos()) {
                TipoPago tipoPago = tipoPagoRepository.findById(pagoReq.idTipoPago())
                        .orElseThrow(() -> new NotFoundException("Tipo de pago no encontrado"));
                VentaPago pago = VentaPago.builder()
                        .venta(venta)
                        .tipoPago(tipoPago)
                        .monto(pagoReq.monto())
                        .referencia(pagoReq.referencia())
                        .build();
                ventaPagoRepository.save(pago);
            }
        }

        if (TipoVenta.CREDITO.name().equals(request.tipoVenta())) {
            if (cliente == null) {
                throw new InvalidEntryException("Se requiere un cliente para venta a credito");
            }
            if (cliente.getTieneCredito() == null || !cliente.getTieneCredito()) {
                throw new InvalidEntryException("El cliente no tiene credito habilitado");
            }
            if (cliente.getLimiteCredito() != null && cliente.getLimiteCredito() > 0) {
                double disponible = cliente.getLimiteCredito()
                        - (cliente.getSaldoActual() != null ? cliente.getSaldoActual() : 0);
                if (request.total() > disponible) {
                    throw new InvalidEntryException("El total ($" + String.format("%.2f", request.total())
                            + ") excede el limite de credito disponible ($" + String.format("%.2f", disponible) + ")");
                }
            }

            double porcentajeInteres = request.porcentajeInteres() != null ? request.porcentajeInteres() : 0;
            double montoOriginal = request.total() + (request.total() * porcentajeInteres / 100);
            int plazoMeses = request.plazoMeses() != null ? request.plazoMeses() : 1;

            Credito credito = Credito.builder()
                    .venta(venta)
                    .folio(generarFolioPagaré())
                    .cliente(cliente)
                    .montoOriginal(montoOriginal)
                    .saldoPendiente(montoOriginal)
                    .plazoMeses(plazoMeses)
                    .porcentajeInteres(porcentajeInteres)
                    .fechaVencimiento(LocalDateTime.now().plusMonths(plazoMeses))
                    .estado(EstadoCredito.ACTIVO)
                    .fechaCreacion(LocalDateTime.now())
                    .usuario(usuario)
                    .build();
            credito = creditoRepository.save(credito);

            MovimientoCredito mov = MovimientoCredito.builder()
                    .credito(credito)
                    .tipo(TipoMovimientoCredito.CARGO)
                    .monto(montoOriginal)
                    .saldoAnterior(cliente.getSaldoActual() != null ? cliente.getSaldoActual() : 0)
                    .saldoNuevo((cliente.getSaldoActual() != null ? cliente.getSaldoActual() : 0) + montoOriginal)
                    .descripcion("Cargo por venta a credito #" + venta.getIdVenta())
                    .fecha(LocalDateTime.now())
                    .usuario(usuario)
                    .build();
            movimientoCreditoRepository.save(mov);

            cliente.setSaldoActual((cliente.getSaldoActual() != null ? cliente.getSaldoActual() : 0) + montoOriginal);
            clienteRepository.save(cliente);
        } else {
            caja.setSaldoActual(caja.getSaldoActual() + request.total());
            cajaRepository.save(caja);
        }

        reservaProductoRepository.deleteByCajaIdCaja(request.idCaja());
        carritoItemRapidoRepository.deleteByCajaIdCajaNative(request.idCaja());

        auditoriaService.registrar("Venta", venta.getIdVenta(), AccionAuditoria.CREACION.name(),
                usuario.getUsuario(), "Venta $" + request.total() + " - " + caja.getNombre());

        return toResponse(venta, detalles);
    }

    @Override
    public VentaResponse obtenerPorId(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        List<VentaDetalle> detalles = ventaDetalleRepository.findByVentaIdVenta(id);
        return toResponse(venta, detalles);
    }

    @Override
    public List<VentaResponse> listarPorCaja(Integer idCaja) {
        return ventaRepository.findByCajaIdCajaAndEstadoOrderByFechaDesc(idCaja, EstadoVenta.COMPLETADA)
                .stream().map(v -> toResponse(v, ventaDetalleRepository.findByVentaIdVenta(v.getIdVenta())))
                .toList();
    }

    @Override
    public Page<VentaResponse> listar(Integer idSucursal, Integer idCaja, String estado,
                                       LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        EstadoVenta estadoEnum = estado != null ? EstadoVenta.valueOf(estado) : null;
        return ventaRepository.listarConFiltros(idSucursal, idCaja, estadoEnum, fechaInicio, fechaFin, pageable)
                .map(v -> toResponse(v, ventaDetalleRepository.findByVentaIdVenta(v.getIdVenta())));
    }

    @Override
    @Transactional
    public VentaResponse solicitarCancelacion(Integer id, String motivo) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        if (venta.getEstado() != EstadoVenta.COMPLETADA) {
            throw new InvalidEntryException("Solo se pueden solicitar cancelaciones de ventas completadas");
        }
        Persona solicitante = obtenerPersonaActual();
        if (motivo == null || motivo.isBlank()) {
            throw new InvalidEntryException("Debe indicar un motivo de cancelacion");
        }
        venta.setEstado(EstadoVenta.SOLICITADA_CANCELACION);
        venta.setMotivoCancelacion(motivo);
        venta.setSolicitanteCancelacion(solicitante);
        venta.setFechaSolicitudCancelacion(LocalDateTime.now());
        venta = ventaRepository.save(venta);

        auditoriaService.registrar("Venta", id, "ACTUALIZACION", solicitante.getUsuario(),
                "Solicitud de cancelacion: " + motivo);

        return toResponse(venta, ventaDetalleRepository.findByVentaIdVenta(id));
    }

    @Override
    @Transactional
    public VentaResponse cancelar(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));

        Persona autorizador = obtenerPersonaActual();
        if (autorizador.getRol() != Rol.ADMINISTRADOR && autorizador.getRol() != Rol.SISTEMAS) {
            throw new InvalidEntryException("Solo un administrador puede autorizar la cancelacion");
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new InvalidEntryException("La venta ya está cancelada");
        }
        if (venta.getEstado() != EstadoVenta.SOLICITADA_CANCELACION) {
            throw new InvalidEntryException("La venta no tiene una solicitud de cancelacion pendiente");
        }
        venta.setEstado(EstadoVenta.CANCELADA);
        venta.setAutorizadorCancelacion(autorizador);
        venta.setFechaAutorizacionCancelacion(LocalDateTime.now());
        venta = ventaRepository.save(venta);

        Sucursal sucursal = venta.getCaja().getSucursal();
        List<VentaDetalle> detalles = ventaDetalleRepository.findByVentaIdVenta(id);
        for (VentaDetalle d : detalles) {
            if (d.getProducto() != null) {
                Producto p = d.getProducto();
                p.setStockActual(p.getStockActual() + d.getCantidad());
                productoRepository.save(p);
                InventarioSucursal inv = inventarioSucursalRepository
                        .findByProductoIdProductoAndSucursalIdSucursal(p.getIdProducto(), sucursal.getIdSucursal())
                        .orElse(null);
                if (inv != null) {
                    inv.setStock(inv.getStock() + d.getCantidad());
                    inventarioSucursalRepository.save(inv);
                }
            }
        }

        if (venta.getTipoVenta() == TipoVenta.CONTADO) {
            Caja caja = venta.getCaja();
            caja.setSaldoActual(caja.getSaldoActual() - venta.getTotal());
            cajaRepository.save(caja);
        } else if (venta.getTipoVenta() == TipoVenta.CREDITO) {
            creditoRepository.findByVentaIdVenta(id).ifPresent(credito -> {
                if (credito.getEstado() == EstadoCredito.ACTIVO || credito.getEstado() == EstadoCredito.VENCIDO) {
                    Cliente cliente = credito.getCliente();
                    double abonado = credito.getMontoOriginal() - credito.getSaldoPendiente();
                    double reversar = abonado < credito.getMontoOriginal() ? abonado : credito.getMontoOriginal();
                    cliente.setSaldoActual(Math.max(0, (cliente.getSaldoActual() != null ? cliente.getSaldoActual() : 0) - reversar));
                    clienteRepository.save(cliente);
                    credito.setEstado(EstadoCredito.CANCELADO);
                    creditoRepository.save(credito);
                }
            });
        }

        auditoriaService.registrar("Venta", id, "ACTUALIZACION", autorizador.getUsuario(),
                "Cancelacion autorizada - Venta #" + id);

        return toResponse(venta, detalles);
    }

    @Override
    @Transactional
    public VentaResponse rechazarCancelacion(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        if (venta.getEstado() != EstadoVenta.SOLICITADA_CANCELACION) {
            throw new InvalidEntryException("La venta no tiene una solicitud de cancelacion pendiente");
        }
        Persona rechazador = obtenerPersonaActual();
        if (rechazador.getRol() != Rol.ADMINISTRADOR && rechazador.getRol() != Rol.SISTEMAS) {
            throw new InvalidEntryException("Solo un administrador puede rechazar la cancelacion");
        }
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setMotivoCancelacion(null);
        venta.setSolicitanteCancelacion(null);
        venta.setFechaSolicitudCancelacion(null);
        venta.setAutorizadorCancelacion(null);
        venta.setFechaAutorizacionCancelacion(null);
        venta = ventaRepository.save(venta);

        auditoriaService.registrar("Venta", id, "ACTUALIZACION", rechazador.getUsuario(),
                "Solicitud de cancelacion rechazada - Venta #" + id);

        return toResponse(venta, ventaDetalleRepository.findByVentaIdVenta(id));
    }

    @Override
    @Transactional
    public VentaResponse ponerEnEspera(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        venta.setEstado(EstadoVenta.ESPERA);
        venta = ventaRepository.save(venta);
        return toResponse(venta, ventaDetalleRepository.findByVentaIdVenta(id));
    }

    @Override
    @Transactional
    public VentaResponse reanudar(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        if (venta.getEstado() != EstadoVenta.ESPERA) {
            throw new InvalidEntryException("La venta no está en espera");
        }
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta = ventaRepository.save(venta);
        return toResponse(venta, ventaDetalleRepository.findByVentaIdVenta(id));
    }

    @Override
    public List<VentaResponse> ventasEnEspera(Integer idCaja) {
        return ventaRepository.findByCajaIdCajaAndEstadoOrderByFechaDesc(idCaja, EstadoVenta.ESPERA)
                .stream().map(v -> toResponse(v, ventaDetalleRepository.findByVentaIdVenta(v.getIdVenta())))
                .toList();
    }

    @Override
    public List<VentaResponse> listarPorSucursal(Integer idSucursal) {
        return ventaRepository.findByCajaSucursalIdSucursalAndEstadoOrderByFechaDesc(idSucursal, EstadoVenta.COMPLETADA)
                .stream().limit(50)
                .map(v -> toResponse(v, ventaDetalleRepository.findByVentaIdVenta(v.getIdVenta())))
                .toList();
    }

    @Override
    @Transactional
    public VentaResponse ventaRapida(Integer idCaja, String descripcion, Double precioCompra,
                                      Double precioVenta, Integer cantidad, Integer idCliente) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        if (caja.getEstado() != CajaEstado.ABIERTA) {
            throw new InvalidEntryException("La caja debe estar abierta");
        }
        if (caja.getTipo() == TipoCaja.CHICA || (caja.getTipo() != null && !TipoCaja.NORMAL.name().equals(caja.getTipo().name()))) {
            throw new InvalidEntryException("La caja chica no puede registrar ventas");
        }

        Persona usuario = obtenerPersonaActual();
        Cliente cliente = idCliente != null
                ? clienteRepository.findById(idCliente).orElse(null) : null;

        Double subtotal = precioVenta * cantidad;

        Venta venta = Venta.builder()
                .caja(caja)
                .cliente(cliente)
                .usuario(usuario)
                .tipoVenta(TipoVenta.CONTADO)
                .precioSeleccionado(1)
                .subtotal(subtotal)
                .descuento(0.0)
                .total(subtotal)
                .estado(EstadoVenta.COMPLETADA)
                .fecha(LocalDateTime.now())
                .build();
        venta = ventaRepository.save(venta);

        VentaDetalle detalle = VentaDetalle.builder()
                .venta(venta)
                .descripcion(descripcion)
                .cantidad(cantidad)
                .precioUnitario(precioVenta)
                .subtotal(subtotal)
                .build();
        ventaDetalleRepository.save(detalle);

        caja.setSaldoActual(caja.getSaldoActual() + subtotal);
        cajaRepository.save(caja);

        return toResponse(venta, List.of(detalle));
    }

    private Persona obtenerPersonaActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return personaRepository.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private String generarFolioPagaré() {
        String prefix = "PAGARE-";
        int num = 1;
        while (creditoRepository.existsByFolio(prefix + String.format("%05d", num))) {
            num++;
        }
        return prefix + String.format("%05d", num);
    }

    private VentaResponse toResponse(Venta v, List<VentaDetalle> detalles) {
        List<VentaDetalleResponse> detalleResponses = detalles.stream()
                .map(d -> new VentaDetalleResponse(
                        d.getIdVentaDetalle(),
                        d.getProducto() != null ? d.getProducto().getIdProducto() : null,
                        d.getProducto() != null ? d.getProducto().getSku() : null,
                        d.getProducto() != null ? d.getProducto().getNombre() : null,
                        d.getDescripcion(),
                        d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal(),
                        d.getAtributosText()))
                .toList();

        List<VentaPagoResponse> pagoResponses = ventaPagoRepository.findByVentaIdVenta(v.getIdVenta())
                .stream().map(p -> new VentaPagoResponse(
                        p.getIdVentaPago(),
                        p.getTipoPago().getIdTipoPago(),
                        p.getTipoPago().getNombre(),
                        p.getMonto(),
                        p.getReferencia()))
                .toList();

        Credito credito = creditoRepository.findByVentaIdVenta(v.getIdVenta()).orElse(null);

        return new VentaResponse(
                v.getIdVenta(), v.getCaja().getIdCaja(),
                v.getCaja().getNombre(),
                v.getCaja().getSucursal().getIdSucursal(),
                v.getCaja().getSucursal().getNombre(),
                v.getCliente() != null ? v.getCliente().getIdCliente() : null,
                v.getCliente() != null ? v.getCliente().getNombre() + " " + v.getCliente().getApellidoPaterno() : null,
                v.getUsuario().getUsuario(),
                v.getTipoVenta().name(), v.getPrecioSeleccionado(),
                v.getSubtotal(), v.getDescuento(), v.getTotal(),
                v.getEstado().name(), v.getNota(), v.getFecha(), detalleResponses, pagoResponses,
                credito != null ? credito.getFolio() : null,
                credito != null ? credito.getPlazoMeses() : null,
                credito != null ? credito.getPorcentajeInteres() : null,
                v.getMotivoCancelacion(),
                v.getSolicitanteCancelacion() != null ? v.getSolicitanteCancelacion().getUsuario() : null,
                v.getFechaSolicitudCancelacion());
    }
}