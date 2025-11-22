package merko.merko.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import merko.merko.Entity.DetalleCompra;

import java.util.List;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    @Query("select dc.producto.id as productoId, dc.producto.nombre as nombre, sum(dc.cantidad) as cantidad " +
	    "from DetalleCompra dc join dc.compra c " +
	    "where c.fecha between :start and :end " +
	    "group by dc.producto.id, dc.producto.nombre " +
	    "order by cantidad desc")
    List<Object[]> topProductosCompradosPorCantidad(@Param("start") java.time.LocalDateTime start,
					    @Param("end") java.time.LocalDateTime end);

    // Contar total de detalles de compra en un rango de fechas
    @Query("select count(dc) from DetalleCompra dc join dc.compra c where c.fecha between :start and :end")
    Long countByFechaBetween(@Param("start") java.time.LocalDateTime start,
                             @Param("end") java.time.LocalDateTime end);
}