package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCaseAndIdProductoNot(String sku, Integer idProducto);
    long countByActivoTrue();

    @Query("SELECT COALESCE(SUM(p.stockActual), 0) FROM Producto p")
    Integer sumStockActual();

    @Query("SELECT COALESCE(SUM(p.stockMinimo), 0) FROM Producto p")
    Integer sumStockMinimo();

    @Query("SELECT COALESCE(SUM(COALESCE(p.costoPromedio, 0) * p.stockActual), 0) FROM Producto p WHERE p.activo = true")
    Double sumCostoTotalInventario();

    @Query("SELECT p FROM Producto p WHERE " +
           "(:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR EXISTS (SELECT pva FROM ProductoVarianteAtributo pva " +
           "WHERE pva.productoVariante.idProducto = p.idProducto " +
           "AND (LOWER(pva.valor.valor) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(pva.atributo.nombre) LIKE LOWER(CONCAT('%', :search, '%'))))) " +
           "AND (:activo IS NULL OR p.activo = :activo) " +
           "AND (:idSucursal IS NULL OR EXISTS (SELECT i FROM InventarioSucursal i WHERE i.producto.idProducto = p.idProducto AND i.sucursal.idSucursal = :idSucursal))")
    Page<Producto> buscarConFiltros(@Param("search") String search,
                                    @Param("activo") Boolean activo,
                                    @Param("idSucursal") Integer idSucursal,
                                    Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.activo = true " +
           "AND (p.tieneVariantes = false OR p.tieneVariantes IS NULL OR p.productoPadre IS NOT NULL) " +
           "AND (:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:idSucursal IS NULL OR EXISTS (SELECT i FROM InventarioSucursal i WHERE i.producto.idProducto = p.idProducto AND i.sucursal.idSucursal = :idSucursal))")
    Page<Producto> buscarParaVenta(@Param("search") String search,
                                   @Param("idSucursal") Integer idSucursal,
                                   Pageable pageable);

    List<Producto> findByProductoPadreIdProducto(Integer idProductoPadre);

    @Modifying
    @Query(value = "DELETE FROM producto WHERE id_producto_padre = :padreId", nativeQuery = true)
    void deleteVariantsByPadreIdNative(@Param("padreId") Integer padreId);

    @Modifying
    @Query(value = "DELETE FROM inventario_sucursal WHERE id_producto IN (SELECT id_producto FROM producto WHERE id_producto_padre = :padreId)", nativeQuery = true)
    void deleteInventariosByPadreId(@Param("padreId") Integer padreId);

    @Modifying
    @Query(value = "DELETE FROM producto_variante_atributo WHERE id_producto_variante IN (SELECT id_producto FROM producto WHERE id_producto_padre = :padreId)", nativeQuery = true)
    void deleteVarianteAtributosByPadreId(@Param("padreId") Integer padreId);

    @Modifying
    @Query(value = "DELETE FROM movimiento_stock WHERE id_producto IN (SELECT id_producto FROM producto WHERE id_producto_padre = :padreId)", nativeQuery = true)
    void deleteMovimientosByPadreId(@Param("padreId") Integer padreId);
}
