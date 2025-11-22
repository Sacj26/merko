package merko.merko.ControllerWeb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import merko.merko.Entity.DetalleVenta;
import merko.merko.Entity.EstadoVenta;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Entity.Venta;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.MovimientoInventarioRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.VentaRepository;
import merko.merko.Service.UsuarioService;
import merko.merko.Service.VentaService;
import merko.merko.dto.DetalleVentaForm;
import merko.merko.dto.VentaForm;

@Controller
@RequestMapping("/admin/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private merko.merko.Service.ProveedorService proveedorService;

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @GetMapping
    public String listarVentas(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(defaultValue = "100") int limit,
            Model model) {
        
        // Optimización: Consultar primero solo los IDs con filtros, luego cargar entidades completas
        List<Long> ventaIds;
        
        if (fecha != null && !fecha.isEmpty()) {
            ventaIds = ventaRepository.findIdsByFechaBetween(fecha, limit);
        } else {
            ventaIds = ventaRepository.findTopIdsByOrderByFechaDesc(limit);
        }
        
        // Cargar entidades completas solo para los IDs filtrados
        List<Venta> ventas = ventaIds.isEmpty() ? 
            java.util.Collections.emptyList() : 
            ventaRepository.findByIdsWithAllRelations(ventaIds);
        
        // Aplicar filtros adicionales en memoria (solo cuando sea necesario)
        if (clienteId != null) {
            ventas = ventas.stream()
                .filter(v -> v.getCliente() != null && v.getCliente().getId().equals(clienteId))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (sucursalId != null) {
            ventas = ventas.stream()
                .filter(v -> v.getBranch() != null && v.getBranch().getId().equals(sucursalId))
                .collect(java.util.stream.Collectors.toList());
        }
        
        model.addAttribute("ventas", ventas);
        model.addAttribute("clientes", usuarioService.getUsuariosByRol(Rol.CLIENTE));
        model.addAttribute("sucursales", branchRepository.findAll());
        model.addAttribute("fechaFiltro", fecha);
        model.addAttribute("clienteIdFiltro", clienteId);
        model.addAttribute("sucursalIdFiltro", sucursalId);
        model.addAttribute("limitActual", limit);
        return "admin/ventas/index";
    }

    @GetMapping("/nueva")
    public String nuevaVenta(Model model) {
        model.addAttribute("ventaForm", new VentaForm());
        // Cargar clientes y sucursales (datos pequeños)
        model.addAttribute("clientes", usuarioService.getUsuariosByRol(Rol.CLIENTE));
        model.addAttribute("sucursales", branchRepository.findAll());
        // Proveedores para filtrar sucursales
        model.addAttribute("proveedores", proveedorService.getAllProveedores());
        // Productos se cargarán dinámicamente via AJAX para mejor rendimiento
        return "admin/ventas/crear";
    }

    // Alias para mantener consistencia con el menú /admin/ventas/crear
    @GetMapping("/crear")
    public String crearVenta(Model model) {
        // Usar el nuevo formulario mejorado
        return "admin/ventas/crear-mejorado";
    }

    @PostMapping("/guardar")
    public String guardarVenta(@ModelAttribute VentaForm ventaForm, Model model) {
        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.ACTIVA); // Establecer estado inicial como ACTIVA

        if (ventaForm.getClienteId() != null) {
            Optional<Usuario> clienteOpt = usuarioService.getUsuarioById(ventaForm.getClienteId());
            clienteOpt.ifPresent(venta::setCliente);
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        if (ventaForm.getDetalles() != null) {
            for (DetalleVentaForm detalleForm : ventaForm.getDetalles()) {
                DetalleVenta det = new DetalleVenta();
                Producto pRef = new Producto();
                pRef.setId(detalleForm.getProductoId());
                det.setProducto(pRef);
                det.setCantidad(detalleForm.getCantidad());
                
                // Agregar branch al detalle si se proporciona
                if (detalleForm.getBranchId() != null) {
                    branchRepository.findById(detalleForm.getBranchId()).ifPresent(det::setBranch);
                }
                
                // Agregar precio de venta si se proporciona
                if (detalleForm.getPrecioVenta() != null) {
                    det.setPrecioUnitario(detalleForm.getPrecioVenta());
                }
                
                det.setVenta(venta);
                detalles.add(det);
            }
        }
        venta.setDetalles(detalles);

        try {
            ventaService.saveVenta(venta, ventaForm.getSucursalId());
            return "redirect:/admin/ventas";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            // recargar combos
            var productos = productoRepository.findAll().stream()
                    .filter(p -> p.getEstado() != null)
                    .toList();
            model.addAttribute("productos", productos);
            model.addAttribute("clientes", usuarioService.getUsuariosByRol(Rol.CLIENTE));
            model.addAttribute("sucursales", branchRepository.findAll());
            model.addAttribute("proveedores", proveedorService.getAllProveedores());
            model.addAttribute("ventaForm", ventaForm);
            return "admin/ventas/crear";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalleVenta(@PathVariable Long id, Model model) {
        var ventaOpt = ventaService.getVentaByIdWithDetalles(id);
        if (ventaOpt.isPresent()) {
            var venta = ventaOpt.get();
            model.addAttribute("venta", venta);
            var movimientos = movimientoInventarioRepository.findByVenta_IdOrderByFechaAsc(venta.getId());
            model.addAttribute("movimientos", movimientos);
            return "admin/ventas/detalle";
        } else {
            return "redirect:/admin/ventas";
        }
    }

    // Endpoint para reversar (anular) una venta: solo debe estar disponible para roles con permiso (control de seguridad se puede añadir via Spring Security)
    @PostMapping("/{id}/reversar")
    public String reversarVenta(@PathVariable Long id) {
        ventaService.reverseVenta(id);
        return "redirect:/admin/ventas/detalle/" + id;
    }

    // ========== API REST para formulario mejorado ==========
    
    @GetMapping("/api/clientes")
    @ResponseBody
    public List<java.util.Map<String, Object>> obtenerClientes(
            @RequestParam(required = false) String search) {
        
        var clientes = usuarioService.getUsuariosByRol(Rol.CLIENTE);
        
        // Filtrar por búsqueda si se proporciona
        if (search != null && !search.trim().isEmpty()) {
            final String query = search.toLowerCase();
            clientes = clientes.stream()
                .filter(c -> 
                    c.getNombre().toLowerCase().contains(query) ||
                    (c.getApellido() != null && c.getApellido().toLowerCase().contains(query)) ||
                    (c.getCorreo() != null && c.getCorreo().toLowerCase().contains(query)) ||
                    (c.getTelefono() != null && c.getTelefono().contains(query))
                )
                .limit(20)  // Limitar resultados para mejor rendimiento
                .collect(java.util.stream.Collectors.toList());
        }
        
        return clientes.stream()
            .map(c -> {
                java.util.Map<String, Object> dto = new java.util.HashMap<>();
                dto.put("id", c.getId());
                dto.put("nombre", c.getNombre());
                dto.put("apellido", c.getApellido());
                dto.put("correo", c.getCorreo());
                dto.put("telefono", c.getTelefono());
                return dto;
            }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/api/sucursales")
    @ResponseBody
    public List<java.util.Map<String, Object>> obtenerTodasSucursales() {
        return branchRepository.findAll().stream()
            .map(s -> {
                java.util.Map<String, Object> dto = new java.util.HashMap<>();
                dto.put("id", s.getId());
                dto.put("nombre", s.getNombre());
                dto.put("ciudad", s.getCiudad());
                dto.put("pais", s.getPais());
                return dto;
            }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/api/productos")
    @ResponseBody
    public List<java.util.Map<String, Object>> obtenerProductosActivos(
            @RequestParam(required = false) Long sucursalId) {
        
        var productos = productoRepository.findAll().stream()
            .filter(p -> p.getEstado() != null && !p.getEstado().isEmpty())
            .collect(java.util.stream.Collectors.toList());

        return productos.stream()
            .map(p -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", p.getId());
                m.put("nombre", p.getNombre());
                m.put("precioVenta", p.getPrecioVenta() != null ? p.getPrecioVenta() : 0.0);
                m.put("tipo", p.getTipo() != null ? p.getTipo() : "");
                // Usar stockMinimo como referencia o calcular stock total
                m.put("stock", p.getStockMinimo() != null ? p.getStockMinimo() : 0);
                return m;
            }).collect(java.util.stream.Collectors.toList());
    }
}
