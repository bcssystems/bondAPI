package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.*;
import com.bcsystems.bonds.domain.en.TipoMovimiento;
import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.*;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.RecepcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecepcionServiceImpl implements RecepcionService {

    private final RecepcionRepository recepcionRepository;
    private final RecepcionDetalleRepository recepcionDetalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final SucursalRepository sucursalRepository;
    private final PersonaRepository personaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final AuditoriaService auditoriaService;

    @Override
    public List<RecepcionResponse> listar(String search, Integer idSucursal) {
        return recepcionRepository.buscar(search, idSucursal).stream()
                .map(r -> toResponse(r, recepcionDetalleRepository.findByRecepcionIdRecepcion(r.getIdRecepcion())))
                .toList();
    }

    @Override
    public List<RecepcionResponse> listarTodos() {
        return recepcionRepository.findAll().stream()
                .sorted(Comparator.comparing(Recepcion::getFechaRecepcion).reversed())
                .map(r -> toResponse(r, recepcionDetalleRepository.findByRecepcionIdRecepcion(r.getIdRecepcion())))
                .toList();
    }

    @Override
    public RecepcionResponse obtenerPorId(Integer id) {
        Recepcion recepcion = buscarOExcepcion(id);
        return toResponse(recepcion, recepcionDetalleRepository.findByRecepcionIdRecepcion(id));
    }

    @Override
    @Transactional
    public RecepcionResponse crear(RecepcionRequest request) {
        Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        Proveedor proveedor = request.idProveedor() != null
                ? proveedorRepository.findById(request.idProveedor()).orElse(null) : null;
        Persona persona = obtenerPersonaActual();

        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new InvalidEntryException("La recepcion debe tener al menos un detalle");
        }

        Recepcion recepcion = Recepcion.builder()
                .folio(generarFolio())
                .proveedor(proveedor)
                .sucursal(sucursal)
                .usuario(persona)
                .totalMetros(0.0)
                .totalRollos(0)
                .nota(request.nota())
                .detalles(new ArrayList<>())
                .build();

        double totalMetros = 0.0;
        int totalRollos = 0;
        String usuarioNombre = persona.getNombre() + (persona.getApellido() != null ? " " + persona.getApellido() : "");

        for (RecepcionDetalleRequest dr : request.detalles()) {
            Producto producto = productoRepository.findById(dr.idProducto())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + dr.idProducto()));
            if (dr.metros() == null || dr.metros() <= 0) {
                throw new InvalidEntryException("Los metros deben ser mayores a cero para: " + producto.getNombre());
            }
            if (dr.precioCompra() == null || dr.precioCompra() < 0) {
                throw new InvalidEntryException("El precio de compra no puede ser negativo para: " + producto.getNombre());
            }

            double metrosPorRollo = producto.getMetrosPorRollo() != null && producto.getMetrosPorRollo() > 0
                    ? producto.getMetrosPorRollo() : 1.0;
            int rollos = (int) Math.ceil(dr.metros() / metrosPorRollo);

            RecepcionDetalle detalle = RecepcionDetalle.builder()
                    .recepcion(recepcion)
                    .producto(producto)
                    .metros(dr.metros())
                    .rollos(rollos)
                    .precioCompra(dr.precioCompra())
                    .subtotal(dr.metros() * dr.precioCompra())
                    .build();
            recepcion.getDetalles().add(detalle);

            totalMetros += dr.metros();
            totalRollos += rollos;

            Integer stockAnterior = producto.getStockActual() != null ? producto.getStockActual() : 0;
            Integer stockNuevo = stockAnterior + dr.metros();
            producto.setStockActual(stockNuevo);

            Double costoActual = producto.getCostoPromedio() != null ? producto.getCostoPromedio() : 0.0;
            if (stockNuevo > 0) {
                double nuevoCosto = ((costoActual * stockAnterior) + (dr.precioCompra() * dr.metros())) / stockNuevo;
                producto.setCostoPromedio(nuevoCosto);
            }
            productoRepository.save(producto);

            InventarioSucursal inv = inventarioSucursalRepository
                    .findByProductoIdProductoAndSucursalIdSucursal(producto.getIdProducto(), sucursal.getIdSucursal())
                    .orElse(null);
            if (inv != null) {
                inv.setStock(inv.getStock() + dr.metros());
                inventarioSucursalRepository.save(inv);
            }

            MovimientoStock ms = MovimientoStock.builder()
                    .producto(producto)
                    .sucursal(sucursal)
                    .tipoMovimiento(TipoMovimiento.RECEPCION)
                    .cantidad(dr.metros())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(stockNuevo)
                    .referencia("Recepcion " + recepcion.getFolio())
                    .usuario(usuarioNombre)
                    .build();
            movimientoStockRepository.save(ms);
        }

        recepcion.setTotalMetros(totalMetros);
        recepcion.setTotalRollos(totalRollos);
        recepcion = recepcionRepository.save(recepcion);

        auditoriaService.registrar("Recepcion", recepcion.getIdRecepcion(),
                "CREACION", persona.getUsuario(),
                "Recepcion " + recepcion.getFolio() + " - " + totalMetros + " m");

        return toResponse(recepcion, recepcionDetalleRepository.findByRecepcionIdRecepcion(recepcion.getIdRecepcion()));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Recepcion recepcion = buscarOExcepcion(id);
        Sucursal sucursal = recepcion.getSucursal();
        List<RecepcionDetalle> detalles = recepcionDetalleRepository.findByRecepcionIdRecepcion(id);

        for (RecepcionDetalle d : detalles) {
            Producto p = d.getProducto();
            p.setStockActual(Math.max(0, p.getStockActual() - d.getMetros()));
            productoRepository.save(p);
            InventarioSucursal inv = inventarioSucursalRepository
                    .findByProductoIdProductoAndSucursalIdSucursal(p.getIdProducto(), sucursal.getIdSucursal())
                    .orElse(null);
            if (inv != null) {
                inv.setStock(Math.max(0, inv.getStock() - d.getMetros()));
                inventarioSucursalRepository.save(inv);
            }
        }

        recepcionDetalleRepository.deleteAll(detalles);
        recepcionRepository.delete(recepcion);

        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrar("Recepcion", id, "ELIMINACION", usuario, "Recepcion " + recepcion.getFolio() + " eliminada");
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRecepciones", recepcionRepository.count());
        result.put("totalMetros", recepcionRepository.sumTotalMetros());
        result.put("totalRollos", recepcionRepository.sumTotalRollos());

        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        List<Recepcion> recientes = recepcionRepository.buscar(null, null).stream()
                .filter(r -> r.getFechaRecepcion() != null && !r.getFechaRecepcion().isBefore(desde))
                .toList();
        result.put("recepciones30Dias", recientes.size());
        result.put("metros30Dias", recientes.stream().mapToDouble(Recepcion::getTotalMetros).sum());
        return result;
    }

    private Recepcion buscarOExcepcion(Integer id) {
        return recepcionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recepcion no encontrada con id: " + id));
    }

    private String generarFolio() {
        String prefix = "RE-";
        int num = 1;
        while (recepcionRepository.existsByFolio(prefix + String.format("%06d", num))) {
            num++;
        }
        return prefix + String.format("%06d", num);
    }

    private Persona obtenerPersonaActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return personaRepository.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private RecepcionResponse toResponse(Recepcion r, List<RecepcionDetalle> detalles) {
        List<RecepcionDetalleResponse> detalleResponses = detalles.stream()
                .map(d -> new RecepcionDetalleResponse(
                        d.getIdRecepcionDetalle(),
                        d.getProducto().getIdProducto(),
                        d.getProducto().getNombre(),
                        d.getProducto().getSku(),
                        d.getMetros(), d.getRollos(),
                        d.getPrecioCompra(), d.getSubtotal()))
                .toList();

        return new RecepcionResponse(
                r.getIdRecepcion(), r.getFolio(),
                r.getProveedor() != null ? r.getProveedor().getIdProveedor() : null,
                r.getProveedor() != null ? r.getProveedor().getNombre() : null,
                r.getProveedor() != null ? r.getProveedor().getRfc() : null,
                r.getSucursal().getIdSucursal(), r.getSucursal().getNombre(),
                r.getUsuario() != null ? r.getUsuario().getIdPersona() : null,
                r.getUsuario() != null ? r.getUsuario().getUsuario() : null,
                r.getTotalMetros(), r.getTotalRollos(),
                r.getNota(), r.getFechaRecepcion(), detalleResponses);
    }
}