package merko.merko.Service;

import merko.merko.Entity.Producto;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProductBranchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public Page<Producto> getProductos(String q, Long proveedorId, Pageable pageable) {
        String qParam = (q != null && !q.isBlank()) ? q : null;
        return productoRepository.search(proveedorId, qParam, pageable);
    }

    public Optional<Producto> getProductoById(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto saveProducto(Producto producto) {
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (producto.getPrecioVenta() < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo");
        }
        // Validaciones de negocio según tipo
        if (producto.getTipo() != null) {
            switch (producto.getTipo()) {
                case merko.merko.Entity.TipoProducto.MATERIA_PRIMA -> {
                    // Materia prima debe tener proveedor asociado
                    if (producto.getProveedor() == null) {
                        throw new IllegalArgumentException("La materia prima debe tener un proveedor asociado");
                    }
                }
                case merko.merko.Entity.TipoProducto.PRODUCTO_TERMINADO -> {
                    // Producto terminado puede no tener proveedor
                }
            }
        }

        // Si requiere vencimiento pero no hay vida útil válida, relajamos para no bloquear el flujo:
        // desactivamos 'requiereVencimiento' automáticamente.
        if (Boolean.TRUE.equals(producto.getRequiereVencimiento())) {
            if (producto.getVidaUtilDias() == null || producto.getVidaUtilDias() <= 0) {
                // Comportamiento tolerante: no lanzar excepción, solo desactivar el requisito.
                producto.setRequiereVencimiento(Boolean.FALSE);
            }
        }
        // Defaults seguros para nuevos campos
        if (producto.getTipo() == null) producto.setTipo(merko.merko.Entity.TipoProducto.MATERIA_PRIMA);
        if (producto.getEstado() == null) producto.setEstado(merko.merko.Entity.EstadoProducto.ACTIVO);
        if (producto.getUnidadMedida() == null) producto.setUnidadMedida(merko.merko.Entity.UnidadMedida.UNID);
    if (producto.getGestionaLotes() == null) producto.setGestionaLotes(Boolean.TRUE);
    // Por defecto, NO requerir vencimiento para evitar bloqueos si no se definió vida útil.
    if (producto.getRequiereVencimiento() == null) producto.setRequiereVencimiento(Boolean.FALSE);
        if (producto.getAlmacenamiento() == null) producto.setAlmacenamiento(merko.merko.Entity.Almacenamiento.AMBIENTE);
        if (producto.getStockMinimo() == null) producto.setStockMinimo(0);
        if (producto.getPuntoReorden() == null) producto.setPuntoReorden(0);
        if (producto.getLeadTimeDias() == null) producto.setLeadTimeDias(0);

        // Unicidad de SKU y código de barras si vienen informados
        if (producto.getSku() != null && !producto.getSku().isBlank()) {
            boolean exists = (producto.getId() == null)
                    ? productoRepository.existsBySku(producto.getSku())
                    : productoRepository.existsBySkuAndIdNot(producto.getSku(), producto.getId());
            if (exists) throw new IllegalArgumentException("El SKU ya está en uso");
        }
        if (producto.getCodigoBarras() != null && !producto.getCodigoBarras().isBlank()) {
            boolean exists = (producto.getId() == null)
                    ? productoRepository.existsByCodigoBarras(producto.getCodigoBarras())
                    : productoRepository.existsByCodigoBarrasAndIdNot(producto.getCodigoBarras(), producto.getId());
            if (exists) throw new IllegalArgumentException("El código de barras ya está en uso");
        }
        return productoRepository.save(producto);
    }

    @Transactional
    public void deleteProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("Producto con id " + id + " no existe");
        }
        // eliminar asignaciones por sucursal antes de borrar el producto para evitar FK constraints
        productBranchRepository.deleteByProductoId(id);
        productoRepository.deleteById(id);
    }

    @Transactional
    public Producto updateProducto(Long id, Producto productoActualizado) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto con id " + id + " no existe"));


        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setDescripcion(productoActualizado.getDescripcion());
        productoExistente.setPrecioVenta(productoActualizado.getPrecioVenta());
        productoExistente.setStock(productoActualizado.getStock());
        // Actualizar campos nuevos si llegan informados
        productoExistente.setSku(productoActualizado.getSku());
        productoExistente.setCodigoBarras(productoActualizado.getCodigoBarras());
        productoExistente.setMarca(productoActualizado.getMarca());
        if (productoActualizado.getTipo() != null) productoExistente.setTipo(productoActualizado.getTipo());
        if (productoActualizado.getEstado() != null) productoExistente.setEstado(productoActualizado.getEstado());
        if (productoActualizado.getUnidadMedida() != null) productoExistente.setUnidadMedida(productoActualizado.getUnidadMedida());
        productoExistente.setContenidoNeto(productoActualizado.getContenidoNeto());
        productoExistente.setContenidoUoM(productoActualizado.getContenidoUoM());
        if (productoActualizado.getGestionaLotes() != null) productoExistente.setGestionaLotes(productoActualizado.getGestionaLotes());
        if (productoActualizado.getRequiereVencimiento() != null) productoExistente.setRequiereVencimiento(productoActualizado.getRequiereVencimiento());
        productoExistente.setVidaUtilDias(productoActualizado.getVidaUtilDias());
        if (productoActualizado.getAlmacenamiento() != null) productoExistente.setAlmacenamiento(productoActualizado.getAlmacenamiento());
        productoExistente.setRegistroSanitario(productoActualizado.getRegistroSanitario());
        productoExistente.setStockMinimo(productoActualizado.getStockMinimo());
        productoExistente.setPuntoReorden(productoActualizado.getPuntoReorden());
        productoExistente.setLeadTimeDias(productoActualizado.getLeadTimeDias());

        return saveProducto(productoExistente);
    }

}
