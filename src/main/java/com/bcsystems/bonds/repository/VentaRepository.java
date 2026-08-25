package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Venta;
import com.bcsystems.bonds.domain.en.EstadoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findByCajaIdCajaAndEstadoOrderByFechaDesc(Integer idCaja, EstadoVenta estado);
    List<Venta> findByCajaIdCajaAndEstadoAndFechaBetweenOrderByFechaDesc(
            Integer idCaja, EstadoVenta estado, LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByCajaIdCajaAndFechaBetweenOrderByFechaDesc(
            Integer idCaja, LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findByCajaSucursalIdSucursalAndEstadoOrderByFechaDesc(
            Integer idSucursal, EstadoVenta estado);

    @Query("""
        SELECT v FROM Venta v
        WHERE (:idSucursal IS NULL OR v.caja.sucursal.idSucursal = :idSucursal)
        AND (:idCaja IS NULL OR v.caja.idCaja = :idCaja)
        AND (:estado IS NULL OR v.estado = :estado)
        AND (:fechaInicio IS NULL OR v.fecha >= :fechaInicio)
        AND (:fechaFin IS NULL OR v.fecha <= :fechaFin)
        ORDER BY v.fecha DESC
    """)
    Page<Venta> listarConFiltros(
            @Param("idSucursal") Integer idSucursal,
            @Param("idCaja") Integer idCaja,
            @Param("estado") EstadoVenta estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);
}
