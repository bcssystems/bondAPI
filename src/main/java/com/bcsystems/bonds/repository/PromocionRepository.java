package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Promocion;
import com.bcsystems.bonds.domain.en.TipoPromocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    List<Promocion> findByActivoTrueAndFechaInicioBeforeAndFechaFinAfterOrFechaFinIsNull(
            LocalDateTime now, LocalDateTime now2);

    List<Promocion> findByTipoAndActivoTrue(TipoPromocion tipo);

    @Query("SELECT p FROM Promocion p WHERE " +
           "(:tipo IS NULL OR p.tipo = :tipo) AND " +
           "(:activo IS NULL OR p.activo = :activo) " +
           "ORDER BY p.fechaCreacion DESC")
    Page<Promocion> listarConFiltros(@Param("tipo") TipoPromocion tipo,
                                     @Param("activo") Boolean activo,
                                     Pageable pageable);

    @Query("SELECT p FROM Promocion p WHERE p.activo = true AND p.fechaInicio <= :now AND " +
           "(p.fechaFin IS NULL OR p.fechaFin >= :now)")
    List<Promocion> findActivas(@Param("now") LocalDateTime now);
}
