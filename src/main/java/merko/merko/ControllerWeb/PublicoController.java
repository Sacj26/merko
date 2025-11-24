package merko.merko.ControllerWeb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Entity.Venta;
import merko.merko.Repository.CategoriaRepository;
import merko.merko.Repository.ProductBranchRepository;
import merko.merko.Repository.VentaRepository;
import merko.merko.Service.CarritoService;
import merko.merko.Service.ProductoService;
import merko.merko.Service.UsuarioService;
import merko.merko.dto.SessionUser;

@Controller
@RequestMapping("/publico")
public class PublicoController {

    private final ProductoService productoService;
    private final ProductBranchRepository productBranchRepository;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;
    private final VentaRepository ventaRepository;
    
    @Autowired
    private CarritoService carritoService;

    public PublicoController(ProductoService productoService, UsuarioService usuarioService, 
                             ProductBranchRepository productBranchRepository, CategoriaRepository categoriaRepository,
                             VentaRepository ventaRepository) {
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.productBranchRepository = productBranchRepository;
        this.categoriaRepository = categoriaRepository;
        this.ventaRepository = ventaRepository;
    }

    @GetMapping({"/productos", ""})
    public String verProductosPublicos(@RequestParam(value = "q", required = false) String q,
                                       @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                                       @RequestParam(value = "orden", required = false) String orden,
                                       @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                       Model model, HttpSession session) {

        // Configurar paginación: 30 productos por página (estilo grandes industrias)
        int pageSize = 30;
        Sort sort = Sort.unsorted();
        
        // Convertir orden a Sort si aplica
        if (orden != null && !orden.isBlank()) {
            switch (orden) {
                case "precioAsc" -> sort = Sort.by(Sort.Direction.ASC, "precioVenta");
                case "precioDesc" -> sort = Sort.by(Sort.Direction.DESC, "precioVenta");
                case "nombre" -> sort = Sort.by(Sort.Direction.ASC, "nombre");
                default -> {}
            }
        }
        
        Pageable pageRequest = PageRequest.of(page, pageSize, sort);

        // Filtrar productos por categoría si se especifica
        Page<Producto> productosPage = productoService.getProductos(q, null, categoriaId, pageRequest);

        // Calcular stock real de cada producto desde ProductBranch
        java.util.Map<Long, Integer> stockMap = new java.util.HashMap<>();
        for (Producto p : productosPage.getContent()) {
            int stockTotal = productBranchRepository.findByProductoId(p.getId())
                .stream()
                .mapToInt(pb -> pb.getStock() != null ? pb.getStock() : 0)
                .sum();
            stockMap.put(p.getId(), stockTotal);
        }

        model.addAttribute("productosPage", productosPage);
        model.addAttribute("stockMap", stockMap);
        model.addAttribute("q", q);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("orden", orden);
        
        // Cargar categorías desde la tabla categoria
        java.util.List<merko.merko.Entity.Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);

