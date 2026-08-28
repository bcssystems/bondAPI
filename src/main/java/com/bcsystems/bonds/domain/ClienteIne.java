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
@Table(name = "cliente_ine")
public class ClienteIne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idClienteIne;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false, unique = true)
    private Cliente cliente;

    @Column(length = 500)
    private String urlFotoFrontal;

    @Column(length = 500)
    private String urlFotoTrasera;

    @Column(length = 200)
    private String nombreArchivoFrontal;

    @Column(length = 200)
    private String nombreArchivoTrasera;

    @Column(nullable = false)
    private LocalDateTime subidoEn;
}