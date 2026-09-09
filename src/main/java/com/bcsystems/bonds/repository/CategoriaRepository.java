package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    @Query("SELECT c FROM Categoria c WHERE c.activo = true ORDER BY c.nombre ASC")
    List<Categoria> findAllActivas();

    Page<Categoria> findByActivo(Boolean activo, Pageable pageable);

    @Query("SELECT c FROM Categoria c WHERE (:search IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY c.nombre ASC")
    Page<Categoria> buscar(String search, Pageable pageable);
}
