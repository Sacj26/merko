package merko.merko.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import merko.merko.Entity.DetalleVenta;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Venta;
import merko.merko.Entity.ProductBranch;
import merko.merko.Repository.MovimientoInventarioRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.VentaRepository;
import merko.merko.Repository.LoteRepository;
import merko.merko.Repository.ProductBranchRepository;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final LoteRepository loteRepository;
    private final ProductBranchRepository productBranchRepository;

    public VentaService(VentaRepository ventaRepository,
                        ProductoRepository productoRepository,
                        MovimientoInventarioRepository movimientoInventarioRepository,
                        LoteRepository loteRepository,
                        ProductBranchRepository productBranchRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.loteRepository = loteRepository;
        this.productBranchRepository = productBranchRepository;
    }

    public List<Venta> getAllVentas() {
        return ventaRepository.findAll();
    }

    public Venta getVentaById(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID " + id));
    }

    @Transactional
    public Venta saveVenta(Venta venta, Long sucursalId) {
        if (venta.getCliente() == null) {
            throw new IllegalArgumentException("La venta debe estar asociada a un cliente.");
        }
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle.");
        }

        double totalVenta = 0.0;
        java.util.List<merko.merko.Entity.MovimientoInventario> movimientosDraft = new java.util.ArrayList<>();

        for (DetalleVenta detalle : venta.getDetalles()) {
            if (detalle.getProducto() == null) {
                throw new IllegalArgumentException("Cada detalle debe tener un producto asociado.");
            }
            if (detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad en cada detalle debe ser mayor a cero.");
            }

            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: ID " + detalle.getProducto().getId()));

            // Solo se venden productos terminados activos
            if (producto.getEstado() != merko.merko.Entity.EstadoProducto.ACTIVO) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no está activo para la venta.");
            }
            if (producto.getTipo() != merko.merko.Entity.TipoProducto.PRODUCTO_TERMINADO) {
                throw new IllegalArgumentException("No se puede vender materia prima: " + producto.getNombre());
            }

            // Si se seleccionó sucursal, intentar obtener ProductBranch
            Optional<ProductBranch> pbOpt = (sucursalId != null) ? productBranchRepository.findByProductoIdAndBranchId(producto.getId(), sucursalId) : Optional.empty();
            ProductBranch pb = pbOpt.orElse(null);

            // Validar stock: preferir stock por sucursal si existe
            if (pb != null) {
                int stockPb = pb.getStock() == null ? 0 : pb.getStock();
                if (stockPb < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente en la sucursal para el producto: " + producto.getNombre());
                }
            } else {
                if (producto.getStock() < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
                }
            }

            // Asignación de lotes FEFO si el producto gestiona lotes
            if (Boolean.TRUE.equals(producto.getGestionaLotes())) {
                int porVender = detalle.getCantidad();
                java.time.LocalDate hoy = java.time.LocalDate.now();
                java.util.List<merko.merko.Entity.Lote> lotes = loteRepository
                        .findByProductoAndEstadoOrderByFechaVencimientoAsc(producto, merko.merko.Entity.EstadoLote.ACTIVO);

                for (merko.merko.Entity.Lote lote : lotes) {
                    if (porVender <= 0) break;
                    // Saltar lotes vencidos y marcarlos como VENCIDO
                    if (lote.getFechaVencimiento() != null && lote.getFechaVencimiento().isBefore(hoy)) {
                        lote.setEstado(merko.merko.Entity.EstadoLote.VENCIDO);
                        loteRepository.save(lote);
                        continue;
                    }
                    int disponible = lote.getCantidadDisponible() != null ? lote.getCantidadDisponible() : 0;
                    if (disponible <= 0) continue;
                    int tomar = Math.min(disponible, porVender);
                    lote.setCantidadDisponible(disponible - tomar);
                    if (lote.getCantidadDisponible() != null && lote.getCantidadDisponible() == 0) {
                        lote.setEstado(merko.merko.Entity.EstadoLote.AGOTADO);
                    }
                    loteRepository.save(lote);

                    merko.merko.Entity.MovimientoInventario mov = new merko.merko.Entity.MovimientoInventario();
                    mov.setProducto(producto);
                    mov.setLote(lote);
                    mov.setTipo(merko.merko.Entity.TipoMovimiento.VENTA_SALIDA);
                    mov.setCantidad(tomar);
                    // usar el costo del lote como costo de salida
                    mov.setCostoUnitario(lote.getCostoUnitario());
                    mov.setFecha(java.time.LocalDateTime.now());
                    if (pb != null) mov.setProductBranch(pb);
                    // venta y referencia se setean tras persistir la venta
                    movimientosDraft.add(mov);
                    porVender -= tomar;
                }

                if (porVender > 0) {
                    throw new IllegalArgumentException("No hay suficientes lotes disponibles (no vencidos) para el producto: " + producto.getNombre());
                }
            }

            // Actualiza stock total del producto
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);

            // Si hay ProductBranch, decrementar stock por sucursal también
            if (pb != null) {
                int cur = pb.getStock() == null ? 0 : pb.getStock();
                pb.setStock(cur - detalle.getCantidad());
                productBranchRepository.save(pb);
            }

            double subtotal = detalle.getCantidad() * producto.getPrecioVenta();
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setVenta(venta);

            totalVenta += subtotal;
            // Para productos que no gestionan lotes, preparar un único movimiento sin lote
            if (!Boolean.TRUE.equals(producto.getGestionaLotes())) {
                merko.merko.Entity.MovimientoInventario mov = new merko.merko.Entity.MovimientoInventario();
                mov.setProducto(producto);
                mov.setLote(null);
                mov.setTipo(merko.merko.Entity.TipoMovimiento.VENTA_SALIDA);
                mov.setCantidad(detalle.getCantidad());
                // sin lotes: usar precioCompra del producto como costo de salida (aproximación)
                mov.setCostoUnitario(producto.getPrecioCompra());
                mov.setFecha(java.time.LocalDateTime.now());
                if (pb != null) mov.setProductBranch(pb);
                movimientosDraft.add(mov);
            }
        }

        if (totalVenta < 0) {
            throw new IllegalArgumentException("El total de la venta no puede ser negativo.");
        }

    venta.setTotal(totalVenta);
    venta.setFecha(java.time.LocalDateTime.now());

        Venta saved = ventaRepository.save(venta);
        // Persistir movimientos preparados con referencia a la venta
        for (merko.merko.Entity.MovimientoInventario mov : movimientosDraft) {
            mov.setVenta(saved);
            mov.setReferencia("VENTA-" + saved.getId());
            movimientoInventarioRepository.save(mov);
        }
        return saved;
    }

    @Transactional
    public void deleteVenta(Long id) {
        if (!ventaRepository.existsById(id)) {
            throw new IllegalArgumentException("La venta con ID " + id + " no existe.");
        }
        ventaRepository.deleteById(id);
    }

    public Optional<Venta> getVentaByIdWithDetalles(Long id) {
        return ventaRepository.findByIdWithDetalles(id);
    }

    // Reversa básica de venta: repone inventario según movimientos de la venta
    @Transactional
    public void reverseVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID " + ventaId));

        var movimientos = movimientoInventarioRepository.findByVenta_IdOrderByFechaAsc(ventaId);
        if (movimientos.isEmpty()) {
            throw new IllegalStateException("No hay movimientos asociados a la venta para revertir.");
        }

        for (var mov : movimientos) {
            if (mov.getTipo() != merko.merko.Entity.TipoMovimiento.VENTA_SALIDA) continue;
            var producto = mov.getProducto();
            Integer cant = mov.getCantidad() != null ? mov.getCantidad() : 0;
            if (cant <= 0) continue;

            // Reponer lotes si aplica
            var lote = mov.getLote();
            if (lote != null) {
                // refrescar estado de lote
                var loteRef = loteRepository.findById(lote.getId()).orElse(null);
                if (loteRef != null) {
                    int disp = loteRef.getCantidadDisponible() != null ? loteRef.getCantidadDisponible() : 0;
                    loteRef.setCantidadDisponible(disp + cant);
                    // Si el lote está vencido por fecha, mantener VENCIDO; de lo contrario, marcar ACTIVO
                    var hoy = java.time.LocalDate.now();
                    boolean vencidoPorFecha = loteRef.getFechaVencimiento() != null && loteRef.getFechaVencimiento().isBefore(hoy);
                    loteRef.setEstado(vencidoPorFecha ? merko.merko.Entity.EstadoLote.VENCIDO : merko.merko.Entity.EstadoLote.ACTIVO);
                    loteRepository.save(loteRef);
                }
            }

            // Reponer stock total
            producto.setStock(producto.getStock() + cant);
            productoRepository.save(producto);

            // Reponer stock de sucursal si aplica
            var pb = mov.getProductBranch();
            if (pb != null) {
                var pbRef = productBranchRepository.findById(pb.getId()).orElse(null);
                if (pbRef != null) {
                    int cur = pbRef.getStock() == null ? 0 : pbRef.getStock();
                    pbRef.setStock(cur + cant);
                    productBranchRepository.save(pbRef);
                }
            }

            // Registrar ajuste de reversa
            var ajuste = new merko.merko.Entity.MovimientoInventario();
            ajuste.setProducto(producto);
            ajuste.setLote(lote);
            ajuste.setTipo(merko.merko.Entity.TipoMovimiento.AJUSTE);
            ajuste.setCantidad(cant);
            ajuste.setCostoUnitario(mov.getCostoUnitario());
            ajuste.setFecha(java.time.LocalDateTime.now());
            ajuste.setVenta(venta);
            ajuste.setReferencia("REVERSO-VENTA-" + ventaId);
            movimientoInventarioRepository.save(ajuste);
        }
        // Marcar venta como ANULADA
        venta.setEstado(merko.merko.Entity.EstadoVenta.ANULADA);
        ventaRepository.save(venta);
    }

}
