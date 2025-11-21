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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    private Double amount;
    private String method; // e.g., CARD, PSE, CASH
    private String status; // e.g., SUCCESS, FAILED, PENDING
    private LocalDateTime createdAt;
}
