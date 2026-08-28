package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Cliente;
import com.bcsystems.bonds.domain.PrecioCliente;
import com.bcsystems.bonds.domain.Producto;
import com.bcsystems.bonds.dto.PrecioClienteRequest;
import com.bcsystems.bonds.dto.PrecioClienteResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.ClienteRepository;
import com.bcsystems.bonds.repository.PrecioClienteRepository;
import com.bcsystems.bonds.repository.ProductoRepository;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.PrecioClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrecioClienteServiceImpl implements PrecioClienteService {

    private final PrecioClienteRepository precioClienteRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final AuditoriaService auditoriaService;

    @Override
    public List<PrecioClienteResponse> listarPorCliente(Integer idCliente) {
        return precioClienteRepository.findByClienteIdCliente(idCliente).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public List<PrecioClienteResponse> guardarPreciosCliente(Integer idCliente, List<PrecioClienteRequest> precios) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        List<PrecioCliente> nuevos = new ArrayList<>();
        double totalRedondeado = 0;
        for (PrecioClienteRequest req : precios) {
            if (req.precio() == null || req.precio() < 0) {
                totalRedondeado += 1; // only to avoid zero-length
                throw new InvalidEntryException("El precio no puede ser negativo");
            }
            Producto producto = productoRepository.findById(req.idProducto())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + req.idProducto()));
            nuevos.add(PrecioCliente.builder()
                    .cliente(cliente)
                    .producto(producto)
                    .precio(req.precio())
                    .actualizadoEn(LocalDateTime.now())
                    .build());
        }
        if (totalRedondeado == 0 && precios.isEmpty()) {
            // no precios, se limpia
        }

        precioClienteRepository.deleteByClienteIdCliente(idCliente);
        precioClienteRepository.saveAll(nuevos);

        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrar("PrecioCliente", idCliente, "ACTUALIZACION", usuario,
                "Precios especiales actualizados para " + cliente.getNombre() + " (" + nuevos.size() + " productos)");

        return precioClienteRepository.findByClienteIdCliente(idCliente).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void eliminarPrecio(Integer idPrecioCliente) {
        PrecioCliente precio = precioClienteRepository.findById(idPrecioCliente)
                .orElseThrow(() -> new NotFoundException("Precio especial no encontrado"));
        precioClienteRepository.delete(precio);
    }

    @Override
    public Double obtenerPrecioEspecial(Integer idCliente, Integer idProducto) {
        if (idCliente == null) return null;
        return precioClienteRepository
                .findByClienteIdClienteAndProductoIdProducto(idCliente, idProducto)
                .map(PrecioCliente::getPrecio)
                .orElse(null);
    }

    private PrecioClienteResponse toResponse(PrecioCliente pc) {
        return new PrecioClienteResponse(
                pc.getIdPrecioCliente(),
                pc.getCliente().getIdCliente(),
                pc.getCliente().getNombre() + " " + pc.getCliente().getApellidoPaterno(),
                pc.getProducto().getIdProducto(),
                pc.getProducto().getNombre(),
                pc.getProducto().getSku(),
                pc.getPrecio(), pc.getActualizadoEn());
    }
}