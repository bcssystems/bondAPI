package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoPagoRepository extends JpaRepository<TipoPago, Integer> {
    List<TipoPago> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdTipoPagoNot(String nombre, Integer idTipoPago);
}
