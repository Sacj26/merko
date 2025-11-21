package merko.merko.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import merko.merko.Entity.EstadoVenta;
import merko.merko.Entity.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.branch WHERE v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.branch ORDER BY v.fecha DESC")
    List<Venta> findAllWithDetalles();

    // VIEJO: Suma desde campo v.total (puede contener valores incorrectos)
    @Query("select coalesce(sum(v.total),0) from Venta v where (v.estado = :estado OR (v.estado IS NULL AND :estado = 'ACTIVA')) and v.fecha between :start and :end")
    Double sumTotalBetweenAndEstado(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("estado") EstadoVenta estado);

    // CORRECTO: Calcula sumando (cantidad * precioUnitario) desde detalle_venta
    @Query("select coalesce(sum(dv.cantidad * dv.precioUnitario), 0.0) from Venta v join v.detalles dv where (v.estado = :estado OR (v.estado IS NULL AND :estado = 'ACTIVA')) and v.fecha between :start and :end")
    Double sumCalculatedTotalBetweenAndEstado(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("estado") EstadoVenta estado);

    // Total histórico de todas las ventas activas
    @Query("select coalesce(sum(dv.cantidad * dv.precioUnitario), 0.0) from Venta v join v.detalles dv where (v.estado = :estado OR (v.estado IS NULL AND :estado = 'ACTIVA'))")
    Double sumAllCalculatedTotalByEstado(@Param("estado") EstadoVenta estado);

    @Query("SELECT COUNT(v) FROM Venta v WHERE (v.estado = :estado OR (v.estado IS NULL AND :estado = 'ACTIVA')) AND v.fecha BETWEEN :start AND :end")
    long countByFechaBetweenAndEstado(@Param("start") LocalDateTime start, 
                                      @Param("end") LocalDateTime end, 
                                      @Param("estado") EstadoVenta estado);

    // CORREGIDO: Calcula desde detalle_venta (cantidad * precio_unitario) en lugar de v.total
    @Query(value = "SELECT DATE(v.fecha) as fecha, COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) as total FROM venta v LEFT JOIN detalle_venta dv ON v.id = dv.venta_id WHERE (v.estado = :#{#estado.name()} OR (v.estado IS NULL AND :#{#estado.name()} = 'ACTIVA')) AND v.fecha BETWEEN :start AND :end GROUP BY DATE(v.fecha) ORDER BY fecha", nativeQuery = true)
    List<Object[]> dailyTotalsBetween(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("estado") EstadoVenta estado);
}
