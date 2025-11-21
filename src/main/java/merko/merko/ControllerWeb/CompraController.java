package merko.merko.ControllerWeb;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import merko.merko.Entity.Compra;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.CompraRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProveedorRepository;
import merko.merko.dto.CompraForm;

@Controller
@RequestMapping("/admin/compras")
public class CompraController {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private BranchRepository branchRepository;



    @Autowired
    private merko.merko.Service.CompraService compraService;
    
    @Autowired
    private merko.merko.Repository.MovimientoInventarioRepository movimientoInventarioRepository;

    @GetMapping("/nueva")
    public String nuevaCompra(Model model) {
        model.addAttribute("compra", new Compra());

        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);

        // Mostrar todos los proveedores para permitir seleccionar y luego agregar productos si es necesario
        List<Proveedor> proveedores = proveedorRepository.findAll();
        model.addAttribute("proveedores", proveedores);
        // Añadir sucursales disponibles para poder asociar la compra a una sucursal (opcional)
        model.addAttribute("sucursales", branchRepository.findAll());
        return "admin/compras/crear";
    }

    // Alias /crear para coincidir con el menú
    @GetMapping("/crear")
    public String crearCompra(Model model) {
        return nuevaCompra(model);
    }


    @PostMapping("/guardar")
    public String guardarCompra(
        @ModelAttribute CompraForm compraForm,
        @RequestParam(value = "productoId", required = false) Long productoId,
        @RequestParam(value = "cantidad", required = false) Integer cantidad,
        @RequestParam(value = "precioUnitario", required = false) Double precioUnitario,
        Model model) {
        // TODO: Implementar guardarCompra con nuevos campos (branch_id, cantidad, precio_unidad)
        // compraService.guardarCompra(compraForm, productoId, cantidad, precioUnitario);
        return "redirect:/admin/compras";
    }


    @GetMapping
    public String historialCompras(Model model) {
        List<Compra> compras = compraRepository.findAllWithBranchAndDetalles();
        model.addAttribute("compras", compras);
        // Añadir branches para filtros en la vista
        model.addAttribute("branches", branchRepository.findAll());
        return "admin/compras/index";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleCompra(@PathVariable Long id, Model model) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        model.addAttribute("compra", compra);
        var movimientos = movimientoInventarioRepository.findByCompra_IdOrderByFechaAsc(compra.getId());
        model.addAttribute("movimientos", movimientos);
        return "admin/compras/detalle";
    }
}

