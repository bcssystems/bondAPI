package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Abono;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbonoRepository extends JpaRepository<Abono, Integer> {

    List<Abono> findByCreditoIdCreditoOrderByFechaDesc(Integer idCredito);
}
