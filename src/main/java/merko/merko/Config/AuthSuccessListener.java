package merko.merko.Config;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import merko.merko.Service.UserDetailsServicelmpl;
import merko.merko.Service.UsuarioService;
import merko.merko.Entity.Usuario;

import java.time.LocalDateTime;

@Component
public class AuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserDetailsServicelmpl userDetailsService;
    private final UsuarioService usuarioService;

    public AuthSuccessListener(UserDetailsServicelmpl userDetailsService, UsuarioService usuarioService) {
        this.userDetailsService = userDetailsService;
        this.usuarioService = usuarioService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        try {
            String username = event.getAuthentication().getName();
            Usuario u = userDetailsService.findByUsername(username);
            if (u != null) {
                u.setUltimoLogin(LocalDateTime.now());
                usuarioService.saveUsuario(u);
            }
        } catch (Exception ex) {
            // log if desired; avoid throwing to not break auth flow
        }
    }
}
