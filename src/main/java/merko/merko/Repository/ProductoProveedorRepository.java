package merko.merko.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import merko.merko.Entity.ProductoProveedor;

@Repository
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Long> {
    List<ProductoProveedor> findByProductoId(Long productoId);
    List<ProductoProveedor> findByProveedorId(Long proveedorId);
    
    @Query("SELECT pp FROM ProductoProveedor pp LEFT JOIN FETCH pp.producto WHERE pp.proveedor.id = :proveedorId AND pp.producto IS NOT NULL")
    List<ProductoProveedor> findByProveedorIdWithProducto(@Param("proveedorId") Long proveedorId);
}
