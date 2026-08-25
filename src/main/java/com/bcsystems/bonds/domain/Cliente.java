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
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(length = 100)
    private String apellidoMaterno;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 5)
    private String codigoPais;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 200)
    private String empresa;

    @Column(nullable = false, length = 50)
    private String regimenFiscal;

    @Column(length = 10)
    private String cp;

    @Column(length = 500)
    private String direccion;

    private Boolean tieneCredito;

    private Double limiteCredito;

    private Double saldoActual;

    @Column(nullable = false)
    private Boolean activo;

    @Column(updatable = false)
    private LocalDateTime fechaRegistro;
}
