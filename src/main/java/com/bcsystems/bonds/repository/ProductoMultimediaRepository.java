package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.ProductoMultimedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoMultimediaRepository extends JpaRepository<ProductoMultimedia, Integer> {
    List<ProductoMultimedia> findByProductoIdProductoOrderByFechaSubidaDesc(Integer idProducto);
    Optional<ProductoMultimedia> findByProductoIdProductoAndEsPrincipalTrue(Integer idProducto);
    void deleteByProductoIdProducto(Integer idProducto);
}
