package com.bcsystems.bonds.domain;

import com.bcsystems.bonds.domain.en.EstadoCredito;
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
@Table(name = "credito")
public class Credito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCredito;

    @Column(unique = true, length = 20)
    private String folio;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false, unique = true)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private Double montoOriginal;

    @Column(nullable = false)
    private Double saldoPendiente;

    @Column(nullable = false)
    private Integer plazoMeses;

    private Double porcentajeInteres;

    @Column(nullable = false)
    private LocalDateTime fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCredito estado;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Persona usuario;
}
