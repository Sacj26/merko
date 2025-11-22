package merko.merko.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import merko.merko.Entity.Compra;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    @Query("SELECT c FROM Compra c LEFT JOIN FETCH c.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH d.branch WHERE c.id = :id")
    java.util.Optional<Compra> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Compra c LEFT JOIN FETCH c.branch b LEFT JOIN FETCH b.proveedor LEFT JOIN FETCH c.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH d.branch WHERE c.id = :id")
    java.util.Optional<Compra> findByIdWithAllRelations(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Compra c LEFT JOIN FETCH c.branch LEFT JOIN FETCH c.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH d.branch ORDER BY c.fecha DESC")
    List<Compra> findAllWithBranchAndDetalles();

    @Query("SELECT c.id FROM Compra c ORDER BY c.fecha DESC")
    List<Long> findTop10IdsByOrderByFechaDesc(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c.id FROM Compra c WHERE c.fecha BETWEEN :start AND :end ORDER BY c.fecha DESC")
    List<Long> findIdsByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT c FROM Compra c LEFT JOIN FETCH c.branch b LEFT JOIN FETCH b.proveedor LEFT JOIN FETCH c.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH d.branch WHERE c.id IN :ids ORDER BY c.fecha DESC")
    List<Compra> findByIdsWithAllRelations(@Param("ids") List<Long> ids);

    @Query("select coalesce(sum(c.total),0) from Compra c where c.fecha between :start and :end")
    Double sumTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Usar c.total directamente (ya recalculado desde SQL script)
    @Query("select coalesce(sum(c.total),0.0) from Compra c where c.fecha between :start and :end")
    Double sumCalculatedTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Total histórico de todas las compras (optimizado - usa índice en total)
    @Query("select coalesce(sum(c.total),0.0) from Compra c")
    Double sumAllCalculatedTotal();

    long countByFechaBetween(LocalDateTime start, LocalDateTime end);

    @Query("select function('date', c.fecha) as fecha, coalesce(sum(c.total),0.0) as total from Compra c where c.fecha between :start and :end group by function('date', c.fecha) order by fecha")
    List<Object[]> dailyTotalsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
