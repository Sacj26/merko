package merko.merko.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import merko.merko.Entity.Compra;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    @Query("SELECT c FROM Compra c LEFT JOIN FETCH c.detalles d LEFT JOIN FETCH d.producto WHERE c.id = :id")
    java.util.Optional<Compra> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Compra c LEFT JOIN FETCH c.branch LEFT JOIN FETCH c.detalles d LEFT JOIN FETCH d.producto ORDER BY c.fecha DESC")
    List<Compra> findAllWithBranchAndDetalles();

    @Query(value = "SELECT c.* FROM compra c ORDER BY c.fecha DESC LIMIT 10", nativeQuery = true)
    List<Compra> findTop10ByOrderByFechaDesc();

    @Query("select coalesce(sum(c.total),0) from Compra c where c.fecha between :start and :end")
    Double sumTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // FIXED: Calculate total correctly as cantidad * precioUnidad instead of using stored total
    @Query("select coalesce(sum(c.cantidad * c.precioUnidad),0.0) from Compra c where c.fecha between :start and :end")
    Double sumCalculatedTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Total histórico de todas las compras
    @Query("select coalesce(sum(c.cantidad * c.precioUnidad),0.0) from Compra c")
    Double sumAllCalculatedTotal();

    long countByFechaBetween(LocalDateTime start, LocalDateTime end);

    @Query("select function('date', c.fecha) as fecha, coalesce(sum(c.cantidad * c.precioUnidad),0.0) as total from Compra c where c.fecha between :start and :end group by function('date', c.fecha) order by fecha")
    List<Object[]> dailyTotalsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
