package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "corte_detalle_pago")
public class CorteDetallePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCorteDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_corte", nullable = false)
    private CorteCaja corte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_pago", nullable = false)
    private TipoPago tipoPago;

    @Column(nullable = false)
    private Double monto;

    private Double montoReal;
}
