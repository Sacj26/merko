package merko.merko.ControllerWeb;

import jakarta.servlet.http.HttpSession;
import merko.merko.Entity.Rol;
import merko.merko.Service.ProductoService;
import merko.merko.Service.UsuarioService;
import merko.merko.Entity.Usuario;
import merko.merko.dto.SessionUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/publico")
public class PublicoController {

    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    public PublicoController(ProductoService productoService, UsuarioService usuarioService) {
        this.productoService = productoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/productos")
    public String verProductosPublicos(Model model, HttpSession session) {
        model.addAttribute("productos", productoService.getAllProductos());


        SessionUser cliente = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();
            Usuario u = usuarioService.findByUsername(username);
            if (u != null && u.getRol() == Rol.CLIENTE) {
                cliente = new SessionUser(u.getId(), u.getUsername(), u.getNombre(), u.getCorreo(), u.getRol());
            }
        }
        // fallback: keep existing session attribute if present (older flows)
        if (cliente == null) {
            Object obj = session.getAttribute("usuarioLogueado");
            if (obj instanceof SessionUser) cliente = (SessionUser) obj;
        }
        model.addAttribute("clienteLogueado", cliente);

        return "publico/productos";
    }


}
