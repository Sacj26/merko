package merko.merko.Repository;

import merko.merko.Entity.ProductoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Long> {
    List<ProductoProveedor> findByProductoId(Long productoId);
    List<ProductoProveedor> findByProveedorId(Long proveedorId);
}
