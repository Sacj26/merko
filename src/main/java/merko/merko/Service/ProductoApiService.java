package merko.merko.Service;

import merko.merko.Entity.ProductBranch;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.ProductBranchRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProveedorRepository;
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
    private final BranchRepository branchRepository;

    public ProductoApiService(ProductoRepository productoRepository,
                          ProveedorRepository proveedorRepository,
                          ProductBranchRepository productBranchRepository,
                          BranchRepository branchRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.productBranchRepository = productBranchRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional
    public Producto crearProducto(ProductCreateDto dto) {
        Producto p = new Producto();
        p.setNombre(dto.nombre);
        p.setDescripcion(dto.descripcion);
        p.setImagenUrl(dto.imagenUrl);
        p.setPrecioVenta(dto.precioVenta != null ? dto.precioVenta.doubleValue() : 0.0);
        if (dto.precioCompra != null) p.setPrecioCompra(dto.precioCompra.doubleValue());
        if (dto.marca != null) p.setMarca(dto.marca);
        if (dto.stock != null) p.setStock(dto.stock);
        if (dto.stockMinimo != null) p.setStockMinimo(dto.stockMinimo);
        if (dto.puntoReorden != null) p.setPuntoReorden(dto.puntoReorden);
        if (dto.leadTimeDias != null) p.setLeadTimeDias(dto.leadTimeDias);
        if (dto.gestionaLotes != null) p.setGestionaLotes(dto.gestionaLotes);
        if (dto.requiereVencimiento != null) p.setRequiereVencimiento(dto.requiereVencimiento);
        if (dto.vidaUtilDias != null) p.setVidaUtilDias(dto.vidaUtilDias);
        if (dto.registroSanitario != null) p.setRegistroSanitario(dto.registroSanitario);
        if (dto.contenidoNeto != null) p.setContenidoNeto(dto.contenidoNeto);
        if (dto.almacenamiento != null) {
            try { p.setAlmacenamiento(merko.merko.Entity.Almacenamiento.valueOf(dto.almacenamiento)); } catch (Exception ignored) {}
        }
        if (dto.tipo != null) {
            try { p.setTipo(merko.merko.Entity.TipoProducto.valueOf(dto.tipo)); } catch (Exception ignored) {}
        }
        if (dto.estado != null) {
            try { p.setEstado(merko.merko.Entity.EstadoProducto.valueOf(dto.estado)); } catch (Exception ignored) {}
        }
        if (dto.unidadMedida != null) {
            try { p.setUnidadMedida(merko.merko.Entity.UnidadMedida.valueOf(dto.unidadMedida)); } catch (Exception ignored) {}
        }
        if (dto.contenidoUoM != null) {
            try { p.setContenidoUoM(merko.merko.Entity.UnidadMedida.valueOf(dto.contenidoUoM)); } catch (Exception ignored) {}
        }
        p.setSku(dto.sku);
        p.setCodigoBarras(dto.codigoBarras);

        if (dto.proveedorId != null) {
            Proveedor prov = proveedorRepository.findById(dto.proveedorId).orElse(null);
            p.setProveedor(prov);
        }

        Producto saved = productoRepository.save(p);

        // crear asignaciones por sucursal si vienen
        if (dto.stockPorSucursal != null) {
            List<ProductBranch> created = new ArrayList<>();
            for (ProductBranchAssignDto assign : dto.stockPorSucursal) {
                branchRepository.findById(assign.branchId).ifPresent(branch -> {
                    ProductBranch pb = new ProductBranch();
                    pb.setProducto(saved);
                    pb.setBranch(branch);
                    pb.setStock(assign.stock != null ? assign.stock : 0);
                    pb.setPrecio(assign.precio != null ? assign.precio.doubleValue() : null);
                    created.add(productBranchRepository.save(pb));
                });
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
            p.setImagenUrl(dto.imagenUrl);
            p.setPrecioVenta(dto.precioVenta != null ? dto.precioVenta.doubleValue() : 0.0);
            if (dto.precioCompra != null) p.setPrecioCompra(dto.precioCompra.doubleValue());
            if (dto.marca != null) p.setMarca(dto.marca);
            if (dto.stock != null) p.setStock(dto.stock);
            if (dto.stockMinimo != null) p.setStockMinimo(dto.stockMinimo);
            if (dto.puntoReorden != null) p.setPuntoReorden(dto.puntoReorden);
            if (dto.leadTimeDias != null) p.setLeadTimeDias(dto.leadTimeDias);
            if (dto.gestionaLotes != null) p.setGestionaLotes(dto.gestionaLotes);
            if (dto.requiereVencimiento != null) p.setRequiereVencimiento(dto.requiereVencimiento);
            if (dto.vidaUtilDias != null) p.setVidaUtilDias(dto.vidaUtilDias);
            if (dto.registroSanitario != null) p.setRegistroSanitario(dto.registroSanitario);
            if (dto.contenidoNeto != null) p.setContenidoNeto(dto.contenidoNeto);
            if (dto.almacenamiento != null) {
                try { p.setAlmacenamiento(merko.merko.Entity.Almacenamiento.valueOf(dto.almacenamiento)); } catch (Exception ignored) {}
            }
            if (dto.tipo != null) {
                try { p.setTipo(merko.merko.Entity.TipoProducto.valueOf(dto.tipo)); } catch (Exception ignored) {}
            }
            if (dto.estado != null) {
                try { p.setEstado(merko.merko.Entity.EstadoProducto.valueOf(dto.estado)); } catch (Exception ignored) {}
            }
            if (dto.unidadMedida != null) {
                try { p.setUnidadMedida(merko.merko.Entity.UnidadMedida.valueOf(dto.unidadMedida)); } catch (Exception ignored) {}
            }
            if (dto.contenidoUoM != null) {
                try { p.setContenidoUoM(merko.merko.Entity.UnidadMedida.valueOf(dto.contenidoUoM)); } catch (Exception ignored) {}
            }
            p.setSku(dto.sku);
            p.setCodigoBarras(dto.codigoBarras);

            if (dto.proveedorId != null) {
                Proveedor prov = proveedorRepository.findById(dto.proveedorId).orElse(null);
                p.setProveedor(prov);
            }

            Producto saved = productoRepository.save(p);

            if (dto.stockPorSucursal != null) {
                for (ProductBranchAssignDto assign : dto.stockPorSucursal) {
                    branchRepository.findById(assign.branchId).ifPresent(branch -> {
                        ProductBranch pb = new ProductBranch();
                        pb.setProducto(saved);
                        pb.setBranch(branch);
                        pb.setStock(assign.stock != null ? assign.stock : 0);
                        pb.setPrecio(assign.precio != null ? assign.precio.doubleValue() : null);
                        productBranchRepository.save(pb);
                    });
                }
            }

            created.add(saved);
        }
        return created;
    }
}
