package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double precio1;
    private Double precio2;
    private Double precio3;
    private Double precio4;

    @Builder.Default
    @Column(nullable = false)
    private Boolean precioPersonalizado = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer stockActual = 0;

    private Integer stockMinimo;
    private Integer stockMaximo;

    private Double costoPromedio;

    @Builder.Default
    @Column(nullable = false)
    private Boolean tieneVariantes = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto_padre")
    private Producto productoPadre;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductoMultimedia> multimedia = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventarioSucursal> inventarioSucursales = new ArrayList<>();

    @OneToMany(mappedBy = "productoVariante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductoVarianteAtributo> varianteAtributos = new ArrayList<>();
}
