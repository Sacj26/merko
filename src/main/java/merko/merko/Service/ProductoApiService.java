package merko.merko.Service;

import merko.merko.Entity.ProductBranch;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Proveedor;
import merko.merko.Entity.ProductoProveedor;
import merko.merko.Repository.ProductBranchRepository;
import merko.merko.Repository.CategoriaRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProveedorRepository;
import merko.merko.Repository.ProductoProveedorRepository;
import merko.merko.Repository.BranchRepository;
import merko.merko.dto.ProductBranchAssignDto;
import merko.merko.dto.ProductCreateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoApiService {

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductBranchRepository productBranchRepository;
    private final ProductoProveedorRepository productoProveedorRepository;
    private final BranchRepository branchRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoApiService(ProductoRepository productoRepository,
                          ProveedorRepository proveedorRepository,
                          ProductoProveedorRepository productoProveedorRepository,
                          ProductBranchRepository productBranchRepository,
                          BranchRepository branchRepository,
                          CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoProveedorRepository = productoProveedorRepository;
        this.productBranchRepository = productBranchRepository;
        this.branchRepository = branchRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public Producto crearProducto(ProductCreateDto dto) {
        Producto p = new Producto();
        p.setNombre(dto.nombre);
        p.setDescripcion(dto.descripcion);
        p.setPrecioBase(dto.precioVenta != null ? dto.precioVenta.doubleValue() : 0.0);
        p.setSku(dto.sku);


        // Asignar categoría si se proporcionó
        if (dto.categoriaId != null) {
            categoriaRepository.findById(dto.categoriaId).ifPresent(p::setCategoria);
        }

        // Si se proporciona proveedor, crear relación en tabla producto_proveedor
        if (dto.proveedorId != null) {
            Proveedor prov = proveedorRepository.findById(dto.proveedorId).orElse(null);
            if (prov != null) {
                // la relación se crea después de guardar el producto
            }
        }

        Producto saved = productoRepository.save(p);

        // crear asignaciones por sucursal si vienen
        if (dto.stockPorSucursal != null) {
            for (ProductBranchAssignDto assign : dto.stockPorSucursal) {
                branchRepository.findById(assign.branchId).ifPresent(branch -> {
                    ProductBranch pb = new ProductBranch();
                    pb.setProducto(saved);
                    pb.setBranch(branch);
                    pb.setStock(assign.stock != null ? assign.stock.intValue() : 0);
                    productBranchRepository.save(pb);
                });
            }
        }

        // crear relación con proveedor si se solicitó
        if (dto.proveedorId != null) {
            Proveedor prov = proveedorRepository.findById(dto.proveedorId).orElse(null);
            if (prov != null) {
                ProductoProveedor pp = new ProductoProveedor();
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
            p.setPrecioBase(dto.precioVenta != null ? dto.precioVenta.doubleValue() : 0.0);
            p.setSku(dto.sku);

            // Asignar categoría si se proporcionó
            if (dto.categoriaId != null) {
                categoriaRepository.findById(dto.categoriaId).ifPresent(p::setCategoria);
            }

            Producto saved = productoRepository.save(p);

            if (dto.stockPorSucursal != null) {
                for (ProductBranchAssignDto assign : dto.stockPorSucursal) {
                    branchRepository.findById(assign.branchId).ifPresent(branch -> {
                        ProductBranch pb = new ProductBranch();
                        pb.setProducto(saved);
                        pb.setBranch(branch);
                        pb.setStock(assign.stock != null ? assign.stock : Integer.valueOf(0));
                        productBranchRepository.save(pb);
                    });
                }
            }

            // crear relación proveedor-producto si existe
            if (dto.proveedorId != null) {
                Proveedor prov = proveedorRepository.findById(dto.proveedorId).orElse(null);
                if (prov != null) {
                    merko.merko.Entity.ProductoProveedor pp = new merko.merko.Entity.ProductoProveedor();
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
