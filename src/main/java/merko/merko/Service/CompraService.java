package merko.merko.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import merko.merko.Entity.Compra;
import merko.merko.Repository.CompraRepository;

/**
 * Servicio simplificado para Compras - BD: compra (id, fecha, cantidad, precio_unidad, total, branch_id)
 */
@Service
public class CompraService {

    private final CompraRepository compraRepository;

    public CompraService(CompraRepository compraRepository) {
        this.compraRepository = compraRepository;
    }

    public List<Compra> getAllCompras() {
        return compraRepository.findAll();
    }

    public List<Compra> getAllComprasWithBranchAndDetalles() {
        return compraRepository.findAllWithBranchAndDetalles();
    }

    public Optional<Compra> getCompraById(Long id) {
        return compraRepository.findById(id);
    }

    @Transactional
    public Compra crearCompra(Compra compra) {
        if (compra.getBranch() == null) {
            throw new IllegalArgumentException("Una compra debe tener una sucursal.");
        }
        if (compra.getFecha() == null) {
            compra.setFecha(LocalDateTime.now());
        }
        if (compra.getTotal() == null || compra.getTotal() <= 0) {
            throw new IllegalArgumentException("El total debe ser mayor a cero.");
        }
        return compraRepository.save(compra);
    }

    @Transactional
    public void deleteCompra(Long id) {
        compraRepository.deleteById(id);
    }
}
