package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Integer> {
    List<MovimientoCaja> findByCajaIdCajaOrderByFechaDesc(Integer idCaja);
    List<MovimientoCaja> findByCajaIdCajaAndFechaBetweenOrderByFechaDesc(
            Integer idCaja, LocalDateTime inicio, LocalDateTime fin);
}
