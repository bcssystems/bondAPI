package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.TipoPago;
import com.bcsystems.bonds.domain.en.AccionAuditoria;
import com.bcsystems.bonds.dto.TipoPagoRequest;
import com.bcsystems.bonds.dto.TipoPagoResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.TipoPagoRepository;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.TipoPagoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoPagoServiceImpl implements TipoPagoService {

    private final TipoPagoRepository tipoPagoRepository;
    private final AuditoriaService auditoriaService;

    public TipoPagoServiceImpl(TipoPagoRepository tipoPagoRepository, AuditoriaService auditoriaService) {
        this.tipoPagoRepository = tipoPagoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public List<TipoPagoResponse> listarTodos() {
        return tipoPagoRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TipoPagoResponse obtenerPorId(Integer id) {
        return toResponse(tipoPagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de pago no encontrado con id: " + id)));
    }

    @Override
    @Transactional
    public TipoPagoResponse crear(TipoPagoRequest request) {
        if (tipoPagoRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new InvalidEntryException("Ya existe un tipo de pago con ese nombre");
        }

        TipoPago tipoPago = TipoPago.builder()
                .nombre(request.nombre())
                .activo(true)
                .build();

        tipoPago = tipoPagoRepository.save(tipoPago);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("TIPO_PAGO", tipoPago.getIdTipoPago(), AccionAuditoria.CREACION.name(), usuario,
                "Se creó el tipo de pago: " + tipoPago.getNombre());

        return toResponse(tipoPago);
    }

    @Override
    @Transactional
    public TipoPagoResponse actualizar(Integer id, TipoPagoRequest request) {
        TipoPago tipoPago = tipoPagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de pago no encontrado con id: " + id));

        if (tipoPagoRepository.existsByNombreIgnoreCaseAndIdTipoPagoNot(request.nombre(), id)) {
            throw new InvalidEntryException("Ya existe otro tipo de pago con ese nombre");
        }

        tipoPago.setNombre(request.nombre());
        tipoPagoRepository.save(tipoPago);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("TIPO_PAGO", id, AccionAuditoria.ACTUALIZACION.name(), usuario,
                "Se actualizó el tipo de pago: " + tipoPago.getNombre());

        return toResponse(tipoPago);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoPago tipoPago = tipoPagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de pago no encontrado con id: " + id));
        tipoPago.setActivo(false);
        tipoPagoRepository.save(tipoPago);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("TIPO_PAGO", id, AccionAuditoria.ELIMINACION.name(), usuario,
                "Se eliminó el tipo de pago: " + tipoPago.getNombre());
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private TipoPagoResponse toResponse(TipoPago t) {
        return new TipoPagoResponse(t.getIdTipoPago(), t.getNombre(), t.getActivo());
    }
}
