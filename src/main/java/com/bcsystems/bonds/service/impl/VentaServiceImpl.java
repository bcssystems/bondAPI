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

    @Override
    @Transactional
    public VentaResponse crear(VentaRequest request) {
        Caja caja = cajaRepository.findById(request.idCaja())
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        if (caja.getEstado() != CajaEstado.ABIERTA) {
            throw new InvalidEntryException("La caja debe estar abierta");
        }

        Persona usuario = obtenerPersonaActual();
        Cliente cliente = request.idCliente() != null
                ? clienteRepository.findById(request.idCliente()).orElse(null) : null;

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
            detalles.add(ventaDetalleRepository.save(detalle));

            if (dto.idProducto() != null && dto.idProducto() > 0) {
                Producto p = productoRepository.findById(dto.idProducto()).orElse(null);
                if (p != null) {
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
                    actualizarStockPadre(p);
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
            double disponible = (cliente.getLimiteCredito() != null ? cliente.getLimiteCredito() : 0)
                    - (cliente.getSaldoActual() != null ? cliente.getSaldoActual() : 0);
            if (request.total() > disponible) {
                throw new InvalidEntryException("El total ($" + String.format("%.2f", request.total())
                        + ") excede el limite de credito disponible ($" + String.format("%.2f", disponible) + ")");
            }

            double porcentajeInteres = request.porcentajeInteres() != null ? request.porcentajeInteres() : 0;
            double montoOriginal = request.total() + (request.total() * porcentajeInteres / 100);
            int plazoMeses = request.plazoMeses() != null ? request.plazoMeses() : 1;

            Credito credito = Credito.builder()
                    .venta(venta)
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
    public VentaResponse cancelar(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new InvalidEntryException("La venta ya está cancelada");
        }
        venta.setEstado(EstadoVenta.CANCELADA);
        venta = ventaRepository.save(venta);

        Sucursal sucursal = venta.getCaja().getSucursal();
        List<VentaDetalle> detalles = ventaDetalleRepository.findByVentaIdVenta(id);
        for (VentaDetalle d : detalles) {
            if (d.getProducto() != null) {
                Producto p = d.getProducto();
                p.setStockActual(p.getStockActual() + d.getCantidad());
                productoRepository.save(p);
                actualizarStockPadre(p);
                InventarioSucursal inv = inventarioSucursalRepository
                        .findByProductoIdProductoAndSucursalIdSucursal(p.getIdProducto(), sucursal.getIdSucursal())
                        .orElse(null);
                if (inv != null) {
                    inv.setStock(inv.getStock() + d.getCantidad());
                    inventarioSucursalRepository.save(inv);
                }
            }
        }

        Caja caja = venta.getCaja();
        caja.setSaldoActual(caja.getSaldoActual() - venta.getTotal());
        cajaRepository.save(caja);

        return toResponse(venta, detalles);
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
                v.getEstado().name(), v.getNota(), v.getFecha(), detalleResponses, pagoResponses);
    }

    private void actualizarStockPadre(Producto variante) {
        Producto padre = variante.getProductoPadre();
        if (padre != null) {
            Integer totalStock = productoRepository.findByProductoPadreIdProducto(padre.getIdProducto())
                    .stream()
                    .mapToInt(Producto::getStockActual)
                    .sum();
            padre.setStockActual(totalStock);
            productoRepository.save(padre);
        }
    }
}
