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
    List<MovimientoInventario> findByProductBranch_ProductoOrderByFechaDesc(Producto producto);
    List<MovimientoInventario> findByProductBranch_ProductoAndFechaBetweenOrderByFechaDesc(Producto producto, LocalDateTime from, LocalDateTime to);
    List<MovimientoInventario> findByProductBranch_ProductoAndTipoOrderByFechaDesc(Producto producto, TipoMovimiento tipo);
    List<MovimientoInventario> findByVenta_IdOrderByFechaAsc(Long ventaId);
    List<MovimientoInventario> findByCompra_IdOrderByFechaAsc(Long compraId);
}
