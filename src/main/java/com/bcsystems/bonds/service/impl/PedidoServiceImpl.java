package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.*;
import com.bcsystems.bonds.domain.en.EstadoPedido;
import com.bcsystems.bonds.domain.en.TipoMovimiento;
import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.*;
import com.bcsystems.bonds.service.PedidoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final double DEFAULT_MARKUP_PERCENTAGE = 30.0;

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final SucursalRepository sucursalRepository;
    private final PersonaRepository personaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             PedidoDetalleRepository pedidoDetalleRepository,
                             ProveedorRepository proveedorRepository,
                             SucursalRepository sucursalRepository,
                             PersonaRepository personaRepository,
                             ProductoRepository productoRepository,
                             InventarioSucursalRepository inventarioSucursalRepository,
                             MovimientoStockRepository movimientoStockRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
        this.proveedorRepository = proveedorRepository;
        this.sucursalRepository = sucursalRepository;
        this.personaRepository = personaRepository;
        this.productoRepository = productoRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.movimientoStockRepository = movimientoStockRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listar(String search, String estado, Integer idProveedor, Pageable pageable) {
        EstadoPedido ep = estado != null ? EstadoPedido.valueOf(estado) : null;
        return pedidoRepository.buscarConFiltros(search, ep, idProveedor, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPorId(Integer id) {
        return toResponse(pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado con id: " + id)));
    }

    @Transactional
    @Override
    public PedidoResponse crear(PedidoRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.idProveedor())
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        Persona persona = obtenerPersonaActual();

        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new InvalidEntryException("El pedido debe tener al menos un detalle");
        }

        Pedido pedido = Pedido.builder()
                .folio(generarFolio())
                .proveedor(proveedor)
                .sucursal(sucursal)
                .persona(persona)
                .estado(EstadoPedido.PENDIENTE)
                .nota(request.nota())
                .detalles(new ArrayList<>())
                .build();

        for (PedidoRequest.PedidoDetalleRequest dr : request.detalles()) {
            Producto producto = productoRepository.findById(dr.idProducto())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + dr.idProducto()));

            PedidoDetalle detalle = PedidoDetalle.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidadPedida(dr.cantidadPedida())
                    .precioCompraUnitario(dr.precioCompraUnitario())
                    .build();
            pedido.getDetalles().add(detalle);
        }

        pedido = pedidoRepository.save(pedido);
        return toResponse(pedido);
    }

    @Transactional
    @Override
    public PedidoResponse cancelar(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado con id: " + id));

        if (pedido.getEstado() == EstadoPedido.COMPLETADO) {
            throw new InvalidEntryException("No se puede cancelar un pedido ya completado");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new InvalidEntryException("El pedido ya está cancelado");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido = pedidoRepository.save(pedido);
        return toResponse(pedido);
    }

    @Transactional
    @Override
    public PedidoResponse recibir(Integer id, RecepcionRequest request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado con id: " + id));

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new InvalidEntryException("No se puede recibir un pedido cancelado");
        }
        if (pedido.getEstado() == EstadoPedido.COMPLETADO) {
            throw new InvalidEntryException("El pedido ya está completamente recibido");
        }

        for (RecepcionRequest.RecepcionDetalleRequest dr : request.detalles()) {
            PedidoDetalle detalle = pedidoDetalleRepository.findById(dr.idPedidoDetalle())
                    .orElseThrow(() -> new NotFoundException("Detalle de pedido no encontrado con id: " + dr.idPedidoDetalle()));

            if (!detalle.getPedido().getIdPedido().equals(id)) {
                throw new InvalidEntryException("El detalle no pertenece a este pedido");
            }

            Integer recibido = dr.cantidadRecibida();
            if (recibido == null || recibido < 0) {
                throw new InvalidEntryException("Cantidad recibida inválida para el detalle " + dr.idPedidoDetalle());
            }

            Integer recibidoPrevio = detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0;
            if (recibidoPrevio + recibido > detalle.getCantidadPedida()) {
                throw new InvalidEntryException("La cantidad recibida excede lo pedido para el detalle " + dr.idPedidoDetalle());
            }

            Double precioCompra = dr.precioCompraUnitario() != null ? dr.precioCompraUnitario() : detalle.getPrecioCompraUnitario();
            detalle.setCantidadRecibida(recibidoPrevio + recibido);
            detalle.setCostoUltimo(precioCompra);
            pedidoDetalleRepository.save(detalle);

            if (recibido > 0) {
                Producto producto = detalle.getProducto();
                Integer stockAnterior = producto.getStockActual();
                Integer nuevoStock = stockAnterior + recibido;

                producto.setStockActual(nuevoStock);

                Double costoActual = producto.getCostoPromedio();
                if (costoActual == null) costoActual = 0.0;
                if (stockAnterior + recibido > 0) {
                    double nuevoCosto = ((costoActual * stockAnterior) + (precioCompra * recibido)) / (stockAnterior + recibido);
                    producto.setCostoPromedio(nuevoCosto);
                }

                productoRepository.save(producto);

                InventarioSucursal inv = inventarioSucursalRepository
                        .findByProductoIdProductoAndSucursalIdSucursal(
                                producto.getIdProducto(), pedido.getSucursal().getIdSucursal())
                        .orElse(null);
                if (inv != null) {
                    inv.setStock(inv.getStock() + recibido);
                    inventarioSucursalRepository.save(inv);
                }

                actualizarStockPadre(producto);

                if (dr.precio1() != null && dr.precio1() > 0) {
                    producto.setPrecio1(dr.precio1());
                } else if (dr.precioVentaSugerido() != null && dr.precioVentaSugerido() > 0) {
                    producto.setPrecio1(dr.precioVentaSugerido());
                }
                if (dr.precio2() != null && dr.precio2() > 0) producto.setPrecio2(dr.precio2());
                if (dr.precio3() != null && dr.precio3() > 0) producto.setPrecio3(dr.precio3());
                if (dr.precio4() != null && dr.precio4() > 0) producto.setPrecio4(dr.precio4());
                productoRepository.save(producto);

                Persona persona = obtenerPersonaActual();
                String usuario = persona != null ? persona.getNombre() + " " + (persona.getApellido() != null ? persona.getApellido() : "") : "SISTEMA";
                MovimientoStock ms = MovimientoStock.builder()
                        .producto(producto)
                        .sucursal(pedido.getSucursal())
                        .tipoMovimiento(TipoMovimiento.RECEPCION_PEDIDO)
                        .cantidad(recibido)
                        .stockAnterior(stockAnterior)
                        .stockNuevo(nuevoStock)
                        .referencia("Recepción pedido #" + pedido.getFolio())
                        .usuario(usuario)
                        .build();
                movimientoStockRepository.save(ms);
            }
        }

        int totalPedidoCalc = pedido.getDetalles().stream().mapToInt(PedidoDetalle::getCantidadPedida).sum();
        int totalRecibidoCalc = pedido.getDetalles().stream()
                .mapToInt(d -> d.getCantidadRecibida() != null ? d.getCantidadRecibida() : 0).sum();

        if (totalRecibidoCalc >= totalPedidoCalc) {
            pedido.setEstado(EstadoPedido.COMPLETADO);
        } else if (totalRecibidoCalc > 0) {
            pedido.setEstado(EstadoPedido.PARCIAL);
        }

        pedido = pedidoRepository.save(pedido);
        return toResponse(pedido);
    }

    @Transactional
    @Override
    public PedidoResponse completar(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado con id: " + id));

        if (pedido.getEstado() != EstadoPedido.PARCIAL) {
            throw new InvalidEntryException("Solo se pueden completar pedidos en estado PARCIAL");
        }

        pedido.setEstado(EstadoPedido.COMPLETADO);
        pedido = pedidoRepository.save(pedido);
        return toResponse(pedido);
    }

    public static double getDefaultMarkupPercentage() {
        return DEFAULT_MARKUP_PERCENTAGE;
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

    private String generarFolio() {
        String prefix = "PED-";
        int num = 1;
        while (pedidoRepository.existsByFolio(prefix + String.format("%06d", num))) {
            num++;
        }
        return prefix + String.format("%06d", num);
    }

    private Persona obtenerPersonaActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return personaRepository.findByUsuario(auth.getName()).orElse(null);
        }
        return null;
    }

    private PedidoResponse toResponse(Pedido p) {
        List<PedidoResponse.PedidoDetalleResponse> detalles = p.getDetalles().stream()
                .map(d -> new PedidoResponse.PedidoDetalleResponse(
                        d.getIdPedidoDetalle(),
                        d.getProducto().getIdProducto(),
                        d.getProducto().getSku(),
                        d.getProducto().getNombre(),
                        d.getCantidadPedida(),
                        d.getPrecioCompraUnitario(),
                        d.getCantidadRecibida(),
                        d.getCostoUltimo(),
                        d.getCantidadPedida() * d.getPrecioCompraUnitario()))
                .collect(Collectors.toList());

        return new PedidoResponse(
                p.getIdPedido(), p.getFolio(),
                p.getProveedor().getIdProveedor(),
                p.getProveedor().getNombre(),
                p.getProveedor().getRfc(),
                p.getSucursal().getIdSucursal(),
                p.getSucursal().getNombre(),
                p.getPersona() != null ? p.getPersona().getIdPersona() : null,
                p.getPersona() != null ? p.getPersona().getNombre() : null,
                p.getEstado().name(),
                p.getNota(),
                detalles,
                p.getFechaCreacion(),
                p.getFechaActualizacion());
    }
}
