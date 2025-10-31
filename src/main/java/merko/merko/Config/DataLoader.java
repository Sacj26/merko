package merko.merko.Config;

import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadInitialUsers(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario(
                        "admin",
                        "admin@merko.com",
                        passwordEncoder.encode("admin123"),
                        Rol.ADMINISTRADOR
                );
                admin.setNombre("Administrador");
                usuarioRepository.save(admin);

                Usuario cliente = new Usuario(
                        "cliente",
                        "cliente@merko.com",
                        passwordEncoder.encode("cliente123"),
                        Rol.CLIENTE
                );
                cliente.setNombre("Cliente de prueba");
                usuarioRepository.save(cliente);

                System.out.println("Usuarios por defecto creados: admin y cliente");
            }
        };
    }
}
