package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "corte_caja")
public class CorteCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCorte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @Column(nullable = false)
    private Double saldoInicial;

    @Column(nullable = false)
    private Double totalVentas;

    @Column(nullable = false)
    private Double totalVentasContado;

    @Column(nullable = false)
    private Double totalVentasCredito;

    @Column(nullable = false)
    private Double totalIngresos;

    @Column(nullable = false)
    private Double totalEgresos;

    @Column(nullable = false)
    private Double saldoFinalContado;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    @Column(nullable = false)
    private LocalDateTime fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Persona usuario;
}
