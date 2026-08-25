package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.CorteCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CorteCajaRepository extends JpaRepository<CorteCaja, Integer> {
    Optional<CorteCaja> findTopByCajaIdCajaOrderByFechaCierreDesc(Integer idCaja);

    @Query("""
        SELECT c FROM CorteCaja c
        WHERE (:idSucursal IS NULL OR c.caja.sucursal.idSucursal = :idSucursal)
        AND (:idCaja IS NULL OR c.caja.idCaja = :idCaja)
        AND (:fechaInicio IS NULL OR c.fechaCierre >= :fechaInicio)
        AND (:fechaFin IS NULL OR c.fechaCierre <= :fechaFin)
        ORDER BY c.fechaCierre DESC
    """)
    Page<CorteCaja> listarConFiltros(
            @Param("idSucursal") Integer idSucursal,
            @Param("idCaja") Integer idCaja,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);
}
