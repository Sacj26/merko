package merko.merko.Repository;

import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByUsername(String username);

    List<Usuario> findByRol(Rol rol);
}
