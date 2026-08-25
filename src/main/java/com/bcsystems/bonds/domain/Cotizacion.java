package com.bcsystems.bonds.domain;

import com.bcsystems.bonds.domain.en.EstadoCotizacion;
import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "cotizacion")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCotizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Persona usuario;

    @Column(length = 100)
    private String paqueteria;

    @Column(nullable = false)
    private Boolean cobraEnvio = false;

    private Double montoEnvio = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer precioSeleccionado = 1;

    @Column(nullable = false)
    private Integer diasVigencia;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String tipoVenta = "CONTADO";

    private Integer plazoMeses;

    @Builder.Default
    private Double porcentajeInteres = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCotizacion estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaExpiracion;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CotizacionDetalle> detalles = new ArrayList<>();
}
