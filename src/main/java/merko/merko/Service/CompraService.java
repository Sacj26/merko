package merko.merko.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import merko.merko.Entity.Branch;
import merko.merko.Entity.Compra;
import merko.merko.Entity.DetalleCompra;
import merko.merko.Entity.Producto;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.CompraRepository;
import merko.merko.Repository.DetalleCompraRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.dto.CompraForm;
import merko.merko.dto.DetalleCompraForm;

/**
 * Servicio simplificado para Compras - BD: compra (id, fecha, cantidad, precio_unidad, total, branch_id)
 */
@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final BranchRepository branchRepository;
    private final ProductoRepository productoRepository;

    public CompraService(CompraRepository compraRepository,
                        DetalleCompraRepository detalleCompraRepository,
                        BranchRepository branchRepository,
                        ProductoRepository productoRepository) {
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.branchRepository = branchRepository;
        this.productoRepository = productoRepository;
    }

    public List<Compra> getAllCompras() {
        return compraRepository.findAll();
    }

    public List<Compra> getAllComprasWithBranchAndDetalles() {
        return compraRepository.findAllWithBranchAndDetalles();
    }

    public Optional<Compra> getCompraById(Long id) {
        return compraRepository.findById(id);
    }

    @Transactional
    public Compra crearCompra(Compra compra) {
        if (compra.getBranch() == null) {
            throw new IllegalArgumentException("Una compra debe tener una sucursal.");
        }
        if (compra.getFecha() == null) {
            compra.setFecha(LocalDateTime.now());
        }
        if (compra.getTotal() == null || compra.getTotal() <= 0) {
            throw new IllegalArgumentException("El total debe ser mayor a cero.");
        }
        return compraRepository.save(compra);
    }

    @Transactional
    public void deleteCompra(Long id) {
        compraRepository.deleteById(id);
    }

    @Transactional
    public Compra guardarCompraConDetalles(CompraForm form) {
        if (form.getDetalles() == null || form.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la compra");
        }

        // Obtener sucursal (branch)
        Branch branch = null;
        if (form.getSucursalId() != null) {
            branch = branchRepository.findById(form.getSucursalId())
                    .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        }

        // Calcular totales
        double totalCompra = 0.0;
        int cantidadTotal = 0;

        for (DetalleCompraForm detalle : form.getDetalles()) {
            double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
            totalCompra += subtotal;
            cantidadTotal += detalle.getCantidad();
        }

        // Crear entidad Compra
        Compra compra = new Compra();
        compra.setFecha(LocalDateTime.now());
        compra.setBranch(branch);
        compra.setCantidad(cantidadTotal);
        compra.setPrecioUnidad(form.getDetalles().size() == 1 ? form.getDetalles().get(0).getPrecioUnitario() : 0.0);
        compra.setTotal(totalCompra);

        // Guardar compra
        compra = compraRepository.save(compra);

        // Crear y guardar detalles
        for (DetalleCompraForm detalleForm : form.getDetalles()) {
            Producto producto = productoRepository.findById(detalleForm.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + detalleForm.getProductoId()));

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleForm.getCantidad());
            detalle.setPrecioCompra(detalleForm.getPrecioUnitario()); // Guardar en precioCompra (campo principal)
            detalle.setPrecioUnitario(detalleForm.getPrecioUnitario()); // También en precioUnitario por compatibilidad

            // Establecer branch del detalle (puede ser diferente al branch de la compra)
            if (detalleForm.getBranchId() != null) {
                branchRepository.findById(detalleForm.getBranchId())
                        .ifPresent(detalle::setBranch);
            }

            detalleCompraRepository.save(detalle);
        }

        return compra;
    }
}
