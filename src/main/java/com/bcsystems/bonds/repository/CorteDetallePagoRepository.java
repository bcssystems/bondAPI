package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.CorteDetallePago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorteDetallePagoRepository extends JpaRepository<CorteDetallePago, Integer> {

    List<CorteDetallePago> findByCorteIdCorte(Integer idCorte);
}
