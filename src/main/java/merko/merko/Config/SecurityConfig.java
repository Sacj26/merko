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
    // UserDetailsService se usa automáticamente por Spring Security, no necesita inyección aquí

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Recursos públicos y páginas sin autenticación
                        .requestMatchers("/", "/login", "/error", "/publico/**", "/css/**", "/js/**", "/images/**", "/img/**", "/uploads/**", "/registro/**").permitAll()

                        // Sólo accesible para administradores
                        .requestMatchers("/admin/**").hasRole("ADMIN")

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
            // OPTIMIZACIÓN: Obtener Usuario desde CustomUserDetails sin query adicional a BD
            UserDetailsServicelmpl.CustomUserDetails userDetails = 
                    (UserDetailsServicelmpl.CustomUserDetails) authentication.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isCliente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

            if (isAdmin) {
                // store a lightweight DTO in session to avoid keeping password or JPA proxies
                merko.merko.dto.SessionUser sessionUser = new merko.merko.dto.SessionUser(
                        usuario.getId(), usuario.getUsername(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol()
                );
                request.getSession().setAttribute("usuarioLogueado", sessionUser);
                response.sendRedirect("/admin");
            } else if (isCliente) {
                merko.merko.dto.SessionUser sessionUser = new merko.merko.dto.SessionUser(
                        usuario.getId(), usuario.getUsername(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol()
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
        // Strength 8 es más rápido que 10 (default) pero sigue siendo seguro
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
