package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "carrito_item_rapido")
public class CarritoItemRapido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idItemRapido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Double precioVenta;

    @Column
    private Double precioCompra;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private LocalDateTime fechaAgregado;
}
