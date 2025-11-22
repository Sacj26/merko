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
    private merko.merko.Repository.MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private merko.merko.Service.CompraService compraService;

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
    public String guardarCompra(@ModelAttribute CompraForm compraForm, Model model) {
        try {
            compraService.guardarCompraConDetalles(compraForm);
            return "redirect:/admin/compras";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            model.addAttribute("sucursales", branchRepository.findAll());
            return "admin/compras/crear";
        }
    }


    @GetMapping
    public String historialCompras(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "100") int limit,
            Model model) {
        
        List<Compra> compras;
        
        if (fecha != null && !fecha.isEmpty()) {
            // Filtrar por fecha específica
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(fecha);
                java.time.LocalDateTime startOfDay = localDate.atStartOfDay();
                java.time.LocalDateTime endOfDay = localDate.atTime(23, 59, 59);
                
                // Primero obtener IDs con filtro de fecha
                List<Long> compraIds = compraRepository.findIdsByFechaBetween(startOfDay, endOfDay, 
                    org.springframework.data.domain.PageRequest.of(0, limit));
                
                compras = compraIds.isEmpty() ? 
                    java.util.Collections.emptyList() : 
                    compraRepository.findByIdsWithAllRelations(compraIds);
            } catch (Exception e) {
                // Si hay error parseando fecha, mostrar últimas 10
                List<Long> compraIds = compraRepository.findTop10IdsByOrderByFechaDesc(
                    org.springframework.data.domain.PageRequest.of(0, 10));
                compras = compraIds.isEmpty() ? 
                    java.util.Collections.emptyList() : 
                    compraRepository.findByIdsWithAllRelations(compraIds);
            }
        } else {
            // Sin filtro de fecha, mostrar las últimas según limit
            List<Long> compraIds = compraRepository.findTop10IdsByOrderByFechaDesc(
                org.springframework.data.domain.PageRequest.of(0, limit));
            compras = compraIds.isEmpty() ? 
                java.util.Collections.emptyList() : 
                compraRepository.findByIdsWithAllRelations(compraIds);
        }
        
        // Filtrar sucursales por proveedor si se seleccionó uno
        var branches = branchRepository.findAll();
        if (proveedorId != null) {
            branches = branches.stream()
                .filter(b -> b.getProveedor() != null && b.getProveedor().getId().equals(proveedorId))
                .collect(java.util.stream.Collectors.toList());
        }
        
        model.addAttribute("compras", compras);
        model.addAttribute("proveedores", proveedorRepository.findAll());
        model.addAttribute("branches", branches);
        model.addAttribute("fechaFiltro", fecha);
        model.addAttribute("proveedorIdFiltro", proveedorId);
        model.addAttribute("branchIdFiltro", branchId);
        model.addAttribute("limitActual", limit);
        return "admin/compras/index";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleCompra(@PathVariable Long id, Model model) {
        Compra compra = compraRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        model.addAttribute("compra", compra);
        var movimientos = movimientoInventarioRepository.findByCompra_IdOrderByFechaAsc(compra.getId());
        model.addAttribute("movimientos", movimientos);
        return "admin/compras/detalle";
    }
}

