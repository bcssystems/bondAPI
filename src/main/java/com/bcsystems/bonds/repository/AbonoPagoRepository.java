package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.AbonoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AbonoPagoRepository extends JpaRepository<AbonoPago, Integer> {
    List<AbonoPago> findByAbonoIdAbono(Integer idAbono);

    @Query("SELECT ap FROM AbonoPago ap WHERE ap.abono.caja.idCaja = :idCaja AND ap.abono.fecha BETWEEN :inicio AND :fin")
    List<AbonoPago> findByCajaAndFechaRange(@Param("idCaja") Integer idCaja,
                                             @Param("inicio") LocalDateTime inicio,
                                             @Param("fin") LocalDateTime fin);
}