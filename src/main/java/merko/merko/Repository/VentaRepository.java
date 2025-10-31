package merko.merko.Repository;

import merko.merko.Entity.Venta;
import merko.merko.Entity.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detalles WHERE v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Long id);

    @Query("select coalesce(sum(v.total),0) from Venta v where v.estado = :estado and v.fecha between :start and :end")
    Double sumTotalBetweenAndEstado(@Param("start") LocalDate start,
                                    @Param("end") LocalDate end,
                                    @Param("estado") EstadoVenta estado);

    long countByFechaBetweenAndEstado(LocalDate start, LocalDate end, EstadoVenta estado);

    @Query("select v.fecha as fecha, coalesce(sum(v.total),0) as total from Venta v where v.estado = :estado and v.fecha between :start and :end group by v.fecha order by v.fecha")
    List<Object[]> dailyTotalsBetween(@Param("start") LocalDate start,
                                      @Param("end") LocalDate end,
                                      @Param("estado") EstadoVenta estado);
}
