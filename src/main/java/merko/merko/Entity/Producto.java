package merko.merko.Entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private double precioCompra;
    private double precioVenta;
    private int stock;

    // Identificación comercial
    @Column(unique = true)
    private String sku;               // Código interno
    @Column(name = "codigo_barras", unique = true)
    private String codigoBarras;      // EAN/UPC
    private String marca;

    // Clasificación y estado
    @Enumerated(EnumType.STRING)
    private TipoProducto tipo = TipoProducto.MATERIA_PRIMA;
    @Enumerated(EnumType.STRING)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    // Unidades y presentación
    @Enumerated(EnumType.STRING)
    private UnidadMedida unidadMedida = UnidadMedida.UNID;
    private Double contenidoNeto; // p. ej. 500
    @Enumerated(EnumType.STRING)
    private UnidadMedida contenidoUoM; // p. ej. G, ML

    // Lotes y vencimientos
    private Boolean gestionaLotes = Boolean.TRUE;
    private Boolean requiereVencimiento = Boolean.TRUE;
    private Integer vidaUtilDias; // días de vida útil
    @Enumerated(EnumType.STRING)
    private Almacenamiento almacenamiento = Almacenamiento.AMBIENTE;
    private String registroSanitario; // INVIMA u otro

    // Reabastecimiento
    private Integer stockMinimo = 0;
    private Integer puntoReorden = 0;
    private Integer leadTimeDias = 0; // tiempo de reposición

    @Column(name = "imagen_url")
    private String imagenUrl;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compra> compras;
}
