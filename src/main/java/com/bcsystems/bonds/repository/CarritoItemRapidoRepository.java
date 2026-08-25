package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.CarritoItemRapido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CarritoItemRapidoRepository extends JpaRepository<CarritoItemRapido, Integer> {

    List<CarritoItemRapido> findByCajaIdCaja(Integer idCaja);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM carrito_item_rapido WHERE id_caja = :idCaja", nativeQuery = true)
    void deleteByCajaIdCajaNative(@Param("idCaja") Integer idCaja);
}
