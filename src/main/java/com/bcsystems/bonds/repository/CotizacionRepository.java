package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Cotizacion;
import com.bcsystems.bonds.domain.en.EstadoCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Integer> {
    List<Cotizacion> findAllByOrderByFechaCreacionDesc();
    List<Cotizacion> findByEstadoOrderByFechaCreacionDesc(EstadoCotizacion estado);
    List<Cotizacion> findByClienteIdClienteOrderByFechaCreacionDesc(Integer idCliente);
}
