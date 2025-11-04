package merko.merko.ControllerWeb;

import merko.merko.Entity.Producto;
import merko.merko.Service.ProductoApiService;
import merko.merko.dto.ProductCreateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductoApiService productoApiService;

    public ProductController(ProductoApiService productoApiService) {
        this.productoApiService = productoApiService;
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody ProductCreateDto dto) {
        Producto created = productoApiService.crearProducto(dto);
        return ResponseEntity.ok(created);
    }
}
