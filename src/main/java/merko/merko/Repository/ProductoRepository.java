package merko.merko.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import merko.merko.Entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

	    @Query("select p from Producto p left join ProductoProveedor pp on pp.producto = p " +
		    "where (:proveedorId is null or pp.proveedor.id = :proveedorId) " +
		    "and (:categoriaId is null or p.categoria.id = :categoriaId) " +
		    "and (:q is null or lower(p.nombre) like lower(concat('%',:q,'%')) " +
		    "or lower(p.descripcion) like lower(concat('%',:q,'%')))" )
	    Page<Producto> search(@Param("proveedorId") Long proveedorId,
			  @Param("categoriaId") Long categoriaId,
			  @Param("q") String q,
			  Pageable pageable);

	boolean existsBySku(String sku);
	boolean existsByCodigoBarras(String codigoBarras);
	boolean existsBySkuAndIdNot(String sku, Long id);
	boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);

	@Query(value = "SELECT p.* FROM producto p LEFT JOIN (SELECT producto_id, COALESCE(SUM(stock),0) AS total_stock FROM product_branch GROUP BY producto_id) pb ON pb.producto_id = p.id WHERE (p.stock_minimo IS NOT NULL AND COALESCE(pb.total_stock,0) < p.stock_minimo) OR (p.stock_minimo IS NULL AND COALESCE(pb.total_stock,0) = 0) ORDER BY CASE WHEN p.stock_minimo IS NULL THEN 999999 ELSE (COALESCE(pb.total_stock,0) - p.stock_minimo) END ASC LIMIT ?1", nativeQuery = true)
	List<Producto> findStockCritico(int limit);

    // Cambio: estado ahora es String, no enum
    // Cuenta productos activos o con estado NULL (considerados activos por defecto)
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = :estado OR (p.estado IS NULL AND :estado = 'ACTIVO')")
    long countByEstado(@Param("estado") String estado);
}
