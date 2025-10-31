package merko.merko.ControllerWeb;

import java.time.LocalDate;
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

import merko.merko.Entity.DetalleVenta;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Rol;
import merko.merko.Entity.Usuario;
import merko.merko.Entity.Venta;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.MovimientoInventarioRepository;
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
    private UsuarioService usuarioService;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @GetMapping
    public String listarVentas(Model model) {
        List<Venta> ventas = ventaService.getAllVentas();
        model.addAttribute("ventas", ventas);
        return "admin/ventas/index";
    }

    @GetMapping("/nueva")
    public String nuevaVenta(Model model) {
        model.addAttribute("ventaForm", new VentaForm());
        // Mostrar sólo productos terminados activos para venta
        var productos = productoRepository.findAll().stream()
                .filter(p -> p.getEstado() == merko.merko.Entity.EstadoProducto.ACTIVO)
                .filter(p -> p.getTipo() == merko.merko.Entity.TipoProducto.PRODUCTO_TERMINADO)
                .toList();
        model.addAttribute("productos", productos);

        model.addAttribute("clientes", usuarioService.getUsuariosByRol(Rol.CLIENTE));

        return "admin/ventas/crear";
    }

    // Alias para mantener consistencia con el menú /admin/ventas/crear
    @GetMapping("/crear")
    public String crearVenta(Model model) {
        return nuevaVenta(model);
    }

    @PostMapping("/guardar")
    public String guardarVenta(@ModelAttribute VentaForm ventaForm, Model model) {
        Venta venta = new Venta();
        venta.setFecha(LocalDate.now());

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
                det.setVenta(venta);
                detalles.add(det);
            }
        }
        venta.setDetalles(detalles);

        try {
            ventaService.saveVenta(venta);
            return "redirect:/admin/ventas";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            // recargar combos
            var productos = productoRepository.findAll().stream()
                    .filter(p -> p.getEstado() == merko.merko.Entity.EstadoProducto.ACTIVO)
                    .filter(p -> p.getTipo() == merko.merko.Entity.TipoProducto.PRODUCTO_TERMINADO)
                    .toList();
            model.addAttribute("productos", productos);
            model.addAttribute("clientes", usuarioService.getUsuariosByRol(Rol.CLIENTE));
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
}
