package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Pedido;
import com.bcsystems.bonds.domain.en.EstadoPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    boolean existsByFolio(String folio);

    Optional<Pedido> findByFolio(String folio);

    List<Pedido> findByEstadoOrderByFechaCreacionDesc(EstadoPedido estado);

    @Query("SELECT p FROM Pedido p WHERE " +
           "(:search IS NULL OR LOWER(p.folio) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "AND (:idProveedor IS NULL OR p.proveedor.idProveedor = :idProveedor) " +
           "ORDER BY p.fechaCreacion DESC")
    Page<Pedido> buscarConFiltros(@Param("search") String search,
                                  @Param("estado") EstadoPedido estado,
                                  @Param("idProveedor") Integer idProveedor,
                                  Pageable pageable);
}
