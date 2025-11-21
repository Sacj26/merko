package merko.merko.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    private Integer cantidad;
    private Double costoUnitario;

    private LocalDateTime fecha;

    // Referencias opcionales
    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_branch_id", nullable = false)
    private ProductBranch productBranch;

    private String referencia; // texto libre (p. ej., codigo de documento)
}
