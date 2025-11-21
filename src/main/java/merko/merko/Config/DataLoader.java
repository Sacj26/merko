package merko.merko.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Repository.UsuarioRepository;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadInitialUsers(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
                System.out.println("=== CARGANDO USUARIOS INICIALES ===");

                // Ensure default admin + demo client exist, but do not overwrite existing users.
                if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setCorreo("admin@merko.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRol(Rol.ADMIN);
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setActivo(true);
                admin.setNotificaciones(false);
                admin.setFechaCreacion(java.time.LocalDateTime.now());
                usuarioRepository.save(admin);
                System.out.println("Usuario admin creado");
                }

                if (usuarioRepository.findByUsername("cliente").isEmpty()) {
                Usuario cliente = new Usuario();
                cliente.setUsername("cliente");
                cliente.setCorreo("cliente@merko.com");
                cliente.setPassword(passwordEncoder.encode("cliente123"));
                cliente.setRol(Rol.CLIENTE);
                cliente.setNombre("Cliente");
                cliente.setApellido("Prueba");
                cliente.setActivo(true);
                cliente.setNotificaciones(false);
                cliente.setFechaCreacion(java.time.LocalDateTime.now());
                usuarioRepository.save(cliente);
                System.out.println("Usuario cliente creado");
                }
        };
    }
}
