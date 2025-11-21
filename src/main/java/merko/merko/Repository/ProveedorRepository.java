package merko.merko.Repository;

import merko.merko.Entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    // Primera query: Cargar proveedores con branches
    @Query("SELECT DISTINCT p FROM Proveedor p LEFT JOIN FETCH p.branches ORDER BY p.id DESC")
    List<Proveedor> findAllWithBranches();
    
    // Query para cargar un solo proveedor con sus branches
    @Query("SELECT p FROM Proveedor p LEFT JOIN FETCH p.branches WHERE p.id = :id")
    Optional<Proveedor> findByIdWithBranches(@org.springframework.data.repository.query.Param("id") Long id);

}
