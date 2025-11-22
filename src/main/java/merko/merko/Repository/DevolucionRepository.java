package merko.merko.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import merko.merko.Entity.Devolucion;
import merko.merko.Entity.Venta;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    
    /**
     * Buscar todas las devoluciones asociadas a una venta específica
     */
    List<Devolucion> findByVenta(Venta venta);
    
    /**
     * Buscar todas las devoluciones por ID de venta
     */
    List<Devolucion> findByVentaId(Long ventaId);
    
    /**
     * Verificar si existe alguna devolución para una venta
     */
    boolean existsByVentaId(Long ventaId);
}
