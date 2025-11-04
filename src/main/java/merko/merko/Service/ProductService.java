package merko.merko.Service;

import merko.merko.Entity.Producto;
import merko.merko.dto.ProductCreateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Legacy facade kept for backward compatibility. Delegates to {@link ApiProductService}.
 * Prefer using {@link ApiProductService} directly.
 */
@Deprecated
// Legacy delegator retained only for backward compatibility during refactor.
// No longer a Spring service bean to avoid confusion — prefer ProductoApiService.
public class ProductService {

    private final ApiProductService delegate;

    @Autowired
    public ProductService(ApiProductService delegate) {
        this.delegate = delegate;
    }

    public Producto crearProducto(ProductCreateDto dto) {
        return delegate.crearProducto(dto);
    }

    public List<Producto> crearProductos(List<ProductCreateDto> dtos) {
        return delegate.crearProductos(dtos);
    }
}
