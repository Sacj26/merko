package merko.merko.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    
    // Alias para OAuth2 (correo = email)
    default Optional<Usuario> findByEmail(String email) {
        return findByCorreo(email);
    }

    Optional<Usuario> findByUsername(String username);
    
    // Query optimizada para login: busca username O correo en una sola consulta
    @Query("SELECT u FROM Usuario u WHERE u.username = :login OR u.correo = :login")
    Optional<Usuario> findByUsernameOrCorreo(@org.springframework.data.repository.query.Param("login") String login);
    
    // Para OAuth2: buscar por Google ID
    Optional<Usuario> findByGoogleId(String googleId);

    List<Usuario> findByRol(Rol rol);

    @Transactional
    @Modifying
    @Query(value = "UPDATE usuario SET rol = UPPER(rol) WHERE rol != UPPER(rol)", nativeQuery = true)
    void fixRolValues();
}
