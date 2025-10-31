package merko.merko.Service;

import java.util.HashMap;
import java.util.Map;

public class CarritoService {


    private Map<Long, Integer> productos = new HashMap<>();

    public void agregarProducto(Long productoId, int cantidad) {
        productos.merge(productoId, cantidad, Integer::sum);
    }

    public int cantidadEnCarrito(Long productoId) {
        return productos.getOrDefault(productoId, 0);
    }

    public Map<Long, Integer> getProductos() {
        return productos;
    }

    public void eliminarProducto(Long productoId) {
        productos.remove(productoId);
    }

    public void vaciarCarrito() {
        productos.clear();
    }
}
