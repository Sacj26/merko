package merko.merko.dto;

import merko.merko.Entity.Rol;

/**
 * Lightweight DTO to store authenticated user info in session (no password, no JPA proxies)
 */
public class SessionUser {

    private Long id;
    private String username;
    private String nombre;
    private String correo;
    private String fotoPerfil;
    private Rol rol;

    public SessionUser() {
    }

    public SessionUser(Long id, String username, String nombre, String correo, Rol rol) {
        this(id, username, nombre, correo, null, rol);
    }

    public SessionUser(Long id, String username, String nombre, String correo, String fotoPerfil, Rol rol) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.correo = correo;
        this.fotoPerfil = fotoPerfil;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Rol getRol() {
        return rol;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
