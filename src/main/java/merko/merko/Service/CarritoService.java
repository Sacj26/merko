package merko.merko.Service;

import jakarta.servlet.http.HttpSession;
import merko.merko.dto.CarritoItem;
import merko.merko.Entity.Producto;
import merko.merko.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private merko.merko.Repository.ProductBranchRepository productBranchRepository;

    private static final String CARRITO_SESSION_KEY = "carrito";

    /**
     * Obtiene el carrito de la sesión actual
     */
    public List<CarritoItem> obtenerCarrito(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute(CARRITO_SESSION_KEY);
        
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        
        return carrito;
    }

    /**
     * Agrega un producto al carrito
     */
    public boolean agregarProducto(HttpSession session, Long productoId, int cantidad) {
        try {
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                System.err.println("[CARRITO SERVICE] Producto no encontrado: " + productoId);
                return false;
            }

            Producto producto = productoOpt.get();
            
            // Verificar stock disponible en todas las sucursales
            int stockTotal = obtenerStockTotal(productoId);
            if (stockTotal <= 0) {
                System.err.println("[CARRITO SERVICE] Producto sin stock: " + producto.getNombre());
                return false;
            }
            List<CarritoItem> carrito = obtenerCarrito(session);

            // Buscar si el producto ya está en el carrito
            Optional<CarritoItem> itemExistente = carrito.stream()
                    .filter(item -> item.getProductoId().equals(productoId))
                    .findFirst();

            if (itemExistente.isPresent()) {
                // Actualizar cantidad
                CarritoItem item = itemExistente.get();
                item.setCantidad(item.getCantidad() + cantidad);
                System.out.println("[CARRITO SERVICE] Cantidad actualizada: " + producto.getNombre() + " -> " + item.getCantidad());
            } else {
                // Agregar nuevo item
                CarritoItem nuevoItem = new CarritoItem(
                        producto.getId(),
                        producto.getNombre(),
                        cantidad,
                        producto.getPrecioVenta()
                );
                carrito.add(nuevoItem);
                System.out.println("[CARRITO SERVICE] Producto agregado: " + producto.getNombre());
            }

            session.setAttribute(CARRITO_SESSION_KEY, carrito);
            return true;

        } catch (Exception e) {
            System.err.println("[CARRITO SERVICE ERROR] Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    public boolean actualizarCantidad(HttpSession session, Long productoId, int nuevaCantidad) {
        try {
            List<CarritoItem> carrito = obtenerCarrito(session);
            
            Optional<CarritoItem> itemOpt = carrito.stream()
                    .filter(item -> item.getProductoId().equals(productoId))
                    .findFirst();

            if (itemOpt.isPresent()) {
                if (nuevaCantidad <= 0) {
                    carrito.remove(itemOpt.get());
                    System.out.println("[CARRITO SERVICE] Producto eliminado: " + productoId);
                } else {
                    itemOpt.get().setCantidad(nuevaCantidad);
                    System.out.println("[CARRITO SERVICE] Cantidad actualizada: " + productoId + " -> " + nuevaCantidad);
                }
                session.setAttribute(CARRITO_SESSION_KEY, carrito);
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("[CARRITO SERVICE ERROR] Error al actualizar cantidad: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto del carrito
     */
    public boolean eliminarProducto(HttpSession session, Long productoId) {
        try {
            List<CarritoItem> carrito = obtenerCarrito(session);
            boolean eliminado = carrito.removeIf(item -> item.getProductoId().equals(productoId));
            
            if (eliminado) {
                session.setAttribute(CARRITO_SESSION_KEY, carrito);
                System.out.println("[CARRITO SERVICE] Producto eliminado del carrito: " + productoId);
            }
            
            return eliminado;

        } catch (Exception e) {
            System.err.println("[CARRITO SERVICE ERROR] Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vacía el carrito completamente
     */
    public void vaciarCarrito(HttpSession session) {
        session.setAttribute(CARRITO_SESSION_KEY, new ArrayList<CarritoItem>());
        System.out.println("[CARRITO SERVICE] Carrito vaciado");
    }

    /**
     * Calcula el total del carrito
     */
    public double calcularTotal(HttpSession session) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        return carrito.stream()
                .mapToDouble(item -> item.getCantidad() * item.getPrecio())
                .sum();
    }

    /**
     * Obtiene la cantidad de items en el carrito
     */
    public int contarItems(HttpSession session) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        return carrito.stream()
                .mapToInt(CarritoItem::getCantidad)
                .sum();
    }
    
    /**
     * Obtiene el stock total de un producto sumando todas las sucursales
     */
    private int obtenerStockTotal(Long productoId) {
        List<merko.merko.Entity.ProductBranch> branches = productBranchRepository.findByProductoId(productoId);
        return branches.stream()
                .mapToInt(pb -> pb.getStock() != null ? pb.getStock() : 0)
                .sum();
    }
    
    /**
     * Verifica si un producto tiene stock disponible (público para API)
     */
    public boolean tieneStock(Long productoId) {
        return obtenerStockTotal(productoId) > 0;
    }
}
