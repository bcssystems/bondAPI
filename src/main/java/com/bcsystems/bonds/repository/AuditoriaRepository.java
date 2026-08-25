package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    @Query("SELECT a FROM Auditoria a WHERE " +
           "(:entidad IS NULL OR a.entidad = :entidad) AND " +
           "(:usuario IS NULL OR LOWER(a.usuario) LIKE LOWER(CONCAT('%', :usuario, '%'))) " +
           "ORDER BY a.fecha DESC")
    Page<Auditoria> buscarConFiltros(@Param("entidad") String entidad,
                                      @Param("usuario") String usuario,
                                      Pageable pageable);

    @Query("SELECT a FROM Auditoria a WHERE " +
           "(:fechaInicio IS NULL OR a.fecha >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR a.fecha <= :fechaFin) " +
           "ORDER BY a.fecha DESC")
    List<Auditoria> buscarPorFechas(@Param("fechaInicio") LocalDateTime fechaInicio,
                                    @Param("fechaFin") LocalDateTime fechaFin);
}
