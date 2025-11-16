package merko.merko.ControllerWeb;

import merko.merko.Service.UsuarioService;
import merko.merko.dto.SessionUser;
import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UsuarioService usuarioService;

    public GlobalModelAttributes(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("clienteLogueado")
    public SessionUser addClienteLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String username = auth.getName();
        Usuario u = usuarioService.findByUsername(username);
        if (u == null) return null;
        if (u.getRol() != Rol.CLIENTE) return null;
        return new SessionUser(u.getId(), u.getUsername(), u.getNombre(), u.getCorreo(), u.getFotoPerfil(), u.getRol());
    }
}
