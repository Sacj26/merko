package merko.merko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    private String nombre;
    private String descripcion;
    private Double precioCompra;
    private Double precioVenta;
    private Integer stock;
    private MultipartFile imagenUrl;
}
