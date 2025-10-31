package merko.merko.Entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El NIT/RUC es obligatorio")
    @Size(min = 5, max = 20, message = "El NIT/RUC debe tener entre 5 y 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String nit;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "Formato de teléfono inválido")
    @Column(nullable = false, length = 20)
    private String telefono;

    @Email(message = "Email inválido")
    @Column(length = 100)
    private String email;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String direccion;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    @Column(length = 100)
    private String ciudad;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    @Column(length = 100)
    private String pais;

    @Size(max = 100, message = "El nombre del contacto no puede exceder 100 caracteres")
    @Column(length = 100)
    private String nombreContacto;

    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    @Column(length = 100)
    private String cargoContacto;

    @Column(length = 20)
    private String telefonoContacto;

    @Email(message = "Email del contacto inválido")
    @Column(length = 100)
    private String emailContacto;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fechaRegistro = LocalDate.now();

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Producto> productos = new ArrayList<>();

    // Constructor para mantener compatibilidad con código existente
    public Proveedor(String nombre, String nit, String telefono, String direccion) {
        this.nombre = nombre;
        this.nit = nit;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fechaRegistro = LocalDate.now();
        this.activo = true;
    }
}
