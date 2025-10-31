package merko.merko.ControllerWeb;

import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistroClienteController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public RegistroClienteController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "/registro-cliente";
    }

    @PostMapping("/registro")
    public String registrarCliente(@ModelAttribute Usuario usuario,
                                   @RequestParam("password") String passPlano) {


        usuario.setRol(Rol.CLIENTE);


        usuario.setPassword(passwordEncoder.encode(passPlano));


        usuarioService.saveUsuario(usuario);

        return "redirect:/login?registroExitoso";
    }
}
