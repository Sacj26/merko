package merko.merko.ControllerWeb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import merko.merko.Entity.Branch;
import merko.merko.Entity.ProductBranch;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.ProductBranchRepository;

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
    @Transactional(readOnly = true)
    public String verSucursal(@PathVariable("id") Long id, Model model) {
        // OPTIMIZACIÓN: Cargar branch con contacts y proveedor en una sola query
        Branch branch = branchRepository.findByIdWithContactsAndProveedor(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        // Cargar productos con sus relaciones (producto) eager-loaded
        List<ProductBranch> productBranches = productBranchRepository.findByBranchId(id);
        model.addAttribute("branch", branch);
        model.addAttribute("productBranches", productBranches);
        return "admin/sucursales/ver";
    }
    
    @GetMapping("/editar/{id}")
    public String editarSucursal(@PathVariable("id") Long id, Model model) {
        Branch branch = branchRepository.findByIdWithContactsAndProveedor(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        model.addAttribute("branch", branch);
        // Redirigir a la página de edición del proveedor con focus en la sucursal
        if (branch.getProveedor() != null) {
            return "redirect:/admin/proveedores/editar/" + branch.getProveedor().getId() + "?sucursalId=" + id;
        }
        return "redirect:/admin/proveedores";
    }

    // API endpoint para búsqueda de sucursales (DTO simple para evitar ciclos)
    @GetMapping("/api/todas")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<Map<String, Object>> obtenerTodasSucursalesAPI(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long proveedorId) {
        
        java.util.stream.Stream<Branch> sucursales;
        
        if (proveedorId != null) {
            // Filtrar por proveedor
            sucursales = branchRepository.findAll().stream()
                .filter(s -> s.getProveedor() != null && s.getProveedor().getId().equals(proveedorId));
        } else {
            // Todas las sucursales
            sucursales = branchRepository.findAll().stream();
        }
        
        return sucursales
            .map(s -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", s.getId());
                dto.put("nombre", s.getNombre());
                dto.put("ciudad", s.getCiudad());
                dto.put("pais", s.getPais());
                dto.put("direccion", s.getDireccion());
                if (s.getProveedor() != null) {
                    dto.put("proveedorId", s.getProveedor().getId());
                    dto.put("proveedorNombre", s.getProveedor().getNombre());
                }
                // Contar productos de la sucursal
                try {
                    int totalProductos = productBranchRepository.findByBranchId(s.getId()).size();
                    dto.put("totalProductos", totalProductos);
                } catch (Exception e) {
                    dto.put("totalProductos", 0);
                }
                return dto;
            })
            .collect(Collectors.toList());
    }
}
