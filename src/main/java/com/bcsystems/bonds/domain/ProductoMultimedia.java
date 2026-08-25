package com.bcsystems.bonds.domain;

import com.bcsystems.bonds.domain.en.TipoMultimedia;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "producto_multimedia")
public class ProductoMultimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMultimedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMultimedia tipo;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String nombreArchivo;

    @Column(nullable = false)
    private Boolean esPrincipal;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSubida;
}
