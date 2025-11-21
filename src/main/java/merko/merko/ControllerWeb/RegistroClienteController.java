package merko.merko.ControllerWeb;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Service.UserDetailsServicelmpl;
import merko.merko.Service.UsuarioService;
import merko.merko.dto.RegistroDTO;

@Controller
public class RegistroClienteController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;

    public RegistroClienteController(UsuarioService usuarioService,
                                     AuthenticationManager authenticationManager) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("registroDTO", new RegistroDTO());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrarCliente(@ModelAttribute("registroDTO") @Valid RegistroDTO registroDTO,
                                   BindingResult bindingResult,
                                   HttpServletRequest request) {

        // basic cross-field validations
        if (!registroDTO.getCorreo().equals(registroDTO.getConfirmarCorreo())) {
            bindingResult.addError(new FieldError("registroDTO", "confirmarCorreo", "Los correos no coinciden"));
        }
        if (!registroDTO.getPassword().equals(registroDTO.getConfirmarPassword())) {
            bindingResult.addError(new FieldError("registroDTO", "confirmarPassword", "Las contraseñas no coinciden"));
        }

        if (bindingResult.hasErrors()) {
            return "auth/registro";
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(registroDTO.getUsername());
        usuario.setCorreo(registroDTO.getCorreo());
        // set raw password -> UsuarioService will encode if needed
        usuario.setPassword(registroDTO.getPassword());
        usuario.setRol(Rol.CLIENTE);
        usuario.setNombre(registroDTO.getNombre());
        usuario.setTelefono(registroDTO.getTelefono());

        usuarioService.saveUsuario(usuario);

        // Auto-login: authenticate with raw password and populate session like successHandler
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(usuario.getUsername(), registroDTO.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // OPTIMIZACIÓN: Obtener Usuario desde CustomUserDetails sin query adicional a BD
            UserDetailsServicelmpl.CustomUserDetails userDetails = 
                    (UserDetailsServicelmpl.CustomUserDetails) authentication.getPrincipal();
            merko.merko.Entity.Usuario cliente = userDetails.getUsuario();
            
            merko.merko.dto.SessionUser sessionUser = new merko.merko.dto.SessionUser(
                    cliente.getId(), cliente.getUsername(), cliente.getNombre(), cliente.getCorreo(), cliente.getRol()
            );
            request.getSession().setAttribute("usuarioLogueado", sessionUser);

            return "redirect:/publico/productos";
        } catch (Exception ex) {
            return "redirect:/login?registroExitoso";
        }
    }
}
