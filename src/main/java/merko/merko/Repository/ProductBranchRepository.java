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
    java.util.List<ProductBranch> findByBranchId(Long branchId);
    java.util.List<ProductBranch> findByProductoId(Long productoId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductBranch pb SET pb.stock = pb.stock - :qty WHERE pb.id = :id AND pb.stock >= :qty")
    int decrementStockIfEnough(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductBranch pb WHERE pb.producto.id = :productoId")
    void deleteByProductoId(@Param("productoId") Long productoId);
}
