package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "abono_pago")
public class AbonoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAbonoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_abono", nullable = false)
    private Abono abono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_pago", nullable = false)
    private TipoPago tipoPago;

    @Column(nullable = false)
    private Double monto;

    @Column(length = 100)
    private String referencia;
}