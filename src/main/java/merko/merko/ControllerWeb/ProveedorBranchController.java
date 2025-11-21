package merko.merko.ControllerWeb;

import merko.merko.Entity.Branch;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.ProductBranchRepository;
import merko.merko.Service.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/proveedores/{proveedorId}/sucursales")
public class ProveedorBranchController {

    private final ProveedorService proveedorService;
    private final BranchRepository branchRepository;
    private final ProductBranchRepository productBranchRepository;

    public ProveedorBranchController(ProveedorService proveedorService, BranchRepository branchRepository, ProductBranchRepository productBranchRepository) {
        this.proveedorService = proveedorService;
        this.branchRepository = branchRepository;
        this.productBranchRepository = productBranchRepository;
    }

    @GetMapping
    public String listar(@PathVariable Long proveedorId, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        List<Branch> branches = proveedor.getBranches();
        
        // Crear un mapa con el conteo de productos por sucursal
        java.util.Map<Long, Long> productCounts = new java.util.HashMap<>();
        for (Branch branch : branches) {
            long count = productBranchRepository.findByBranchId(branch.getId()).size();
            productCounts.put(branch.getId(), count);
        }
        
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branches", branches);
        model.addAttribute("productCounts", productCounts);
        return "admin/proveedores/sucursales/list";
    }

    // API JSON: obtener sucursales de un proveedor (para AJAX)
    @GetMapping(value = "/json", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> listarJson(@PathVariable Long proveedorId) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElse(null);
        if (proveedor == null) return java.util.Collections.emptyList();
        java.util.List<Branch> branches = proveedor.getBranches();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (Branch b : branches) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", b.getId());
            m.put("nombre", b.getNombre());
            m.put("ciudad", b.getCiudad());
            m.put("pais", b.getPais());
            out.add(m);
        }
        return out;
    }

    @GetMapping("/nuevo")
    public String nuevo(@PathVariable Long proveedorId, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Branch branch = new Branch();
        // añadir un contacto vacío para que el formulario lo muestre al crear
        branch.getContacts().add(new merko.merko.Entity.ContactPerson());
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branch", branch);
        model.addAttribute("accion", "/admin/proveedores/" + proveedorId + "/sucursales");
        return "admin/proveedores/sucursales/form";
    }

    @PostMapping
    public String crear(@PathVariable Long proveedorId, @ModelAttribute Branch branch, RedirectAttributes redirectAttributes) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        branch.setProveedor(proveedor);
        // asegurar que cada contacto tenga la referencia a la sucursal
        if (branch.getContacts() != null) {
            for (merko.merko.Entity.ContactPerson c : branch.getContacts()) {
                c.setBranch(branch);
            }
        }
        branchRepository.save(branch);
        redirectAttributes.addFlashAttribute("mensaje", "Sucursal creada");
        return "redirect:/admin/proveedores/" + proveedorId + "/sucursales";
    }

    @GetMapping("/editar/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String editar(@PathVariable Long proveedorId, @PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        // Cargar sucursal con contactos eager-loaded
        Branch branch = branchRepository.findByIdWithContactsAndProveedor(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        // asegurar que haya al menos un contacto para mostrar en el formulario
        if (branch.getContacts() == null || branch.getContacts().isEmpty()) {
            branch.getContacts().add(new merko.merko.Entity.ContactPerson());
        }
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branch", branch);
        model.addAttribute("accion", "/admin/proveedores/" + proveedorId + "/sucursales/" + id + "/actualizar");
        return "admin/proveedores/sucursales/form";
    }

    @PostMapping("/{id}/actualizar")
    @org.springframework.transaction.annotation.Transactional
    public String actualizar(@PathVariable Long proveedorId, @PathVariable Long id, @ModelAttribute Branch branch, RedirectAttributes redirectAttributes) {
        // Cargar sucursal con contactos eager-loaded
        Branch existente = branchRepository.findByIdWithContactsAndProveedor(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        existente.setNombre(branch.getNombre());
        existente.setDireccion(branch.getDireccion());
        existente.setTelefono(branch.getTelefono());
        existente.setCiudad(branch.getCiudad());
        existente.setPais(branch.getPais());
        existente.setActivo(branch.getActivo());
        // reemplazar contactos: limpiar y volver a añadir (cascade=ALL permite persistir)
        existente.getContacts().clear();
        if (branch.getContacts() != null) {
            for (merko.merko.Entity.ContactPerson c : branch.getContacts()) {
                c.setBranch(existente);
                existente.getContacts().add(c);
            }
        }
        branchRepository.save(existente);
        redirectAttributes.addFlashAttribute("mensaje", "Sucursal actualizada");
        return "redirect:/admin/proveedores/" + proveedorId + "/sucursales";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long proveedorId, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        branchRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("mensaje", "Sucursal eliminada");
        return "redirect:/admin/proveedores/" + proveedorId + "/sucursales";
    }

    @GetMapping("/{branchId}/agregar-productos")
    public String agregarProductosPorSucursal(@PathVariable Long proveedorId,
                                             @PathVariable Long branchId,
                                             Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branch", branch);
        return "admin/proveedores/agregar-productos";
    }
}
