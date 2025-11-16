package merko.merko.ControllerWeb;

import jakarta.servlet.http.HttpSession;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Service.ProductoService;
import merko.merko.Service.UsuarioService;
import merko.merko.dto.SessionUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/publico")
public class PublicoController {

    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    public PublicoController(ProductoService productoService, UsuarioService usuarioService) {
        this.productoService = productoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping({"/productos", ""})
    public String verProductosPublicos(@RequestParam(value = "q", required = false) String q,
                                       @RequestParam(value = "categoria", required = false) String categoria,
                                       @RequestParam(value = "orden", required = false) String orden,
                                       Model model, HttpSession session, Pageable pageable) {

        // Convertir orden a Sort si aplica
        Pageable pageRequest = pageable;
        if (orden != null && !orden.isBlank()) {
            Sort sort = Sort.unsorted();
            switch (orden) {
                case "precioAsc" -> sort = Sort.by(Sort.Direction.ASC, "precioVenta");
                case "precioDesc" -> sort = Sort.by(Sort.Direction.DESC, "precioVenta");
                default -> {}
            }
            pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }

        Page<Producto> productosPage = productoService.getProductos(q, null, pageRequest);

        model.addAttribute("productosPage", productosPage);
        model.addAttribute("q", q);
        model.addAttribute("categoria", categoria);
        model.addAttribute("orden", orden);

        // user session detection
        SessionUser cliente = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();
            Usuario u = usuarioService.findByUsername(username);
                if (u != null && u.getRol() == Rol.CLIENTE) {
                cliente = new SessionUser(u.getId(), u.getUsername(), u.getNombre(), u.getCorreo(), u.getFotoPerfil(), u.getRol());
            }
        }
        if (cliente == null) {
            Object obj = session.getAttribute("usuarioLogueado");
            if (obj instanceof SessionUser) cliente = (SessionUser) obj;
        }
        model.addAttribute("clienteLogueado", cliente);

        return "publico/productos";
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        String username = auth.getName();
        Usuario u = usuarioService.findByUsername(username);
        if (u == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", u);
        return "publico/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute Usuario formUsuario, @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        String username = auth.getName();
        Usuario existente = usuarioService.findByUsername(username);
        if (existente == null) return "redirect:/login";

        // Actualizar campos permitidos (no sobrescribir contraseña si no se provee)
        existente.setNombre(formUsuario.getNombre());
        existente.setApellido(formUsuario.getApellido());
        existente.setTelefono(formUsuario.getTelefono());
        existente.setDireccion(formUsuario.getDireccion());
        // handle avatar upload
        if (avatar != null && !avatar.isEmpty()) {
            try {
                Path uploadDir = Paths.get("src/main/resources/static/uploads/avatars");
                if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
                String filename = java.util.UUID.randomUUID().toString() + "_" + avatar.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
                Path target = uploadDir.resolve(filename);
                avatar.transferTo(target.toFile());
                String webPath = "/uploads/avatars/" + filename;
                existente.setFotoPerfil(webPath);
            } catch (IOException ex) {
                // log and continue
                System.err.println("Error saving avatar: " + ex.getMessage());
            }
        } else {
            existente.setFotoPerfil(formUsuario.getFotoPerfil());
        }

        // conservar username/correo/rol/activo; asegurarnos de no limpiar la contraseña
        usuarioService.saveUsuario(existente);

        return "redirect:/publico/perfil?success";
    }

}
