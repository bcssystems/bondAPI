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
@Table(name = "precio_cliente", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_cliente", "id_producto"})
})
public class PrecioCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPrecioCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private LocalDateTime actualizadoEn;
}