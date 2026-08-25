package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.*;
import com.bcsystems.bonds.domain.en.EstadoCotizacion;
import com.bcsystems.bonds.dto.CotizacionDetalleResponse;
import com.bcsystems.bonds.dto.CotizacionRequest;
import com.bcsystems.bonds.dto.CotizacionResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.*;
import com.bcsystems.bonds.service.CotizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl implements CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PersonaRepository personaRepository;

    @Override
    @Transactional
    public CotizacionResponse crear(CotizacionRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        Persona usuario = obtenerPersonaActual();

        Double totalProductos = 0.0;
        for (CotizacionRequest.Detalle d : request.detalles()) {
            totalProductos += d.precioUnitario() * d.cantidad();
        }

        Double montoEnvio = (request.cobraEnvio() != null && request.cobraEnvio())
                ? (request.montoEnvio() != null ? request.montoEnvio() : 0.0) : 0.0;
        Boolean cobraEnvio = request.cobraEnvio() != null ? request.cobraEnvio() : false;
        Integer precioSeleccionado = request.precioSeleccionado() != null ? request.precioSeleccionado() : 1;

        Double total = totalProductos + montoEnvio;

        Cotizacion cotizacion = Cotizacion.builder()
                .cliente(cliente)
                .usuario(usuario)
                .paqueteria(request.paqueteria())
                .cobraEnvio(cobraEnvio)
                .montoEnvio(montoEnvio)
                .precioSeleccionado(precioSeleccionado)
                .diasVigencia(request.diasVigencia())
                .total(total)
                .tipoVenta(request.tipoVenta() != null ? request.tipoVenta() : "CONTADO")
                .plazoMeses(request.plazoMeses())
                .porcentajeInteres(request.porcentajeInteres() != null ? request.porcentajeInteres() : 0.0)
                .estado(EstadoCotizacion.VIGENTE)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusDays(request.diasVigencia()))
                .build();

        for (CotizacionRequest.Detalle d : request.detalles()) {
            Producto producto = productoRepository.findById(d.idProducto())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + d.idProducto()));

            Double subtotal = d.precioUnitario() * d.cantidad();
            CotizacionDetalle detalle = CotizacionDetalle.builder()
                    .cotizacion(cotizacion)
                    .producto(producto)
                    .cantidad(d.cantidad())
                    .precioUnitario(d.precioUnitario())
                    .subtotal(subtotal)
                    .build();
            cotizacion.getDetalles().add(detalle);
        }

        cotizacion = cotizacionRepository.save(cotizacion);
        return toResponse(cotizacion);
    }

    @Override
    public CotizacionResponse obtenerPorId(Integer id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cotización no encontrada"));
        return toResponse(cotizacion);
    }

    @Override
    public List<CotizacionResponse> listarTodas() {
        return cotizacionRepository.findAllByOrderByFechaCreacionDesc().stream()
                .map(this::toResponse).toList();
    }

    @Override
    public List<CotizacionResponse> listarPorEstado(String estado) {
        EstadoCotizacion e = EstadoCotizacion.valueOf(estado);
        return cotizacionRepository.findByEstadoOrderByFechaCreacionDesc(e).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CotizacionResponse cancelar(Integer id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cotización no encontrada"));
        if (cotizacion.getEstado() != EstadoCotizacion.VIGENTE) {
            throw new InvalidEntryException("Solo se pueden cancelar cotizaciones vigentes");
        }
        cotizacion.setEstado(EstadoCotizacion.CANCELADA);
        cotizacion = cotizacionRepository.save(cotizacion);
        return toResponse(cotizacion);
    }

    @Override
    @Transactional
    public CotizacionResponse convertirAVenta(Integer id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cotización no encontrada"));
        if (cotizacion.getEstado() != EstadoCotizacion.VIGENTE) {
            throw new InvalidEntryException("Solo se pueden convertir cotizaciones vigentes");
        }
        if (LocalDateTime.now().isAfter(cotizacion.getFechaExpiracion())) {
            cotizacion.setEstado(EstadoCotizacion.EXPIRADA);
            cotizacionRepository.save(cotizacion);
            throw new InvalidEntryException("La cotización ha expirado");
        }
        cotizacion.setEstado(EstadoCotizacion.CONVERTIDA);
        cotizacion = cotizacionRepository.save(cotizacion);
        return toResponse(cotizacion);
    }

    private Persona obtenerPersonaActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return personaRepository.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private CotizacionResponse toResponse(Cotizacion c) {
        List<CotizacionDetalleResponse> detalles = c.getDetalles().stream()
                .map(d -> new CotizacionDetalleResponse(
                        d.getIdDetalle(),
                        d.getProducto().getIdProducto(),
                        d.getProducto().getNombre(),
                        d.getProducto().getSku(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                )).toList();

        return new CotizacionResponse(
                c.getIdCotizacion(),
                c.getCliente().getIdCliente(),
                c.getCliente().getNombre() + " " + c.getCliente().getApellidoPaterno(),
                c.getUsuario().getIdPersona(),
                c.getUsuario().getNombre() + " " + c.getUsuario().getApellido(),
                c.getPaqueteria(),
                c.getCobraEnvio(),
                c.getMontoEnvio(),
                c.getPrecioSeleccionado(),
                c.getDiasVigencia(),
                c.getTotal(),
                c.getEstado().name(),
                c.getFechaCreacion(),
                c.getFechaExpiracion(),
                c.getTipoVenta(),
                c.getPlazoMeses(),
                c.getPorcentajeInteres(),
                detalles
        );
    }
}
