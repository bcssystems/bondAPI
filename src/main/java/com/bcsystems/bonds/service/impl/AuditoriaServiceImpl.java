package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Auditoria;
import com.bcsystems.bonds.domain.en.AccionAuditoria;
import com.bcsystems.bonds.dto.AuditoriaResponse;
import com.bcsystems.bonds.repository.AuditoriaRepository;
import com.bcsystems.bonds.service.AuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaServiceImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Page<AuditoriaResponse> listar(String entidad, String usuario, Pageable pageable) {
        return auditoriaRepository.buscarConFiltros(entidad, usuario, pageable)
                .map(a -> new AuditoriaResponse(
                        a.getIdAuditoria(), a.getEntidad(), a.getEntidadId(),
                        a.getAccion().name(), a.getUsuario(), a.getFecha(),
                        a.getDetalle(), a.getReferencia(), a.getCantidad(),
                        a.getStockAnterior(), a.getStockNuevo()));
    }

    @Transactional
    @Override
    public void registrar(String entidad, Integer entidadId, String accion, String usuario, String detalle) {
        Auditoria auditoria = Auditoria.builder()
                .entidad(entidad)
                .entidadId(entidadId)
                .accion(mapearAccion(accion))
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .detalle(detalle)
                .build();
        auditoriaRepository.save(auditoria);
    }

    @Transactional
    @Override
    public void registrarMovimiento(String entidad, Integer entidadId, String accion, String usuario,
                                     String detalle, String referencia, Integer cantidad,
                                     Integer stockAnterior, Integer stockNuevo) {
        Auditoria auditoria = Auditoria.builder()
                .entidad(entidad)
                .entidadId(entidadId)
                .accion(mapearAccion(accion))
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .detalle(detalle)
                .referencia(referencia)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .build();
        auditoriaRepository.save(auditoria);
    }

    private AccionAuditoria mapearAccion(String accion) {
        try {
            return AccionAuditoria.valueOf(accion);
        } catch (IllegalArgumentException e) {
            return switch (accion) {
                case "ENTRADA" -> AccionAuditoria.ENTRADA_STOCK;
                case "SALIDA" -> AccionAuditoria.SALIDA_STOCK;
                case "AJUSTE" -> AccionAuditoria.AJUSTE_STOCK;
                case "TRANSFERENCIA" -> AccionAuditoria.TRANSFERENCIA_STOCK;
                case "CREAR" -> AccionAuditoria.CREACION;
                case "ACTUALIZAR" -> AccionAuditoria.ACTUALIZACION;
                case "ELIMINAR" -> AccionAuditoria.ELIMINACION;
                default -> throw e;
            };
        }
    }
}
