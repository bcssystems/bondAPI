package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.RecepcionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecepcionDetalleRepository extends JpaRepository<RecepcionDetalle, Integer> {
    List<RecepcionDetalle> findByRecepcionIdRecepcion(Integer idRecepcion);

    List<RecepcionDetalle> findByRecepcionSucursalIdSucursal(Integer idSucursal);
}