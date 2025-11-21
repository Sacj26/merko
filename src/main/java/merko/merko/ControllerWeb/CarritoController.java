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
    private merko.merko.Repository.ProductBranchRepository productBranchRepository;

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

        // calcular stock total disponible en todas las sucursales para el producto
        int totalStock = productBranchRepository.findByProductoId(productoId)
                .stream().mapToInt(pb -> pb.getStock() == null ? 0 : pb.getStock()).sum();
        int stockDisponible = totalStock - cantidadEnCarrito;

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

        // Prefer session-stored SessionUser/clienteLogueado to avoid DB hits
        Usuario cliente = null;
        Object suObj = session.getAttribute("usuarioLogueado");
        if (suObj instanceof SessionUser sessionUser) {
            // Use a JPA reference instead of fetching the full entity to avoid
            // triggering a select for simple FK assignments (the reference will
            // supply the id to Hibernate when persisting the Venta).
            cliente = usuarioService.getUsuarioReference(sessionUser.getId());
        } else {
            Object legacy = session.getAttribute("clienteLogueado");
            if (legacy instanceof Usuario u) {
                cliente = u;
            } else {
                // fallback to authenticated principal and load once from DB, then cache in session
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                    String username = auth.getName();
                    cliente = usuarioService.findByUsername(username);
                    if (cliente != null) {
                        SessionUser cached = new SessionUser(cliente.getId(), cliente.getUsername(), cliente.getNombre(), cliente.getCorreo(), cliente.getFotoPerfil(), cliente.getRol());
                        session.setAttribute("usuarioLogueado", cached);
                    }
                }
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


            // Buscar una sucursal que tenga suficiente stock para el producto
            Optional<merko.merko.Entity.ProductBranch> pbOpt = productBranchRepository.findByProductoId(item.getProductoId())
                    .stream().filter(pb -> (pb.getStock() != null ? pb.getStock() : 0) >= item.getCantidad()).findFirst();

            if (pbOpt.isEmpty()) {
                model.addAttribute("error", "Stock insuficiente para " + producto.getNombre());
                return "redirect:/carrito?error=true";
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setVenta(venta);

            detalles.add(detalle);

            // decrementar stock en la sucursal seleccionada
            merko.merko.Entity.ProductBranch targetPb = pbOpt.get();
            int cur = targetPb.getStock() == null ? 0 : targetPb.getStock();
            targetPb.setStock(cur - item.getCantidad());
            productBranchRepository.save(targetPb);

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
