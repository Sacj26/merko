package merko.merko.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import merko.merko.Entity.Compra;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Proveedor;
import merko.merko.Entity.DetalleCompra;
import merko.merko.Repository.CompraRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProveedorRepository;
import merko.merko.Repository.DetalleCompraRepository;
import merko.merko.Repository.LoteRepository;
import merko.merko.Repository.MovimientoInventarioRepository;
import merko.merko.dto.CompraForm;
import merko.merko.dto.DetalleCompraForm;

@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public CompraService(CompraRepository compraRepository,
                         ProductoRepository productoRepository,
                         ProveedorRepository proveedorRepository,
                         DetalleCompraRepository detalleCompraRepository,
                         LoteRepository loteRepository,
                         MovimientoInventarioRepository movimientoInventarioRepository) {
        this.compraRepository = compraRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.loteRepository = loteRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        
    }
    public List<Compra> getAllCompras() {
        return compraRepository.findAll();
    }

    public Optional<Compra> getCompraById(Long id) {
        return compraRepository.findById(id);
    }

    @Transactional
    public void deleteCompra(Long id) {
        compraRepository.deleteById(id);
    }

    @Transactional
    public Compra saveCompra(Compra compra) {

        if (compra.getProveedor() == null) {
            throw new IllegalArgumentException("La compra debe estar asociada a un proveedor.");
        }
        if (compra.getDetalles() == null || compra.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener al menos un detalle.");
        }

        double totalCompra = 0.0;

        for (var detalle : compra.getDetalles()) {
            if (detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad en cada detalle debe ser mayor a cero.");
            }
            if (detalle.getProducto() == null) {
                throw new IllegalArgumentException("El producto en cada detalle es obligatorio.");
            }

            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: ID " + detalle.getProducto().getId()));

            // En una compra incrementamos el stock
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);

            Double precioCompraObj = detalle.getPrecioCompra();
            double precioCompra;
            if (precioCompraObj != null) {
                precioCompra = precioCompraObj.doubleValue();
            } else {
                precioCompra = producto.getPrecioCompra();
            }
            double subtotal = detalle.getCantidad() * precioCompra;
            detalle.setPrecioUnitario(precioCompra);
            detalle.setCompra(compra);
            totalCompra += subtotal;
        }
        if (totalCompra <= 0) {
            throw new IllegalArgumentException("El total de la compra debe ser mayor a cero.");
        }
        compra.setTotal(totalCompra);
        compra.setFecha(LocalDate.now());

        return compraRepository.save(compra);
    }


    public Optional<Compra> getCompraByIdWithDetalles(Long id) {
        return compraRepository.findByIdWithDetalles(id);
    }

    // Nueva API usada por el controlador para manejar el formulario
    @Transactional
    public Compra guardarCompra(CompraForm compraForm,
                                Long productoId,
                                Integer cantidad,
                                Double precioUnitario) {
        Compra compra = new Compra();
        compra.setFecha(LocalDate.now());

        if (compraForm.getProveedorId() != null) {
            Optional<Proveedor> proveedorOpt = proveedorRepository.findById(compraForm.getProveedorId());
            proveedorOpt.ifPresent(compra::setProveedor);
        }
        if (compra.getProveedor() == null) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor para la compra.");
        }

        List<DetalleCompra> detalles = new ArrayList<>();
        if (compraForm.getDetalles() != null) {
            for (DetalleCompraForm df : compraForm.getDetalles()) {
                Producto p = productoRepository.findById(df.getProductoId()).orElse(null);
                if (p == null) continue;
                if (p.getProveedor() == null || !Objects.equals(p.getProveedor().getId(), compra.getProveedor().getId())) {
                    throw new IllegalArgumentException("El producto " + p.getNombre() + " no pertenece al proveedor seleccionado.");
                }
                if (p.getEstado() != merko.merko.Entity.EstadoProducto.ACTIVO) {
                    throw new IllegalArgumentException("El producto " + p.getNombre() + " no está activo para compras.");
                }
                if (df.getCantidad() <= 0) {
                    throw new IllegalArgumentException("La cantidad debe ser mayor a cero para el producto " + p.getNombre());
                }
                if (df.getPrecioUnitario() < 0) {
                    throw new IllegalArgumentException("El precio unitario no puede ser negativo para el producto " + p.getNombre());
                }
                DetalleCompra det = new DetalleCompra();
                det.setProducto(p);
                det.setCantidad(df.getCantidad());
                det.setPrecioUnitario(df.getPrecioUnitario());
                det.setCompra(compra);
                detalles.add(det);

                p.setStock(p.getStock() + df.getCantidad());
                productoRepository.save(p);
            }
        } else if (productoId != null && cantidad != null && precioUnitario != null) {
            Producto p = productoRepository.findById(productoId).orElse(null);
            if (p != null) {
                if (p.getProveedor() == null || !Objects.equals(p.getProveedor().getId(), compra.getProveedor().getId())) {
                    throw new IllegalArgumentException("El producto " + p.getNombre() + " no pertenece al proveedor seleccionado.");
                }
                if (p.getEstado() != merko.merko.Entity.EstadoProducto.ACTIVO) {
                    throw new IllegalArgumentException("El producto " + p.getNombre() + " no está activo para compras.");
                }
                if (cantidad <= 0) {
                    throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
                }
                if (precioUnitario < 0) {
                    throw new IllegalArgumentException("El precio unitario no puede ser negativo.");
                }
                DetalleCompra det = new DetalleCompra();
                det.setProducto(p);
                det.setCantidad(cantidad);
                det.setPrecioUnitario(precioUnitario);
                det.setCompra(compra);
                detalles.add(det);

                p.setStock(p.getStock() + cantidad);
                productoRepository.save(p);
            }
        }

        double total = detalles.stream().mapToDouble(d -> d.getCantidad() * (d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0.0)).sum();
        compra.setDetalles(detalles);
        compra.setTotal(total);

        if (detalles.size() == 1) {
            var unico = detalles.get(0);
            compra.setProducto(unico.getProducto());
            compra.setCantidad(unico.getCantidad());
            if (unico.getPrecioUnitario() != null) {
                compra.setPrecioUnidad(unico.getPrecioUnitario());
            }
        }

        compraRepository.save(compra);
        if (!detalles.isEmpty()) {
            detalleCompraRepository.saveAll(detalles);
            for (DetalleCompra det : detalles) {
                Producto p = det.getProducto();
                merko.merko.Entity.Lote lote = null;
                if (Boolean.TRUE.equals(p.getGestionaLotes())) {
                    lote = new merko.merko.Entity.Lote();
                    lote.setProducto(p);
                    lote.setCodigoLote(java.util.UUID.randomUUID().toString());
                    LocalDate hoy = LocalDate.now();
                    lote.setFechaFabricacion(hoy);
                    if (Boolean.TRUE.equals(p.getRequiereVencimiento()) && p.getVidaUtilDias() != null && p.getVidaUtilDias() > 0) {
                        lote.setFechaVencimiento(hoy.plusDays(p.getVidaUtilDias()));
                    }
                    lote.setCantidadDisponible(det.getCantidad());
                    lote.setEstado(merko.merko.Entity.EstadoLote.ACTIVO);
                    // costo del lote proveniente del precio de compra
                    lote.setCostoUnitario(det.getPrecioUnitario());
                    lote = loteRepository.save(lote);
                }

                merko.merko.Entity.MovimientoInventario mov = new merko.merko.Entity.MovimientoInventario();
                mov.setProducto(p);
                mov.setLote(lote);
                mov.setTipo(merko.merko.Entity.TipoMovimiento.COMPRA_INGRESO);
                mov.setCantidad(det.getCantidad());
                mov.setCostoUnitario(det.getPrecioUnitario());
                mov.setFecha(java.time.LocalDateTime.now());
                mov.setCompra(compra);
                mov.setReferencia("COMPRA-" + compra.getId());
                movimientoInventarioRepository.save(mov);
            }
        }

        return compra;
    }
}
