package merko.merko.ControllerWeb;

import merko.merko.Entity.Branch;
import merko.merko.Entity.ContactPerson;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.ContactPersonRepository;
import merko.merko.Service.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/proveedores/{proveedorId}/sucursales/{branchId}/contactos")
public class ProveedorBranchContactController {

    private final ProveedorService proveedorService;
    private final BranchRepository branchRepository;
    private final ContactPersonRepository contactPersonRepository;

    public ProveedorBranchContactController(ProveedorService proveedorService, BranchRepository branchRepository, ContactPersonRepository contactPersonRepository) {
        this.proveedorService = proveedorService;
        this.branchRepository = branchRepository;
        this.contactPersonRepository = contactPersonRepository;
    }

    @GetMapping
    public String listar(@PathVariable Long proveedorId, @PathVariable Long branchId, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        List<ContactPerson> contacts = branch.getContacts();
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branch", branch);
        model.addAttribute("contacts", contacts);
        return "admin/proveedores/sucursales/contactos/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(@PathVariable Long proveedorId, @PathVariable Long branchId, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        ContactPerson contact = new ContactPerson();
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branch", branch);
        model.addAttribute("contact", contact);
        model.addAttribute("accion", "/admin/proveedores/" + proveedorId + "/sucursales/" + branchId + "/contactos");
        return "admin/proveedores/sucursales/contactos/form";
    }

    @PostMapping
    public String crear(@PathVariable Long proveedorId, @PathVariable Long branchId, @ModelAttribute ContactPerson contact, RedirectAttributes redirectAttributes) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        // Enforce single primary per branch
        if (contact.getIsPrimary() != null && contact.getIsPrimary()) {
            branch.getContacts().forEach(c -> c.setIsPrimary(false));
        }
        contact.setBranch(branch);
        contactPersonRepository.save(contact);
        redirectAttributes.addFlashAttribute("mensaje", "Contacto creado");
        return "redirect:/admin/proveedores/" + proveedorId + "/sucursales/" + branchId + "/contactos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long proveedorId, @PathVariable Long branchId, @PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        ContactPerson contact = contactPersonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado"));
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branch", branch);
        model.addAttribute("contact", contact);
        model.addAttribute("accion", "/admin/proveedores/" + proveedorId + "/sucursales/" + branchId + "/contactos/" + id + "/actualizar");
        return "admin/proveedores/sucursales/contactos/form";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Long proveedorId, @PathVariable Long branchId, @PathVariable Long id, @ModelAttribute ContactPerson contact, RedirectAttributes redirectAttributes) {
        ContactPerson existente = contactPersonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado"));
        // If setting primary, unset others
        if (contact.getIsPrimary() != null && contact.getIsPrimary()) {
            Branch branch = existente.getBranch();
            if (branch != null) branch.getContacts().forEach(c -> c.setIsPrimary(false));
        }
        existente.setNombre(contact.getNombre());
        existente.setRol(contact.getRol());
        existente.setTelefono(contact.getTelefono());
        existente.setEmail(contact.getEmail());
        existente.setNotas(contact.getNotas());
        existente.setIsPrimary(contact.getIsPrimary());
        contactPersonRepository.save(existente);
        redirectAttributes.addFlashAttribute("mensaje", "Contacto actualizado");
        return "redirect:/admin/proveedores/" + proveedorId + "/sucursales/" + branchId + "/contactos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long proveedorId, @PathVariable Long branchId, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        contactPersonRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("mensaje", "Contacto eliminado");
        return "redirect:/admin/proveedores/" + proveedorId + "/sucursales/" + branchId + "/contactos";
    }
}
