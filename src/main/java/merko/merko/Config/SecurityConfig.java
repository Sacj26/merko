package merko.merko.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import merko.merko.Entity.Usuario;
import merko.merko.Service.UserDetailsServicelmpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServicelmpl userDetailsService;

    public SecurityConfig(UserDetailsServicelmpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Recursos públicos y páginas sin autenticación
                        .requestMatchers("/", "/login", "/error", "/publico/**", "/css/**", "/js/**", "/images/**", "/img/**", "/uploads/**", "/registro/**").permitAll()

                        // Sólo accesible para administradores
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")

                        // Rutas para clientes
                        .requestMatchers("/cliente/**", "/carrito/**").hasRole("CLIENTE")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(successHandler())
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error")
                )

                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
            boolean isCliente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

            if (isAdmin) {
                Usuario admin = userDetailsService.findByUsername(authentication.getName());
                // store a lightweight DTO in session to avoid keeping password or JPA proxies
                merko.merko.dto.SessionUser sessionUser = new merko.merko.dto.SessionUser(
                        admin.getId(), admin.getUsername(), admin.getNombre(), admin.getCorreo(), admin.getRol()
                );
                request.getSession().setAttribute("usuarioLogueado", sessionUser);
                response.sendRedirect("/admin");
            } else if (isCliente) {
                Usuario cliente = userDetailsService.findByUsername(authentication.getName());
                merko.merko.dto.SessionUser sessionUser = new merko.merko.dto.SessionUser(
                        cliente.getId(), cliente.getUsername(), cliente.getNombre(), cliente.getCorreo(), cliente.getRol()
                );
                request.getSession().setAttribute("usuarioLogueado", sessionUser);
                response.sendRedirect("/publico/productos");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
