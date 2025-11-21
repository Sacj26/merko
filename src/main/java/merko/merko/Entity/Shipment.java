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
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;
}
