package merko.merko.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductCreateDto {
    public String nombre;
    public String descripcion;
    public BigDecimal precioVenta;
    public String sku;
    public String codigoBarras;
    public Long proveedorId;
    public List<ProductBranchAssignDto> stockPorSucursal;
    // ruta relativa de la imagen si fue subida y procesada por el controlador (ej: /images/xyz.jpg)
    public String imagenUrl;
    // Campos adicionales del formulario
    public String marca;
    public BigDecimal precioCompra;
    public Integer stock;
    public Integer stockMinimo;
    public Integer puntoReorden;
    public Integer leadTimeDias;
    public Boolean gestionaLotes;
    public Boolean requiereVencimiento;
    public Integer vidaUtilDias;
    public String almacenamiento;
    public String registroSanitario;
    public String tipo; // Enum name expected
    public String estado; // Enum name expected
    public String unidadMedida; // Enum name expected
    public Double contenidoNeto;
    public String contenidoUoM; // Enum name expected
}
