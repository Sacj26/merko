package merko.merko.Service;

import merko.merko.Entity.Usuario;
import merko.merko.Repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServicelmpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServicelmpl(UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;

    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(login)
                .or(() -> usuarioRepository.findByCorreo(login))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }

    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
