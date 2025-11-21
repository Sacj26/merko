package merko.merko.Repository;

import merko.merko.Entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Branch b LEFT JOIN FETCH b.contacts LEFT JOIN FETCH b.proveedor WHERE b.id = :id")
    java.util.Optional<Branch> findByIdWithContactsAndProveedor(@org.springframework.data.repository.query.Param("id") Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT b FROM Branch b LEFT JOIN FETCH b.contacts WHERE b.proveedor.id = :proveedorId")
    java.util.List<Branch> findByProveedorIdWithContacts(@org.springframework.data.repository.query.Param("proveedorId") Long proveedorId);

}
