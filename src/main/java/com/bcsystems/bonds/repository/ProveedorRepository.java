package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    List<Proveedor> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdProveedorNot(String nombre, Integer idProveedor);

    @Query("SELECT p FROM Proveedor p WHERE " +
           "(:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.rfc) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:activo IS NULL OR p.activo = :activo) " +
           "ORDER BY p.nombre ASC")
    Page<Proveedor> buscarConFiltros(@Param("search") String search,
                                     @Param("activo") Boolean activo,
                                     Pageable pageable);
}
