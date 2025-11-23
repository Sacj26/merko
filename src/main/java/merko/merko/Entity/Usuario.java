package merko.merko.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@Table(name = "usuario", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_correo", columnList = "correo"),
    @Index(name = "idx_rol", columnList = "rol")
})
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String correo;

    private String nombre;

    @Column(nullable = false)
    private String apellido;

    private String telefono;

    private String direccion;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean notificaciones = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    // ===== Campos para OAuth2 (Google) =====
    
    @Column(name = "google_id", unique = true)
    private String googleId;
    
    @Column(name = "profile_picture")
    private String profilePicture;
    
    @Column(name = "oauth2_user")
    private Boolean oauth2User = false;
    
    // Método auxiliar para compatibilidad con email
    public String getEmail() {
        return this.correo;
    }
    
    public void setEmail(String email) {
        this.correo = email;
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.notificaciones == null) {
            this.notificaciones = true;
        }
        if (this.oauth2User == null) {
            this.oauth2User = false;
        }
    }
}
