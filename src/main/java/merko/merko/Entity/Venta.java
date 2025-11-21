package merko.merko.Entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "venta", indexes = {
    @Index(name = "idx_venta_fecha", columnList = "fecha"),
    @Index(name = "idx_venta_estado", columnList = "estado"),
    @Index(name = "idx_venta_cliente_id", columnList = "cliente_id"),
    @Index(name = "idx_venta_branch_id", columnList = "branch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"detalles", "cliente", "branch"})
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    private Double total;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado;

    private String channel;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "dispatch_date")
    private LocalDateTime dispatchDate;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles;

    // Alias para compatibilidad con código que usa "usuario"
    public void setUsuario(Usuario usuario) {
        this.cliente = usuario;
    }

    public Usuario getUsuario() {
        return this.cliente;
    }
}
