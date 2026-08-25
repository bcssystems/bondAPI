package com.bcsystems.bonds.domain;

import com.bcsystems.bonds.domain.en.EstadoGasto;
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
@Table(name = "gasto")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idGasto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(nullable = false)
    private Double monto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Persona usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autorizador")
    private Persona autorizador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoGasto estado;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaAutorizacion;
}
