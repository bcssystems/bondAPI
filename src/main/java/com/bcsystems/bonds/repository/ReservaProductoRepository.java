package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.ReservaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaProductoRepository extends JpaRepository<ReservaProducto, Integer> {

    List<ReservaProducto> findByCajaIdCaja(Integer idCaja);

    List<ReservaProducto> findByCajaSucursalIdSucursal(Integer idSucursal);

    List<ReservaProducto> findByExpiraEnBefore(LocalDateTime fecha);

    void deleteByCajaIdCaja(Integer idCaja);

    Optional<ReservaProducto> findByCajaIdCajaAndIdProducto(Integer idCaja, Integer idProducto);

    @Query("SELECT COALESCE(SUM(r.cantidad), 0) FROM ReservaProducto r " +
           "WHERE r.idProducto = :idProducto AND r.caja.sucursal.idSucursal = :idSucursal " +
           "AND r.expiraEn > :now")
    Integer sumCantidadReservada(@Param("idProducto") Integer idProducto,
                                 @Param("idSucursal") Integer idSucursal,
                                 @Param("now") LocalDateTime now);
}
