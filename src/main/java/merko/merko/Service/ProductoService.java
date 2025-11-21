package merko.merko.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import merko.merko.Entity.Producto;
import merko.merko.Repository.ProductBranchRepository;
import merko.merko.Repository.ProductoRepository;

/**
 * Servicio para Productos - BD: producto con 23 campos (precio_compra, precio_venta, estado, tipo, gestiona_lotes, etc.)
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductBranchRepository productBranchRepository;

    public ProductoService(ProductoRepository productoRepository, ProductBranchRepository productBranchRepository) {
        this.productoRepository = productoRepository;
        this.productBranchRepository = productBranchRepository;
    }

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public long countAll() {
        return productoRepository.count();
    }

    public Page<Producto> getProductos(String q, Long proveedorId, Pageable pageable) {
        String qParam = (q != null && !q.isBlank()) ? q : null;
        return productoRepository.search(proveedorId, qParam, pageable);
    }

    public Optional<Producto> getProductoById(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto saveProducto(Producto producto) {
        // Validación básica de precio
        if (producto.getPrecioBase() != null && producto.getPrecioBase() < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo");
        }

        // Validar unicidad de SKU
        if (producto.getSku() != null && !producto.getSku().isBlank()) {
            boolean exists = (producto.getId() == null)
                    ? productoRepository.existsBySku(producto.getSku())
                    : productoRepository.existsBySkuAndIdNot(producto.getSku(), producto.getId());
            if (exists) {
                throw new IllegalArgumentException("El SKU ya está en uso");
            }
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public void deleteProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("Producto con id " + id + " no existe");
        }
        // Eliminar asignaciones por sucursal antes de borrar el producto para evitar FK constraints
        productBranchRepository.deleteByProductoId(id);
        productoRepository.deleteById(id);
    }

    @Transactional
    public Producto updateProducto(Long id, Producto productoActualizado) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto con id " + id + " no existe"));

        // Actualizar solo los campos que existen en la BD simplificada
        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setDescripcion(productoActualizado.getDescripcion());
        productoExistente.setPrecioBase(productoActualizado.getPrecioBase());
        productoExistente.setSku(productoActualizado.getSku());
        
        // Si se actualiza la categoría
        if (productoActualizado.getCategoria() != null) {
            productoExistente.setCategoria(productoActualizado.getCategoria());
        }

        return saveProducto(productoExistente);
    }

}
