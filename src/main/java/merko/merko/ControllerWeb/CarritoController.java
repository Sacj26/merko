package merko.merko.ControllerWeb;

import jakarta.servlet.http.HttpSession;
import merko.merko.Entity.*;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.ProductBranchRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.UsuarioRepository;
import merko.merko.Repository.VentaRepository;
import merko.merko.Service.CarritoService;
import merko.merko.dto.CarritoItem;
import merko.merko.dto.SessionUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private ProductBranchRepository productBranchRepository;

    /**
     * Ver carrito
     */
    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        System.out.println("\n========================================");
        System.out.println("[CARRITO] INICIO - Ver carrito");
        System.out.println("========================================");
        
        try {
            // 1. Verificar sesión
            System.out.println("[CARRITO] Session ID: " + session.getId());
            
            // 2. Obtener usuario de sesión
            SessionUser clienteLogueado = (SessionUser) session.getAttribute("usuarioLogueado");
            System.out.println("[CARRITO] Usuario en sesión: " + (clienteLogueado != null ? clienteLogueado.getNombre() : "null"));
            model.addAttribute("clienteLogueado", clienteLogueado);

            // 3. Obtener carrito
            System.out.println("[CARRITO] Obteniendo carrito...");
            List<CarritoItem> carrito = carritoService.obtenerCarrito(session);
            System.out.println("[CARRITO] Carrito obtenido: " + (carrito != null ? carrito.size() + " items" : "null"));
            
            // 4. Calcular totales
            System.out.println("[CARRITO] Calculando total...");
            double total = carritoService.calcularTotal(session);
            System.out.println("[CARRITO] Total calculado: $" + total);
            
            System.out.println("[CARRITO] Contando items...");
            int cartCount = carritoService.contarItems(session);
            System.out.println("[CARRITO] Items contados: " + cartCount);

            // 5. Agregar al modelo
            System.out.println("[CARRITO] Agregando atributos al modelo...");
            model.addAttribute("carrito", carrito);
            model.addAttribute("total", total);
            model.addAttribute("cartCount", cartCount);

            System.out.println("[CARRITO] ✅ TODO OK - Retornando vista carrito/ver");
            System.out.println("========================================\n");

            return "carrito/ver";

        } catch (Exception e) {
            System.err.println("\n❌❌❌ [CARRITO ERROR CRÍTICO] ❌❌❌");
            System.err.println("[CARRITO ERROR] Tipo: " + e.getClass().getName());
            System.err.println("[CARRITO ERROR] Mensaje: " + e.getMessage());
            System.err.println("[CARRITO ERROR] Stack trace:");
            e.printStackTrace();
            System.err.println("❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌\n");
            
            model.addAttribute("mensaje", "Error al cargar el carrito: " + e.getMessage());
            return "error/error";
        }
    }

    /**
     * Agregar producto al carrito
     */
    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam Long productoId,
                                  @RequestParam(defaultValue = "1") int cantidad,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            System.out.println("[CARRITO] Agregando producto ID: " + productoId + ", Cantidad: " + cantidad);

            boolean exito = carritoService.agregarProducto(session, productoId, cantidad);

            if (exito) {
                redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito");
            } else {
                redirectAttributes.addFlashAttribute("error", "Producto sin stock disponible o no encontrado");
            }

        } catch (Exception e) {
            System.err.println("[CARRITO ERROR] Error al agregar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al agregar producto");
        }

        return "redirect:/publico/productos";
    }

    /**
     * Actualizar cantidad de un producto
     */
    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam Long productoId,
                                     @RequestParam int cantidad,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            System.out.println("[CARRITO] Actualizando producto ID: " + productoId + " a cantidad: " + cantidad);

            boolean exito = carritoService.actualizarCantidad(session, productoId, cantidad);

            if (exito) {
                redirectAttributes.addFlashAttribute("success", "Cantidad actualizada");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo actualizar la cantidad");
            }

        } catch (Exception e) {
            System.err.println("[CARRITO ERROR] Error al actualizar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al actualizar cantidad");
        }

        return "redirect:/carrito";
    }

    /**
     * Eliminar producto del carrito
     */
    @PostMapping("/eliminar")
    public String eliminarProducto(@RequestParam Long productoId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            System.out.println("[CARRITO] Eliminando producto ID: " + productoId);

            boolean exito = carritoService.eliminarProducto(session, productoId);

            if (exito) {
                redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el producto");
            }

        } catch (Exception e) {
            System.err.println("[CARRITO ERROR] Error al eliminar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto");
        }

        return "redirect:/carrito";
    }

    /**
     * Vaciar carrito completamente
     */
    @PostMapping("/vaciar")
    public String vaciarCarrito(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("[CARRITO] Vaciando carrito");
            carritoService.vaciarCarrito(session);
            redirectAttributes.addFlashAttribute("success", "Carrito vaciado");

        } catch (Exception e) {
            System.err.println("[CARRITO ERROR] Error al vaciar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al vaciar el carrito");
        }

        return "redirect:/carrito";
    }

    /**
     * API: Obtener cantidad de items en el carrito (JSON)
     */
    @GetMapping("/api/count")
    @ResponseBody
    public java.util.Map<String, Integer> getCartCount(HttpSession session) {
        int count = carritoService.contarItems(session);
        return java.util.Collections.singletonMap("count", count);
    }
    
    /**
     * API: Verificar stock disponible de un producto (JSON)
     */
    @GetMapping("/api/stock/{productoId}")
    @ResponseBody
    public java.util.Map<String, Object> checkStock(@PathVariable Long productoId) {
        boolean hasStock = carritoService.tieneStock(productoId);
        return java.util.Map.of(
            "hasStock", hasStock,
            "message", hasStock ? "Stock disponible" : "Sin stock"
        );
    }
    
    /**
     * Finalizar compra - Crear venta desde el carrito
     */
    @PostMapping("/finalizar")
    public String finalizarCompra(HttpSession session, 
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        System.out.println("\n========================================");
        System.out.println("[COMPRA] INICIO - Finalizando compra");
        System.out.println("========================================");
        
        try {
            // 1. Verificar que el cliente esté logueado
            SessionUser clienteLogueado = (SessionUser) session.getAttribute("usuarioLogueado");
            if (clienteLogueado == null) {
                System.out.println("[COMPRA] ❌ Cliente no logueado");
                redirectAttributes.addFlashAttribute("error", "Debe iniciar sesión para realizar la compra");
                return "redirect:/auth/login";
            }
            System.out.println("[COMPRA] Cliente: " + clienteLogueado.getNombre());
            
            // 2. Obtener carrito
            List<CarritoItem> carrito = carritoService.obtenerCarrito(session);
            if (carrito == null || carrito.isEmpty()) {
                System.out.println("[COMPRA] ❌ Carrito vacío");
                redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
                return "redirect:/carrito";
            }
            System.out.println("[COMPRA] Carrito con " + carrito.size() + " items");
            
            // 3. Obtener cliente desde BD
            Usuario cliente = usuarioRepository.findById(clienteLogueado.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            System.out.println("[COMPRA] Cliente BD: " + cliente.getNombre());
            
            // 4. Seleccionar sucursal automáticamente (primera disponible)
            List<Branch> sucursales = branchRepository.findAll();
            if (sucursales.isEmpty()) {
                System.out.println("[COMPRA] ❌ No hay sucursales disponibles");
                redirectAttributes.addFlashAttribute("error", "No hay sucursales disponibles");
                return "redirect:/carrito";
            }
            Branch sucursal = sucursales.get(0); // Tomar la primera sucursal
            System.out.println("[COMPRA] Sucursal seleccionada: " + sucursal.getNombre());
            
            // 5. Crear venta
            Venta venta = new Venta();
            venta.setCliente(cliente);
            venta.setBranch(sucursal);
            venta.setFecha(LocalDateTime.now());
            venta.setEstado(EstadoVenta.ACTIVA);
            venta.setChannel("WEB");
            venta.setDiscountAmount(0.0);
            
            // 6. Crear detalles de venta desde carrito
            List<DetalleVenta> detalles = new ArrayList<>();
            for (CarritoItem item : carrito) {
                Producto producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.getProductoId()));
                
                DetalleVenta detalle = new DetalleVenta();
                detalle.setProducto(producto);
                detalle.setCantidad(item.getCantidad());
                detalle.setPrecioUnitario(item.getPrecio());
                detalle.setVenta(venta);
                detalles.add(detalle);
                
                System.out.println("[COMPRA] Detalle: " + producto.getNombre() + " x" + item.getCantidad());
            }
            venta.setDetalles(detalles);
            
            // 7. Calcular total y actualizar stock por sucursal
            double totalVenta = 0.0;
            for (DetalleVenta detalle : detalles) {
                Producto producto = detalle.getProducto();
                
                // Buscar ProductBranch para esta sucursal y producto
                ProductBranch productBranch = productBranchRepository
                    .findByProductoIdAndBranchId(producto.getId(), sucursal.getId())
                    .orElse(null);
                
                if (productBranch != null) {
                    // Validar stock disponible
                    int stockActual = productBranch.getStock() != null ? productBranch.getStock() : 0;
                    if (stockActual < detalle.getCantidad()) {
                        throw new IllegalArgumentException("Stock insuficiente en sucursal para: " + producto.getNombre() + 
                                                         " (Disponible: " + stockActual + ", Solicitado: " + detalle.getCantidad() + ")");
                    }
                    
                    // Decrementar stock
                    productBranch.setStock(stockActual - detalle.getCantidad());
                    productBranchRepository.save(productBranch);
                    
                    System.out.println("[COMPRA] Stock actualizado - " + producto.getNombre() + 
                                     " | Anterior: " + stockActual + 
                                     " | Vendido: " + detalle.getCantidad() + 
                                     " | Nuevo: " + productBranch.getStock());
                } else {
                    System.out.println("[COMPRA] ⚠️ Advertencia: Producto sin stock en sucursal: " + producto.getNombre());
                }
                
                // Calcular subtotal
                double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
                totalVenta += subtotal;
                
                System.out.println("[COMPRA] Producto: " + producto.getNombre() + 
                                 " | Cantidad: " + detalle.getCantidad() + 
                                 " | Precio: $" + detalle.getPrecioUnitario() + 
                                 " | Subtotal: $" + subtotal);
            }
            
            venta.setTotal(totalVenta);
            System.out.println("[COMPRA] Total de la venta: $" + totalVenta);
            
            // 8. Guardar venta
            System.out.println("[COMPRA] Guardando venta...");
            Venta ventaGuardada = ventaRepository.save(venta);
            System.out.println("[COMPRA] ✅ Venta guardada con ID: " + ventaGuardada.getId());
            
            // 9. Vaciar carrito
            carritoService.vaciarCarrito(session);
            System.out.println("[COMPRA] ✅ Carrito vaciado");
            
            // 10. Redirigir a confirmación
            System.out.println("[COMPRA] ✅ Compra finalizada exitosamente");
            System.out.println("========================================\n");
            
            redirectAttributes.addFlashAttribute("success", "¡Compra realizada exitosamente!");
            redirectAttributes.addFlashAttribute("ventaId", ventaGuardada.getId());
            redirectAttributes.addFlashAttribute("total", ventaGuardada.getTotal());
            return "redirect:/carrito/confirmacion";
            
        } catch (IllegalArgumentException e) {
            System.err.println("[COMPRA] ❌ Error de validación: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrito";
            
        } catch (Exception e) {
            System.err.println("[COMPRA] ❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar la compra: " + e.getMessage());
            return "redirect:/carrito";
        }
    }
    
    /**
     * Página de confirmación de compra
     */
    @GetMapping("/confirmacion")
    public String confirmacion(HttpSession session, Model model) {
        SessionUser clienteLogueado = (SessionUser) session.getAttribute("usuarioLogueado");
        model.addAttribute("clienteLogueado", clienteLogueado);
        return "carrito/confirmacion-nueva";
    }
}
