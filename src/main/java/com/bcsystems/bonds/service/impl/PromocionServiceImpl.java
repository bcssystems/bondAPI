package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Promocion;
import com.bcsystems.bonds.domain.PromocionDetalle;
import com.bcsystems.bonds.domain.en.TipoPromocion;
import com.bcsystems.bonds.dto.PromocionRequest;
import com.bcsystems.bonds.dto.PromocionResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.ProductoRepository;
import com.bcsystems.bonds.repository.PromocionDetalleRepository;
import com.bcsystems.bonds.repository.PromocionRepository;
import com.bcsystems.bonds.service.PromocionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromocionServiceImpl implements PromocionService {

    private final PromocionRepository promocionRepository;
    private final PromocionDetalleRepository promocionDetalleRepository;
    private final ProductoRepository productoRepository;

    public PromocionServiceImpl(PromocionRepository promocionRepository,
                                PromocionDetalleRepository promocionDetalleRepository,
                                ProductoRepository productoRepository) {
        this.promocionRepository = promocionRepository;
        this.promocionDetalleRepository = promocionDetalleRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromocionResponse> listar(TipoPromocion tipo, Boolean activo, Pageable pageable) {
        return promocionRepository.listarConFiltros(tipo, activo, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Override
    @Transactional
    public PromocionResponse crear(PromocionRequest request) {
        validarRequest(request);

        Promocion promocion = Promocion.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .tipo(request.tipo())
                .descuentoPorcentaje(request.descuentoPorcentaje())
                .activo(request.activo())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .build();

        if (request.tipo() == TipoPromocion.PROMOCION) {
            validateProductoExists(request.idProducto());
            promocion.setIdProducto(request.idProducto());
        }

        calcularPrecios(promocion, request);
        Promocion saved = promocionRepository.save(promocion);

        if (request.tipo() == TipoPromocion.COMBO && request.detalles() != null) {
            List<PromocionDetalle> detalles = request.detalles().stream()
                    .map(d -> PromocionDetalle.builder()
                            .promocion(saved)
                            .producto(productoRepository.findById(d.idProducto())
                                    .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + d.idProducto())))
                            .cantidad(d.cantidad() != null ? d.cantidad() : 1)
                            .build())
                    .collect(Collectors.toList());
            promocionDetalleRepository.saveAll(detalles);
            saved.setDetalles(detalles);
        }

        promocion = saved;

        return toResponse(promocion);
    }

    @Override
    @Transactional
    public PromocionResponse actualizar(Integer id, PromocionRequest request) {
        Promocion promocion = buscarOExcepcion(id);

        promocion.setNombre(request.nombre());
        promocion.setDescripcion(request.descripcion());
        promocion.setTipo(request.tipo());
        promocion.setDescuentoPorcentaje(request.descuentoPorcentaje());
        promocion.setActivo(request.activo());
        promocion.setFechaInicio(request.fechaInicio());
        promocion.setFechaFin(request.fechaFin());

        if (request.tipo() == TipoPromocion.PROMOCION) {
            validateProductoExists(request.idProducto());
            promocion.setIdProducto(request.idProducto());
        } else {
            promocion.setIdProducto(null);
        }

        calcularPrecios(promocion, request);

        if (request.tipo() == TipoPromocion.COMBO) {
            promocion.getDetalles().clear();
            if (request.detalles() != null) {
                Promocion finalPromocion = promocion;
                List<PromocionDetalle> detalles = request.detalles().stream()
                        .map(d -> PromocionDetalle.builder()
                                .promocion(finalPromocion)
                                .producto(productoRepository.findById(d.idProducto())
                                        .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + d.idProducto())))
                                .cantidad(d.cantidad() != null ? d.cantidad() : 1)
                                .build())
                        .collect(Collectors.toList());
                promocion.getDetalles().addAll(detalles);
            }
        } else {
            promocion.getDetalles().clear();
        }

        Promocion saved = promocionRepository.save(promocion);
        promocion = saved;

        return toResponse(promocion);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Promocion promocion = buscarOExcepcion(id);
        promocion.setActivo(false);
        promocionRepository.save(promocion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponse> listarActivas() {
        return promocionRepository.findActivas(LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Promocion buscarOExcepcion(Integer id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promocion no encontrada con id: " + id));
    }

    private void validarRequest(PromocionRequest request) {
        if (request.descuentoPorcentaje() < 0 || request.descuentoPorcentaje() > 100) {
            throw new InvalidEntryException("El descuento debe estar entre 0 y 100");
        }
        if (request.tipo() == TipoPromocion.PROMOCION && request.idProducto() == null) {
            throw new InvalidEntryException("Para promocion se requiere un producto");
        }
        if (request.tipo() == TipoPromocion.COMBO && (request.detalles() == null || request.detalles().isEmpty())) {
            throw new InvalidEntryException("Para combo se requiere al menos un producto");
        }
        if (request.fechaInicio() == null) {
            throw new InvalidEntryException("La fecha de inicio es obligatoria");
        }
        if (request.fechaFin() != null && request.fechaFin().isBefore(request.fechaInicio())) {
            throw new InvalidEntryException("La fecha fin debe ser posterior a la fecha inicio");
        }
    }

    private void validateProductoExists(Integer idProducto) {
        if (!productoRepository.existsById(idProducto)) {
            throw new NotFoundException("Producto no encontrado con id: " + idProducto);
        }
    }

    private void calcularPrecios(Promocion promocion, PromocionRequest request) {
        Double desc = request.descuentoPorcentaje();
        Double sugerido;

        if (request.tipo() == TipoPromocion.PROMOCION && request.idProducto() != null) {
            var producto = productoRepository.findById(request.idProducto())
                    .orElse(null);
            if (producto != null) {
                Double basePrice = producto.getPrecio1() != null ? producto.getPrecio1() : 0;
                sugerido = basePrice * (1 - desc / 100);
            } else {
                sugerido = 0.0;
            }
        } else if (request.tipo() == TipoPromocion.COMBO && request.detalles() != null) {
            sugerido = request.detalles().stream()
                    .mapToDouble(d -> {
                        var prod = productoRepository.findById(d.idProducto()).orElse(null);
                        double base = (prod != null && prod.getPrecio1() != null) ? prod.getPrecio1() : 0;
                        return base * (d.cantidad() != null ? d.cantidad() : 1);
                    })
                    .sum() * (1 - desc / 100);
        } else {
            sugerido = 0.0;
        }

        if (request.precioSugerido() != null) {
            promocion.setPrecioSugerido(request.precioSugerido());
        } else {
            promocion.setPrecioSugerido(sugerido);
        }

        if (request.precioFinal() != null) {
            promocion.setPrecioFinal(request.precioFinal());
        } else {
            promocion.setPrecioFinal(promocion.getPrecioSugerido());
        }
    }

    private PromocionResponse toResponse(Promocion p) {
        String productoSku = null;
        String productoNombre = null;
        if (p.getIdProducto() != null) {
            var prod = productoRepository.findById(p.getIdProducto()).orElse(null);
            if (prod != null) {
                productoSku = prod.getSku();
                productoNombre = prod.getNombre();
            }
        }

        List<PromocionResponse.PromocionDetalleResponse> detalles = null;
        if (p.getDetalles() != null && !p.getDetalles().isEmpty()) {
            detalles = p.getDetalles().stream()
                    .map(d -> {
                        var prod = d.getProducto();
                        return new PromocionResponse.PromocionDetalleResponse(
                                d.getIdPromocionDetalle(),
                                prod.getIdProducto(),
                                prod.getSku(),
                                prod.getNombre(),
                                d.getCantidad()
                        );
                    })
                    .collect(Collectors.toList());
        }

        return new PromocionResponse(
                p.getIdPromocion(),
                p.getNombre(),
                p.getDescripcion(),
                p.getTipo(),
                p.getDescuentoPorcentaje(),
                p.getPrecioSugerido(),
                p.getPrecioFinal(),
                p.getIdProducto(),
                productoSku,
                productoNombre,
                p.getActivo(),
                p.getFechaInicio(),
                p.getFechaFin(),
                p.getFechaCreacion(),
                detalles
        );
    }
}
