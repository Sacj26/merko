package merko.merko.Entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    private String nombre;
    private String telefono;
    @Column(nullable = false)
    private String apellido = "";
    
    // Estado del usuario. No nulo en BD; por defecto true para usuarios activos.
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean notificaciones = false;

    public Usuario(String username, String correo, String password, Rol rol) {

        this.username = username;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
        this.activo = true;
        this.apellido = "";
        this.notificaciones = false;
    }
}
