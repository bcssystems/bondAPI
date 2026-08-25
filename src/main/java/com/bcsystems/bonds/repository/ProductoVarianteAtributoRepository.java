package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.ProductoVarianteAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoVarianteAtributoRepository extends JpaRepository<ProductoVarianteAtributo, Integer> {
    List<ProductoVarianteAtributo> findByProductoVarianteIdProducto(Integer idProductoVariante);
    void deleteByProductoVarianteIdProducto(Integer idProductoVariante);
}
