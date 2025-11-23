package merko.merko.Config;

import org.springframework.beans.factory.annotation.Autowired;
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
import merko.merko.Repository.UsuarioRepository;
import merko.merko.Service.CustomOAuth2UserService;
import merko.merko.Service.UserDetailsServicelmpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Recursos públicos y páginas sin autenticación
                        .requestMatchers("/", "/login", "/error", "/publico/**", "/css/**", "/js/**", "/images/**", "/img/**", "/uploads/**", "/registro/**").permitAll()

                        // Sólo accesible para administradores
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Rutas para clientes - carrito accesible para cualquier usuario autenticado
                        .requestMatchers("/cliente/**").hasRole("CLIENTE")
                        .requestMatchers("/carrito/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(successHandler())
                )
                // Configuración OAuth2 para Google
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oauth2SuccessHandler())
                        .failureUrl("/login?error=oauth2")
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error")
                )
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
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

    /**
     * Success handler para OAuth2 (Google)
     */
    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            // Obtener información del usuario OAuth2
            org.springframework.security.oauth2.core.user.OAuth2User oauth2User = 
                    (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
            
            String email = oauth2User.getAttribute("email");
            String googleId = oauth2User.getAttribute("sub");
            
            System.out.println("[OAUTH2 LOGIN] Google ID: " + googleId);
            System.out.println("[OAUTH2 LOGIN] Email: " + email);
            System.out.println("[OAUTH2 LOGIN] auth.getName(): " + authentication.getName());
            
            // Buscar usuario en BD por email (OAuth2 siempre usa email)
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario OAuth2 no encontrado en BD: " + email));
            
            System.out.println("[OAUTH2 LOGIN] Usuario encontrado: " + usuario.getNombre() + " (ID: " + usuario.getId() + ")");
            
            // IMPORTANTE: Guardar el EMAIL en la sesión como identificador principal
            // Esto hace que auth.getName() devuelva el email en lugar del Google ID
            merko.merko.dto.SessionUser sessionUser = new merko.merko.dto.SessionUser(
                    usuario.getId(), 
                    email,  // Usar email como username para consistencia
                    usuario.getNombre(), 
                    usuario.getCorreo(), 
                    usuario.getFotoPerfil(),
                    usuario.getRol()
            );
            request.getSession().setAttribute("usuarioLogueado", sessionUser);
            
            // CRÍTICO: También guardamos el email como atributo para que auth.getName() lo use
            request.getSession().setAttribute("oauth2Email", email);
            
            // Redirigir según el rol
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (isAdmin) {
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/publico/productos");
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
