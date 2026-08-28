package com.bcsystems.bonds.domain;

import com.bcsystems.bonds.domain.en.EstadoVenta;
import com.bcsystems.bonds.domain.en.TipoVenta;
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
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Persona usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoVenta tipoVenta;

    @Column(nullable = false)
    private Integer precioSeleccionado;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double descuento;

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoVenta estado;

    @Column(length = 500)
    private String nota;

    @Column(length = 500)
    private String motivoCancelacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante_cancelacion")
    private Persona solicitanteCancelacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autorizador_cancelacion")
    private Persona autorizadorCancelacion;

    private LocalDateTime fechaSolicitudCancelacion;

    private LocalDateTime fechaAutorizacionCancelacion;

    @Column(updatable = false)
    private LocalDateTime fecha;
}
