package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.MovimientoStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Integer> {
    @Query("SELECT m FROM MovimientoStock m WHERE " +
           "(:idProducto IS NULL OR m.producto.idProducto = :idProducto) AND " +
           "(:idSucursal IS NULL OR m.sucursal.idSucursal = :idSucursal) AND " +
           "(:fechaInicio IS NULL OR m.fechaMovimiento >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR m.fechaMovimiento <= :fechaFin) " +
           "ORDER BY m.fechaMovimiento DESC")
    Page<MovimientoStock> buscarConFiltros(@Param("idProducto") Integer idProducto,
                                            @Param("idSucursal") Integer idSucursal,
                                            @Param("fechaInicio") LocalDateTime fechaInicio,
                                            @Param("fechaFin") LocalDateTime fechaFin,
                                            Pageable pageable);

    @Query("SELECT m FROM MovimientoStock m WHERE " +
           "(:idSucursal IS NULL OR m.sucursal.idSucursal = :idSucursal) AND " +
           "(:fechaInicio IS NULL OR m.fechaMovimiento >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR m.fechaMovimiento <= :fechaFin) " +
           "ORDER BY m.fechaMovimiento DESC")
    List<MovimientoStock> buscarMovimientosPorFechas(@Param("idSucursal") Integer idSucursal,
                                                      @Param("fechaInicio") LocalDateTime fechaInicio,
                                                      @Param("fechaFin") LocalDateTime fechaFin);
}
