package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recepcion")
public class Recepcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRecepcion;

    @Column(nullable = false, unique = true, length = 20)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Persona usuario;

    @Column(nullable = false)
    private Double totalMetros;

    @Column(nullable = false)
    private Integer totalRollos;

    @Column(length = 500)
    private String nota;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRecepcion;

    @Builder.Default
    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecepcionDetalle> detalles = new ArrayList<>();
}