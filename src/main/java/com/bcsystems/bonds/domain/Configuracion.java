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
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConfiguracion;

    @Column(nullable = false, unique = true, length = 50)
    private String clave;

    @Column(nullable = false, length = 500)
    private String valor;

    @Column(length = 200)
    private String descripcion;

    private LocalDateTime actualizadaEn;
}