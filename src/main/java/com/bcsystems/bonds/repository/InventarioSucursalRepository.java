package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.InventarioSucursal;
import com.bcsystems.bonds.dto.InventarioSucursalProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioSucursalRepository extends JpaRepository<InventarioSucursal, Integer> {
    List<InventarioSucursal> findByProductoIdProducto(Integer idProducto);
    Optional<InventarioSucursal> findByProductoIdProductoAndSucursalIdSucursal(Integer idProducto, Integer idSucursal);

    @Modifying
    @Query("DELETE FROM InventarioSucursal i WHERE i.producto.idProducto = :idProducto")
    void deleteByProductoIdProducto(@Param("idProducto") Integer idProducto);

    @Query("SELECT new com.bcsystems.bonds.dto.InventarioSucursalProjection(i.id, i.producto.idProducto, s.idSucursal, s.nombre, i.stock, i.stockMinimo, i.stockMaximo) " +
           "FROM InventarioSucursal i JOIN i.sucursal s " +
           "WHERE i.producto.idProducto IN :ids " +
           "AND (:idSucursal IS NULL OR s.idSucursal = :idSucursal)")
    List<InventarioSucursalProjection> findProyeccionPorIds(@Param("idSucursal") Integer idSucursal,
                                                            @Param("ids") java.util.Collection<Integer> ids);

    @Query("SELECT i.sucursal.nombre, COALESCE(SUM(COALESCE(i.producto.costoPromedio, 0) * i.stock), 0) FROM InventarioSucursal i GROUP BY i.sucursal.nombre")
    List<Object[]> sumCostoPorSucursal();
}
