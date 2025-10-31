package merko.merko.Service;

import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public Usuario saveUsuario(Usuario usuario) {

        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario no puede estar vacío");
        }

        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        // Ensure password is encoded before saving. If it's already encoded (startsWith $2a$ or $2b$), assume it's ok.
        String raw = usuario.getPassword();
        if (!(raw.startsWith("$2a$") || raw.startsWith("$2b$") || raw.startsWith("$2y$"))) {
            usuario.setPassword(passwordEncoder.encode(raw));
        }
        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deleteUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario con id " + id + " no existe");
        }
        usuarioRepository.deleteById(id);
    }

    public Optional<Optional<Usuario>> findByCorreo(String correo) {
        return Optional.ofNullable(usuarioRepository.findByCorreo(correo));
    }

    public List<Usuario> getUsuariosByRol(Rol rol) {
        return usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getRol() == rol)
                .collect(Collectors.toList());
    }

    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}
