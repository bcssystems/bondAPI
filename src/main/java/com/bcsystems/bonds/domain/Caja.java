package com.bcsystems.bonds.domain;

import com.bcsystems.bonds.domain.en.CajaEstado;
import com.bcsystems.bonds.domain.en.TipoCaja;
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
@Table(name = "caja")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCaja;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private TipoCaja tipo = TipoCaja.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CajaEstado estado;

    @Column(nullable = false)
    private Double saldoActual;

    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Column(nullable = false)
    private Boolean activa;
}
