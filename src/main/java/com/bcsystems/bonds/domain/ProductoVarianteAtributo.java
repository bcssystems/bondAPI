package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "producto_variante_atributo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_producto_variante", "id_atributo"})
})
public class ProductoVarianteAtributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto_variante", nullable = false)
    private Producto productoVariante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_atributo", nullable = false)
    private Atributo atributo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_valor", nullable = false)
    private AtributoValor valor;
}
