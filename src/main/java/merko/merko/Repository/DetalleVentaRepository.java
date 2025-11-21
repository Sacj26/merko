package merko.merko.Repository;

import merko.merko.Entity.DetalleVenta;
import merko.merko.Entity.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    @Query("select dv.producto.id as productoId, dv.producto.nombre as nombre, sum(dv.cantidad) as cantidad " +
	    "from DetalleVenta dv join dv.venta v " +
	    "where (v.estado = :estado OR (v.estado IS NULL AND :estado = 'ACTIVA')) and v.fecha between :start and :end " +
	    "group by dv.producto.id, dv.producto.nombre " +
	    "order by cantidad desc")
	List<Object[]> topProductosPorCantidad(@Param("start") LocalDateTime start,
					   @Param("end") LocalDateTime end,
					   @Param("estado") EstadoVenta estado);

    // Contar total de detalles de venta en un rango de fechas
    @Query("select count(dv) from DetalleVenta dv join dv.venta v where (v.estado = :estado OR (v.estado IS NULL AND :estado = 'ACTIVA')) and v.fecha between :start and :end")
    Long countByFechaBetweenAndEstado(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("estado") EstadoVenta estado);
}
