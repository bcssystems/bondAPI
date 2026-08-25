package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.MovimientoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoCreditoRepository extends JpaRepository<MovimientoCredito, Integer> {

    List<MovimientoCredito> findByCreditoIdCreditoOrderByFechaDesc(Integer idCredito);

    List<MovimientoCredito> findByCreditoClienteIdClienteOrderByFechaDesc(Integer idCliente);
}
