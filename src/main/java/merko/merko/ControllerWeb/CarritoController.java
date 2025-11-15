package merko.merko.ControllerWeb;

import jakarta.servlet.http.HttpSession;
import merko.merko.Entity.*;
import merko.merko.Repository.DetalleVentaRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.VentaRepository;
import merko.merko.dto.CarritoItem;
import merko.merko.dto.SessionUser;
import merko.merko.Service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute("carrito");
        if (carrito == null) carrito = new ArrayList<>();

        double total = carrito.stream().mapToDouble(item -> item.getCantidad() * item.getPrecio()).sum();

        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        return "carrito/ver";
    }

    @PostMapping("/agregar")
    public String agregarAlCarrito(@RequestParam Long productoId,
                                   @RequestParam int cantidad,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/publico/productos";
        }

        @SuppressWarnings("unchecked")
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute("carrito");
        if (carrito == null) carrito = new ArrayList<>();

        int cantidadEnCarrito = carrito.stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .mapToInt(CarritoItem::getCantidad)
                .sum();

        int stockDisponible = producto.getStock() - cantidadEnCarrito;

        if (cantidad > stockDisponible) {
            redirectAttributes.addFlashAttribute("error", "No hay suficiente stock disponible. Stock restante: " + stockDisponible);
            return "redirect:/publico/productos";
        }

        Optional<CarritoItem> existente = carrito.stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + cantidad);
        } else {
            carrito.add(new CarritoItem(productoId, producto.getNombre(), cantidad, producto.getPrecioVenta()));
        }

        session.setAttribute("carrito", carrito);
        redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito.");
        return "redirect:/publico/productos";
    }

    @PostMapping("/eliminar")
    public String eliminarDelCarrito(@RequestParam Long productoId, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute("carrito");
        if (carrito != null) {
            carrito.removeIf(item -> item.getProductoId().equals(productoId));
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/carrito";
    }

    @PostMapping("/vaciar")
    public String vaciarCarrito(HttpSession session) {
        session.removeAttribute("carrito");
        return "redirect:/carrito";
    }

    @PostMapping("/checkout")
    public String finalizarCompra(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute("carrito");

        // Prefer authenticated principal from SecurityContext
        Usuario cliente = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();
            cliente = usuarioService.findByUsername(username);
        }
        // fallback to session-stored SessionUser or legacy clienteLogueado
        if (cliente == null) {
            SessionUser sessionUser = (SessionUser) session.getAttribute("usuarioLogueado");
            if (sessionUser != null) {
                cliente = usuarioService.getUsuarioById(sessionUser.getId()).orElse(null);
            } else {
                Object obj = session.getAttribute("clienteLogueado");
                if (obj instanceof Usuario u) cliente = u;
            }
        }

        if (carrito == null || carrito.isEmpty() || cliente == null || cliente.getRol() != Rol.CLIENTE) {
            return "redirect:/carrito?error=true";
        }

    Venta venta = new Venta();
    venta.setFecha(LocalDateTime.now());
        venta.setCliente(cliente);

        List<DetalleVenta> detalles = new ArrayList<>();
        double totalVenta = 0.0;

        // NOTA: Este bloque duplica lógica de VentaService; considerar refactorizar para usar VentaService.saveVenta
        for (CarritoItem item : carrito) {
            Optional<Producto> productoOpt = productoRepository.findById(item.getProductoId());

            if (productoOpt.isEmpty()) {
                model.addAttribute("error", "Producto no encontrado");
                return "redirect:/carrito?error=true";
            }

            Producto producto = productoOpt.get();

            if (producto.getStock() < item.getCantidad()) {
                model.addAttribute("error", "Stock insuficiente para " + producto.getNombre());
                return "redirect:/carrito?error=true";
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setVenta(venta);

            detalles.add(detalle);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            totalVenta += item.getCantidad() * producto.getPrecioVenta();
        }

        venta.setTotal(totalVenta);
        ventaRepository.save(venta);
        detalleVentaRepository.saveAll(detalles);

        session.removeAttribute("carrito");
        return "redirect:/carrito/confirmacion";
    }

    @GetMapping("/confirmacion")
    public String mostrarConfirmacion() {
        return "carrito/confirmacion";
    }
}
