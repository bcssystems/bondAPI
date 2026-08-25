package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Cliente;
import com.bcsystems.bonds.dto.ClienteRequest;
import com.bcsystems.bonds.dto.ClienteResponse;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.ClienteRepository;
import com.bcsystems.bonds.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public Page<ClienteResponse> listar(String search, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nombre"));
        Page<Cliente> clientes;
        if (search != null && !search.isBlank()) {
            clientes = clienteRepository.buscar(search, pageable);
        } else {
            clientes = clienteRepository.findActivos(pageable);
        }
        return clientes.map(this::toResponse);
    }

    @Override
    public Page<ClienteResponse> listarCreditClients(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nombre"));
        return clienteRepository.findCreditClients(pageable).map(this::toResponse);
    }

    @Override
    public ClienteResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        Cliente cliente = Cliente.builder()
                .nombre(request.nombre())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .telefono(request.telefono())
                .codigoPais(request.codigoPais() != null ? request.codigoPais() : "+52")
                .whatsapp(request.whatsapp())
                .empresa(request.empresa())
                .regimenFiscal(request.regimenFiscal())
                .cp(request.cp())
                .direccion(request.direccion())
                .tieneCredito(request.tieneCredito() != null && request.tieneCredito())
                .limiteCredito(request.limiteCredito() != null ? request.limiteCredito() : 0)
                .saldoActual(0.0)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
        cliente = clienteRepository.save(cliente);
        return toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscarOExcepcion(id);
        cliente.setNombre(request.nombre());
        cliente.setApellidoPaterno(request.apellidoPaterno());
        cliente.setApellidoMaterno(request.apellidoMaterno());
        cliente.setTelefono(request.telefono());
        cliente.setCodigoPais(request.codigoPais() != null ? request.codigoPais() : "+52");
        cliente.setWhatsapp(request.whatsapp());
        cliente.setEmpresa(request.empresa());
        cliente.setRegimenFiscal(request.regimenFiscal());
        cliente.setCp(request.cp());
        cliente.setDireccion(request.direccion());
        cliente.setTieneCredito(request.tieneCredito() != null && request.tieneCredito());
        cliente.setLimiteCredito(request.limiteCredito() != null ? request.limiteCredito() : 0);
        cliente = clienteRepository.save(cliente);
        return toResponse(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Cliente cliente = buscarOExcepcion(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    private Cliente buscarOExcepcion(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado con id: " + id));
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getIdCliente(), c.getNombre(),
                c.getApellidoPaterno(), c.getApellidoMaterno(),
                c.getTelefono(), c.getCodigoPais(),
                c.getWhatsapp(), c.getEmpresa(),
                c.getRegimenFiscal(), c.getCp(), c.getDireccion(),
                c.getActivo(), c.getFechaRegistro(),
                c.getTieneCredito(), c.getLimiteCredito(), c.getSaldoActual());
    }
}
