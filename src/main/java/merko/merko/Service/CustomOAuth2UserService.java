package merko.merko.Service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Repository.UsuarioRepository;

/**
 * Servicio personalizado para manejar usuarios que se autentican con Google OAuth2
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioRepository usuarioRepository;

    public CustomOAuth2UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Obtener información del usuario desde Google
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Procesar y guardar/actualizar el usuario
        Usuario usuario = processOAuth2User(oauth2User);
        
        // Crear OAuth2User personalizado con las authorities correctas
        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                java.util.Collections.singleton(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
                ),
                oauth2User.getAttributes(),
                "sub"  // El ID principal es "sub" (Google ID)
        );
    }

    private Usuario processOAuth2User(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // Extraer información de Google (solo lo importante para el modelo Usuario)
        String email = (String) attributes.get("email");
        String googleId = (String) attributes.get("sub");
        String picture = (String) attributes.get("picture");
        
        // Separar nombre y apellido
        String givenName = (String) attributes.get("given_name");  // Primer nombre
        String familyName = (String) attributes.get("family_name"); // Apellido
        
        // Verificar si el usuario ya existe
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElse(null);
        
        if (usuario == null) {
            // Crear nuevo usuario con rol CLIENTE (no ADMIN)
            usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setCorreo(email);
            usuario.setUsername(email);
            
            // Asignar nombre y apellido separados
            usuario.setNombre(givenName != null ? givenName : "Usuario");
            usuario.setApellido(familyName != null ? familyName : "Google");
            
            usuario.setGoogleId(googleId);
            usuario.setProfilePicture(picture);
            usuario.setFotoPerfil(picture); // Si tu modelo tiene este campo
            usuario.setActivo(true);
            usuario.setOauth2User(true);
            
            // ⚠️ IMPORTANTE: Solo CLIENTE, nunca ADMIN para OAuth2
            usuario.setRol(Rol.CLIENTE);
            
            // No necesita password porque usa OAuth2
            usuario.setPassword(null);
            
            usuarioRepository.save(usuario);
        } else {
            // Actualizar información si cambió
            if (googleId != null && !googleId.equals(usuario.getGoogleId())) {
                usuario.setGoogleId(googleId);
            }
            if (picture != null && !picture.equals(usuario.getProfilePicture())) {
                usuario.setProfilePicture(picture);
            }
            if (!usuario.getOauth2User()) {
                usuario.setOauth2User(true);
            }
            
            usuarioRepository.save(usuario);
        }
        
        return usuario;
    }
}
