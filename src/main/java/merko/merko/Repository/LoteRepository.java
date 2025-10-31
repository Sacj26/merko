package merko.merko.Repository;

import merko.merko.Entity.Lote;
import merko.merko.Entity.Producto;
import merko.merko.Entity.EstadoLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Lote> findByProductoAndEstadoOrderByFechaVencimientoAsc(Producto producto, EstadoLote estado);

    @Query("select l from Lote l where l.estado = :estado and l.cantidadDisponible > 0 and l.fechaVencimiento between :start and :end order by l.fechaVencimiento asc")
    List<Lote> findExpiringBetween(@Param("start") LocalDate start,
                                   @Param("end") LocalDate end,
                                   @Param("estado") EstadoLote estado);
}
