package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Recepcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecepcionRepository extends JpaRepository<Recepcion, Integer> {

    boolean existsByFolio(String folio);

    Optional<Recepcion> findByFolio(String folio);

    @Query("SELECT r FROM Recepcion r WHERE " +
           "(:search IS NULL OR LOWER(r.folio) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR (r.proveedor IS NOT NULL AND LOWER(r.proveedor.nombre) LIKE LOWER(CONCAT('%', :search, '%')))) " +
           "AND (:idSucursal IS NULL OR r.sucursal.idSucursal = :idSucursal) " +
           "ORDER BY r.fechaRecepcion DESC")
    List<Recepcion> buscar(@Param("search") String search, @Param("idSucursal") Integer idSucursal);

    @Query("SELECT COALESCE(SUM(r.totalMetros), 0) FROM Recepcion r")
    double sumTotalMetros();

    @Query("SELECT COALESCE(SUM(r.totalRollos), 0) FROM Recepcion r")
    long sumTotalRollos();
}