        // user session detection - prefer session-stored SessionUser to avoid DB hits
        SessionUser cliente = null;
        Object obj = session.getAttribute("usuarioLogueado");
        if (obj instanceof SessionUser) {
            cliente = (SessionUser) obj;
            System.out.println("[PRODUCTOS] Usuario desde sesión: " + cliente.getNombre());
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                String identifier = auth.getName();
                
                // Para OAuth2, usar email de sesión si está disponible
                String oauth2Email = (String) session.getAttribute("oauth2Email");
                if (oauth2Email != null) {
                    identifier = oauth2Email;
                    System.out.println("[PRODUCTOS] Usando email OAuth2: " + identifier);
                }
                
                System.out.println("[PRODUCTOS] Buscando usuario: " + identifier);
                
                // Buscar por username primero, luego por email para soportar OAuth2
                Usuario u = usuarioService.findByUsername(identifier);
                if (u == null) {
                    // Intentar buscar por correo (OAuth2 users)
                    Optional<Usuario> opt = usuarioService.findByCorreo(identifier);
                    if (opt.isPresent()) {
                        u = opt.get();
                        System.out.println("[PRODUCTOS] Usuario encontrado por email: " + u.getNombre());
                    }
                } else {
                    System.out.println("[PRODUCTOS] Usuario encontrado por username: " + u.getNombre());
                }
                
                if (u != null && u.getRol() == Rol.CLIENTE) {
                    cliente = new SessionUser(u.getId(), u.getUsername(), u.getNombre(), u.getCorreo(), u.getFotoPerfil(), u.getRol());
                    // cache the lightweight session user to avoid repeated DB queries
                    session.setAttribute("usuarioLogueado", cliente);
                } else if (u != null) {
                    System.err.println("[PRODUCTOS ERROR] Usuario encontrado pero no es CLIENTE: " + u.getRol());
                }
            }
        }
        model.addAttribute("clienteLogueado", cliente);
        
        // Agregar contador del carrito usando CarritoService
        int cartCount = carritoService.contarItems(session);
        model.addAttribute("cartCount", cartCount);

        return "publico/productos";
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            System.err.println("[PERFIL] Usuario no autenticado");
            return "redirect:/login";
        }
        
        // Primero intentar obtener usuario desde la sesión (más eficiente)
        SessionUser sessionUser = (SessionUser) session.getAttribute("usuarioLogueado");
        if (sessionUser != null) {
            System.out.println("[PERFIL] Usuario obtenido desde sesión: " + sessionUser.getNombre());
            Usuario u = usuarioService.findByUsername(sessionUser.getUsername());
            if (u == null) {
                u = usuarioService.findByCorreo(sessionUser.getCorreo()).orElse(null);
            }
            if (u != null) {
                model.addAttribute("usuario", u);
                // Agregar historial de compras del usuario
                List<Venta> comprasUsuario = ventaRepository.findByClienteIdOrderByFechaDesc(u.getId());
                model.addAttribute("compras", comprasUsuario);
                System.out.println("[PERFIL] Usuario tiene " + comprasUsuario.size() + " compras");
                return "publico/perfil";
            }
        }
        
        // Fallback: usar authentication
        String identifier = auth.getName();
        
        // Para OAuth2, verificar si hay email en sesión
        String oauth2Email = (String) session.getAttribute("oauth2Email");
        if (oauth2Email != null) {
            identifier = oauth2Email;
            System.out.println("[PERFIL] Usando email OAuth2 de sesión: " + identifier);
        }
        
        System.out.println("[PERFIL] Buscando usuario: " + identifier);
        System.out.println("[PERFIL] Tipo de autenticación: " + auth.getClass().getName());
        
        // Buscar por username o email para soportar OAuth2
        Usuario u = usuarioService.findByUsername(identifier);
        if (u == null) {
            System.out.println("[PERFIL] No encontrado por username, buscando por correo...");
            // Intentar buscar por correo (OAuth2 users)
            Optional<Usuario> opt = usuarioService.findByCorreo(identifier);
            if (opt.isPresent()) {
                u = opt.get();
                System.out.println("[PERFIL] Usuario encontrado por correo: " + u.getNombre());
            }
        } else {
            System.out.println("[PERFIL] Usuario encontrado por username: " + u.getNombre());
        }
        
        if (u == null) {
            System.err.println("[PERFIL ERROR] Usuario no encontrado en BD: " + identifier);
            System.err.println("[PERFIL ERROR] auth.getName() = " + auth.getName());
            System.err.println("[PERFIL ERROR] sessionUser = " + sessionUser);
            model.addAttribute("error", "Usuario no encontrado. Por favor, inicia sesión nuevamente.");
            return "redirect:/login?error=usernotfound";
        }
        
        model.addAttribute("usuario", u);
        // Agregar historial de compras del usuario
        List<Venta> comprasUsuario = ventaRepository.findByClienteIdOrderByFechaDesc(u.getId());
        model.addAttribute("compras", comprasUsuario);
        System.out.println("[PERFIL] Usuario tiene " + comprasUsuario.size() + " compras");
        return "publico/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute Usuario formUsuario, 
                                   @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                   HttpSession session) {
        System.out.println("[PERFIL UPDATE] Iniciando actualización de perfil");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            System.err.println("[PERFIL UPDATE ERROR] Usuario no autenticado");
            return "redirect:/login";
        }
        
        // Primero intentar desde sesión
        SessionUser sessionUser = (SessionUser) session.getAttribute("usuarioLogueado");
        String identifier = null;
        
        if (sessionUser != null) {
            identifier = sessionUser.getCorreo(); // Usar correo desde sesión
            System.out.println("[PERFIL UPDATE] Usando correo de sesión: " + identifier);
        } else {
            identifier = auth.getName();
            // Para OAuth2, usar email de sesión
            String oauth2Email = (String) session.getAttribute("oauth2Email");
            if (oauth2Email != null) {
                identifier = oauth2Email;
                System.out.println("[PERFIL UPDATE] Usando email OAuth2: " + identifier);
            }
        }
        
        System.out.println("[PERFIL UPDATE] Buscando usuario: " + identifier);
        
        Usuario existente = usuarioService.findByUsername(identifier);
        if (existente == null) {
            System.out.println("[PERFIL UPDATE] No encontrado por username, buscando por correo...");
            existente = usuarioService.findByCorreo(identifier).orElse(null);
        }
        
        if (existente == null) {
            System.err.println("[PERFIL UPDATE ERROR] Usuario no encontrado: " + identifier);
            return "redirect:/login?error=usernotfound";
        }
        
        System.out.println("[PERFIL UPDATE] Usuario encontrado: " + existente.getNombre());

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
                String originalName = avatar.getOriginalFilename();
                String safeFilename = originalName != null ? originalName.replaceAll("[^a-zA-Z0-9._-]", "_") : "avatar";
                String filename = java.util.UUID.randomUUID().toString() + "_" + safeFilename;
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
