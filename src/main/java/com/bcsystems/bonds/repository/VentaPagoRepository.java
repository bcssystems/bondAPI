package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.VentaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaPagoRepository extends JpaRepository<VentaPago, Integer> {
    List<VentaPago> findByVentaIdVenta(Integer idVenta);

    @Query("SELECT vp FROM VentaPago vp WHERE vp.venta.caja.idCaja = :idCaja AND vp.venta.fecha BETWEEN :inicio AND :fin")
    List<VentaPago> findByCajaAndFechaRange(@Param("idCaja") Integer idCaja,
                                             @Param("inicio") LocalDateTime inicio,
                                             @Param("fin") LocalDateTime fin);
}
