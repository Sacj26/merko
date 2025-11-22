package merko.merko.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import merko.merko.Entity.Devolucion;
import merko.merko.Entity.EstadoLote;
import merko.merko.Entity.EstadoVenta;
import merko.merko.Entity.Lote;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Venta;
import merko.merko.Repository.DevolucionRepository;
import merko.merko.Repository.LoteRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.VentaRepository;

@Service
public class DevolucionService {

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    /**
     * Registrar una devolución TOTAL: devuelve el inventario y anula la venta completa
     * 
     * Proceso:
     * 1. Busca el lote ACTIVO más antiguo (FIFO) del producto devuelto
     * 2. Devuelve la cantidad al inventario del lote (cantidadDisponible)
     * 3. Registra la devolución en la base de datos
     * 4. Cambia el estado de la venta a ANULADA
     * 
     * NOTA: Usa estrategia FIFO (First In, First Out) - devuelve al lote más antiguo.
     * Como DetalleVenta no registra de qué lote se vendió originalmente, se usa esta aproximación.
     * 
     * @throws IllegalArgumentException si la venta o producto no existen
     * @throws IllegalStateException si no hay lotes activos del producto
     */
    @Transactional
    public Devolucion registrarDevolucion(Long ventaId, Long productoId, int cantidad, String motivo) {
        // Buscar la venta
        Optional<Venta> ventaOpt = ventaRepository.findById(ventaId);
        if (ventaOpt.isEmpty()) {
            throw new IllegalArgumentException("Venta no encontrada");
        }
        
        Venta venta = ventaOpt.get();
        
        // Buscar el producto
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        if (productoOpt.isEmpty()) {
            throw new IllegalArgumentException("Producto no encontrado");
        }
        
        Producto producto = productoOpt.get();
        
        // ✅ DEVOLVER INVENTARIO A LOTES (Estrategia FIFO - First In, First Out)
        // Buscar lotes ACTIVOS del producto, ordenados por fecha de vencimiento (más antiguo primero)
        List<Lote> lotesActivos = loteRepository.findByProductoAndEstadoOrderByFechaVencimientoAsc(
            producto, 
            EstadoLote.ACTIVO
        );
        
        if (lotesActivos.isEmpty()) {
            throw new IllegalStateException(
                "No hay lotes activos para devolver el inventario del producto: " + producto.getNombre()
            );
        }
        
        // Devolver la cantidad al PRIMER lote activo (FIFO - el más antiguo)
        Lote loteDestino = lotesActivos.get(0);
        int cantidadActual = loteDestino.getCantidadDisponible() != null ? loteDestino.getCantidadDisponible() : 0;
        loteDestino.setCantidadDisponible(cantidadActual + cantidad);
        loteRepository.save(loteDestino);
        
        // Crear la devolución
        Devolucion devolucion = new Devolucion();
        devolucion.setVenta(venta);
        devolucion.setProducto(producto);
        devolucion.setCantidad(cantidad);
        devolucion.setMotivo(motivo);
        devolucion.setFecha(LocalDateTime.now());
        
        // Guardar la devolución
        Devolucion devolucionGuardada = devolucionRepository.save(devolucion);
        
        // ✅ ANULAR VENTA: Cambiar el estado de la venta a ANULADA
        venta.setEstado(EstadoVenta.ANULADA);
        ventaRepository.save(venta);
        
        return devolucionGuardada;
    }

    /**
     * Obtener todas las devoluciones de una venta
     */
    public List<Devolucion> obtenerDevolucionesPorVenta(Long ventaId) {
        return devolucionRepository.findByVentaId(ventaId);
    }

    /**
     * Verificar si una venta tiene devoluciones
     */
    public boolean tieneDevolucion(Long ventaId) {
        return devolucionRepository.existsByVentaId(ventaId);
    }

    /**
     * Obtener todas las devoluciones
     */
    public List<Devolucion> obtenerTodasLasDevoluciones() {
        return devolucionRepository.findAll();
    }

    /**
     * Obtener una devolución por ID
     */
    public Optional<Devolucion> obtenerDevolucionPorId(Long id) {
        return devolucionRepository.findById(id);
    }
}
