package merko.merko.Repository;

import merko.merko.Entity.EstadoProducto;
import merko.merko.Entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

	@Query("select p from Producto p where (:proveedorId is null or p.proveedor.id = :proveedorId) " +
		  "and (:q is null or lower(p.nombre) like lower(concat('%',:q,'%')) " +
		  "or lower(p.descripcion) like lower(concat('%',:q,'%')))" )
	Page<Producto> search(@Param("proveedorId") Long proveedorId,
			  @Param("q") String q,
			  Pageable pageable);

	boolean existsBySku(String sku);
	boolean existsByCodigoBarras(String codigoBarras);
	boolean existsBySkuAndIdNot(String sku, Long id);
	boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);

    @Query("select p from Producto p where p.stockMinimo is not null and p.stock < p.stockMinimo order by (p.stock - p.stockMinimo) asc")
    List<Producto> findStockCritico(Pageable pageable);

    long countByEstado(EstadoProducto estado);
}
