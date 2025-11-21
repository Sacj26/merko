package merko.merko.Service;

import merko.merko.Entity.ProductBranch;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.ProductBranchRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProveedorRepository;
import merko.merko.Repository.ProductoProveedorRepository;
import merko.merko.Repository.BranchRepository;
import merko.merko.dto.ProductBranchAssignDto;
import merko.merko.dto.ProductCreateDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Clase deprecada - Las operaciones de API se han consolidado en ProductoApiService.
 * Esta clase se mantiene solo como referencia y no expone un bean @Service para evitar duplicación.
 * 
 * Adaptada para BD simplificada: producto (id, sku, nombre, descripcion, precio_base, categoria_id)
 */
@Deprecated
public class ApiProductService {

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductBranchRepository productBranchRepository;
    private final BranchRepository branchRepository;
    private final ProductoProveedorRepository productoProveedorRepository;

    public ApiProductService(ProductoRepository productoRepository,
                          ProveedorRepository proveedorRepository,
                          ProductBranchRepository productBranchRepository,
                          BranchRepository branchRepository,
                          ProductoProveedorRepository productoProveedorRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.productBranchRepository = productBranchRepository;
        this.branchRepository = branchRepository;
        this.productoProveedorRepository = productoProveedorRepository;
    }

    @Transactional
    public Producto crearProducto(ProductCreateDto dto) {
        Producto p = new Producto();
        p.setNombre(dto.nombre);
        p.setDescripcion(dto.descripcion);
        // BD simplificada: usar precioVenta como precio_base
        p.setPrecioBase(dto.precioVenta != null ? dto.precioVenta.doubleValue() : 0.0);
        p.setSku(dto.sku);

        Producto saved = productoRepository.save(p);

        // Crear asignaciones por sucursal si vienen
        if (dto.stockPorSucursal != null) {
            for (ProductBranchAssignDto assign : dto.stockPorSucursal) {
                branchRepository.findById(assign.branchId).ifPresent(branch -> {
                    ProductBranch pb = new ProductBranch();
                    pb.setProducto(saved);
                    pb.setBranch(branch);
                    pb.setStock(assign.stock != null ? assign.stock : 0);
                    productBranchRepository.save(pb);
                });
            }
        }

        // Crear relación proveedor-producto
        if (dto.proveedorId != null) {
            Proveedor prov = proveedorRepository.findById(dto.proveedorId).orElse(null);
            if (prov != null) {
                var pp = new merko.merko.Entity.ProductoProveedor();
                pp.setProducto(saved);
                pp.setProveedor(prov);
                productoProveedorRepository.save(pp);
            }
        }
        return saved;
    }

    @Transactional
    public List<Producto> crearProductos(List<ProductCreateDto> dtos) {
        List<Producto> created = new ArrayList<>();
        for (ProductCreateDto dto : dtos) {
            Producto p = new Producto();
            p.setNombre(dto.nombre);
            p.setDescripcion(dto.descripcion);
            // BD simplificada: usar precioVenta como precio_base
            p.setPrecioBase(dto.precioVenta != null ? dto.precioVenta.doubleValue() : 0.0);
            p.setSku(dto.sku);

            Producto saved = productoRepository.save(p);

            if (dto.stockPorSucursal != null) {
                for (ProductBranchAssignDto assign : dto.stockPorSucursal) {
                    branchRepository.findById(assign.branchId).ifPresent(branch -> {
                        ProductBranch pb = new ProductBranch();
                        pb.setProducto(saved);
                        pb.setBranch(branch);
                        pb.setStock(assign.stock != null ? assign.stock : 0);
                        productBranchRepository.save(pb);
                    });
                }
            }

            // Crear relación proveedor-producto si existe
            if (dto.proveedorId != null) {
                Proveedor prov = proveedorRepository.findById(dto.proveedorId).orElse(null);
                if (prov != null) {
                    var pp = new merko.merko.Entity.ProductoProveedor();
                    pp.setProducto(saved);
                    pp.setProveedor(prov);
                    productoProveedorRepository.save(pp);
                }
            }

            created.add(saved);
        }
        return created;
    }
}
