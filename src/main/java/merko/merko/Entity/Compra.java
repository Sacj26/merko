package merko.merko.Entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@Table(name = "compra", indexes = {
    @Index(name = "idx_compra_fecha", columnList = "fecha"),
    @Index(name = "idx_compra_branch_id", columnList = "branch_id")
})
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"detalles", "branch"})
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    @jakarta.persistence.Column(nullable = false)
    private Integer cantidad;

    @jakarta.persistence.Column(name = "precio_unidad", nullable = false)
    private Double precioUnidad;

    @jakarta.persistence.Column(nullable = false)
    private Double total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToMany(mappedBy = "compra", fetch = FetchType.LAZY)
    private List<DetalleCompra> detalles;
}
