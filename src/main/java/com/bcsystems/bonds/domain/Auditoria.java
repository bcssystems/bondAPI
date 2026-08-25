package com.bcsystems.bonds.domain;

import com.bcsystems.bonds.domain.en.AccionAuditoria;
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
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAuditoria;

    @Column(nullable = false, length = 50)
    private String entidad;

    private Integer entidadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccionAuditoria accion;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(length = 100)
    private String referencia;

    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
}
