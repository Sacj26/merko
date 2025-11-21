package merko.merko.Repository;

import merko.merko.Entity.ProductBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductBranchRepository extends JpaRepository<ProductBranch, Long> {
    Optional<ProductBranch> findByProductoIdAndBranchId(Long productoId, Long branchId);
    
    // Consulta mejorada: solo trae ProductBranch donde el producto existe (INNER JOIN)
    // Esto evita FetchNotFoundException cuando hay referencias a productos eliminados
    @Query("SELECT pb FROM ProductBranch pb JOIN FETCH pb.producto p WHERE pb.branch.id = :branchId")
    java.util.List<ProductBranch> findByBranchId(@Param("branchId") Long branchId);
    
    java.util.List<ProductBranch> findByProductoId(Long productoId);
    
    // Consulta con eager loading de branch para evitar LazyInitializationException
    @Query("SELECT pb FROM ProductBranch pb JOIN FETCH pb.branch b WHERE pb.producto.id = :productoId")
    java.util.List<ProductBranch> findByProductoIdWithBranch(@Param("productoId") Long productoId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductBranch pb SET pb.stock = pb.stock - :qty WHERE pb.id = :id AND pb.stock >= :qty")
    int decrementStockIfEnough(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductBranch pb WHERE pb.producto.id = :productoId")
    void deleteByProductoId(@Param("productoId") Long productoId);
    
    // Consulta para limpiar registros huérfanos (ProductBranch que referencian productos que no existen)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM product_branch WHERE producto_id NOT IN (SELECT id FROM producto)", nativeQuery = true)
    int deleteOrphanedProductBranches();
}
