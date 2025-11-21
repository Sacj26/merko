package merko.merko.Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @Column(nullable = false, unique = true, length = 20)
    private String nit;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String pais;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Email(message = "Email inválido")
    @Column(length = 100)
    private String email;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "fecha_registro", nullable = false)
    private java.time.LocalDate fechaRegistro;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Branch> branches = new java.util.ArrayList<>();
}
