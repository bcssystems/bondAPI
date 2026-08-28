package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recepcion_detalle")
public class RecepcionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRecepcionDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recepcion", nullable = false)
    private Recepcion recepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer metros;

    @Column(nullable = false)
    private Integer rollos;

    @Column(nullable = false)
    private Double precioCompra;

    @Column(nullable = false)
    private Double subtotal;
}