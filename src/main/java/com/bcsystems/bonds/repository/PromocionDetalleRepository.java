package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.PromocionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromocionDetalleRepository extends JpaRepository<PromocionDetalle, Integer> {
    void deleteByPromocionIdPromocion(Integer idPromocion);
}
