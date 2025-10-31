package merko.merko.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private String codigoLote;

    private LocalDate fechaFabricacion;
    private LocalDate fechaVencimiento;

    private Integer cantidadDisponible;

    @Enumerated(EnumType.STRING)
    private EstadoLote estado = EstadoLote.ACTIVO;

    private String ubicacion;

    // Costeo del lote (p. ej., costo unitario de compra)
    private Double costoUnitario;

    // Control de concurrencia para asignación FEFO
    @Version
    private Long version;
}
