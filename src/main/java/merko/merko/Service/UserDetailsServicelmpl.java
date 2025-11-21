package merko.merko.Service;

import merko.merko.Entity.Usuario;
import merko.merko.Repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Service
public class UserDetailsServicelmpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServicelmpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // OPTIMIZACIÓN: Una sola query que busca username O correo
        Usuario usuario = usuarioRepository.findByUsernameOrCorreo(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        // UserDetails personalizado que mantiene referencia al Usuario entity
        return new CustomUserDetails(usuario);
    }
    
    /**
     * UserDetails personalizado que almacena el Usuario completo
     * Esto permite acceder al Usuario en el successHandler sin query adicional
     */
    public static class CustomUserDetails implements UserDetails {
        private final Usuario usuario;
        
        public CustomUserDetails(Usuario usuario) {
            this.usuario = usuario;
        }
        
        public Usuario getUsuario() {
            return usuario;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        }
        
        @Override
        public String getPassword() {
            return usuario.getPassword();
        }
        
        @Override
        public String getUsername() {
            return usuario.getUsername();
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
