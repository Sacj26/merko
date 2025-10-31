package merko.merko.Repository;

import merko.merko.Entity.MovimientoInventario;
import merko.merko.Entity.Producto;
import merko.merko.Entity.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByProductoOrderByFechaDesc(Producto producto);
    List<MovimientoInventario> findByProductoAndFechaBetweenOrderByFechaDesc(Producto producto, LocalDateTime from, LocalDateTime to);
    List<MovimientoInventario> findByProductoAndTipoOrderByFechaDesc(Producto producto, TipoMovimiento tipo);
    List<MovimientoInventario> findByVenta_IdOrderByFechaAsc(Long ventaId);
}
