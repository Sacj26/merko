package merko.merko.ControllerWeb;

import merko.merko.Entity.Branch;
import merko.merko.Entity.ProductBranch;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.ProductBranchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/sucursales")
public class SucursalController {

    private final BranchRepository branchRepository;
    private final ProductBranchRepository productBranchRepository;

    public SucursalController(BranchRepository branchRepository, ProductBranchRepository productBranchRepository) {
        this.branchRepository = branchRepository;
        this.productBranchRepository = productBranchRepository;
    }

    @GetMapping("/ver/{id}")
    public String verSucursal(@PathVariable("id") Long id, Model model) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        List<ProductBranch> productBranches = productBranchRepository.findByBranchId(id);
        model.addAttribute("branch", branch);
        model.addAttribute("productBranches", productBranches);
        return "admin/sucursales/ver";
    }
}
