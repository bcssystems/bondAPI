package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Integer> {

    List<PedidoDetalle> findByPedidoIdPedido(Integer idPedido);
}
