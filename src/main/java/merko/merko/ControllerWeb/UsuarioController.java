package merko.merko.ControllerWeb;

import merko.merko.Entity.Usuario;
import merko.merko.Entity.Rol;
import merko.merko.Service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.getAllUsuarios());
        return "usuarios/list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        usuarioService.saveUsuario(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.getUsuarioById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        return "usuarios/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return "redirect:/usuarios";
    }


    @GetMapping("/clientes")
    public String listarClientes(Model model) {
        model.addAttribute("clientes", usuarioService.getUsuariosByRol(Rol.CLIENTE));
        return "clientes/list";
    }

    @GetMapping("/clientes/nuevo")
    public String mostrarFormularioNuevoCliente(Model model) {
        Usuario cliente = new Usuario();
        cliente.setRol(Rol.CLIENTE);
        model.addAttribute("usuario", cliente);
        return "clientes/form";
    }

    @PostMapping("/clientes/guardar")
    public String guardarCliente(@ModelAttribute Usuario usuario) {
        usuario.setRol(Rol.CLIENTE);
        usuarioService.saveUsuario(usuario);
        return "redirect:/usuarios/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public String mostrarFormularioEditarCliente(@PathVariable Long id, Model model) {
        Usuario cliente = usuarioService.getUsuarioById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        model.addAttribute("usuario", cliente);
        return "clientes/form";
    }

    @GetMapping("/clientes/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return "redirect:/usuarios/clientes";
    }
}
