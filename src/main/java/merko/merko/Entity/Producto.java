package merko.merko.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@Table(name = "producto", indexes = {
    @Index(name = "idx_producto_sku", columnList = "sku"),
    @Index(name = "idx_producto_estado", columnList = "estado"),
    @Index(name = "idx_producto_nombre", columnList = "nombre"),
    @Index(name = "idx_producto_categoria_id", columnList = "categoria_id")
})
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sku;

    private String nombre;

    private String descripcion;

    @Column(name = "precio_compra", nullable = false)
    private Double precioCompra;

    @Column(name = "precio_venta", nullable = false)
    private Double precioVenta;

    @Column(length = 100)
    private String estado;

    @Column(length = 100)
    private String tipo;

    @Column(name = "gestiona_lotes")
    private Boolean gestionaLotes;

    @Column(name = "codigo_barras", unique = true)
    private String codigoBarras;

    private String marca;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "punto_reorden")
    private Integer puntoReorden;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    private TipoAlmacenamiento almacenamiento;

    @Column(name = "requiere_vencimiento")
    private Boolean requiereVencimiento;

    @Column(name = "vida_util_dias")
    private Integer vidaUtilDias;

    @Column(name = "contenido_neto")
    private Double contenidoNeto;

    @Column(name = "contenido_uom")
    private String contenidoUom;

    @Column(name = "registro_sanitario")
    private String registroSanitario;

    @Column(name = "lead_time_dias")
    private Integer leadTimeDias;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Alias para compatibilidad con código antiguo que usa "precioBase"
    public Double getPrecioBase() {
        return this.precioVenta;
    }

    public void setPrecioBase(Double precioBase) {
        this.precioVenta = precioBase;
    }
}
