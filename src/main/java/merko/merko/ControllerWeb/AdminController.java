package merko.merko.ControllerWeb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import merko.merko.Service.DashboardService;
import merko.merko.Service.ProductoService;
import merko.merko.Service.UsuarioService;
import merko.merko.Service.VentaService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final DashboardService dashboardService;
    private final merko.merko.Repository.CompraRepository compraRepository;
    private final merko.merko.Repository.DetalleCompraRepository detalleCompraRepository;

    public AdminController(VentaService ventaService, ProductoService productoService, UsuarioService usuarioService, 
                          DashboardService dashboardService,
                          merko.merko.Repository.CompraRepository compraRepository,
                          merko.merko.Repository.DetalleCompraRepository detalleCompraRepository) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.dashboardService = dashboardService;
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
    }

    @GetMapping
    public String rootAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Accediendo a /admin con usuario: {}", auth.getName());
        logger.info("Autoridades del usuario: {}", auth.getAuthorities());
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String panelAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Accediendo a /admin/dashboard con usuario: {}", auth.getName());
        logger.info("Autoridades del usuario: {}", auth.getAuthorities());

        // Datos para el dashboard existentes
        int totalVentas = ventaService.getAllVentas().size();
        int totalProductos = productoService.getAllProductos().size();
        int totalClientes = (int) usuarioService.getAllUsuarios().stream().filter(u -> u.getRol() != null).count();

        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalClientes", totalClientes);

        // Últimas ventas (limitar a 5)
        var ventas = ventaService.getAllVentas();
        if (ventas.size() > 5) {
            model.addAttribute("ultimasVentas", ventas.subList(0, 5));
        } else {
            model.addAttribute("ultimasVentas", ventas);
        }

    // Nuevos KPIs
        Map<String, Object> kpis = dashboardService.kpis();
        logger.info("KPIs obtenidos del servicio: {}", kpis);
        model.addAllAttributes(kpis);

        // Alertas
        var stockCrit = dashboardService.stockCritico(5);
            var proxVenc = dashboardService.proximosVencimientos(30, 5);
            logger.info("Stock crítico: {} items, Próximos vencimientos: {} items", stockCrit.size(), proxVenc.size());
            model.addAttribute("stockCritico", stockCrit);

            // Expandir información de próximos vencimientos para incluir sucursales donde el producto está asignado
            java.util.List<java.util.Map<String, Object>> proxWithBranches = new java.util.ArrayList<>();
            // We will use Spring injection by looking up the bean from application context via repository class autowiring pattern
            try {
                org.springframework.context.ApplicationContext ctx = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes)org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext());
                merko.merko.Repository.ProductBranchRepository productBranchRepository = ctx.getBean(merko.merko.Repository.ProductBranchRepository.class);
                for (merko.merko.Entity.Lote l : proxVenc) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("lote", l);
                    java.util.List<merko.merko.Entity.ProductBranch> pbs = productBranchRepository.findByProductoId(l.getProducto().getId());
                    java.util.List<java.util.Map<String, Object>> branches = new java.util.ArrayList<>();
                    for (merko.merko.Entity.ProductBranch pb : pbs) {
                        java.util.Map<String, Object> b = new java.util.HashMap<>();
                        if (pb.getBranch() != null) {
                            b.put("id", pb.getBranch().getId());
                            b.put("nombre", pb.getBranch().getNombre());
                            b.put("ciudad", pb.getBranch().getCiudad());
                            b.put("pais", pb.getBranch().getPais());
                        }
                        branches.add(b);
                    }
                    m.put("branches", branches);
                    proxWithBranches.add(m);
                }
            } catch (Exception ex) {
                // fallback: attach empty branches
                for (merko.merko.Entity.Lote l : proxVenc) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("lote", l);
                    m.put("branches", java.util.Collections.emptyList());
                    proxWithBranches.add(m);
                }
            }

            model.addAttribute("proximosVencimientos", proxWithBranches);

        return "admin/dashboard/index";
    }

    // Endpoints JSON para gráficos del dashboard
    @GetMapping("/dashboard/api/ventas-diarias")
    @ResponseBody
    public List<Map<String, Object>> apiVentasDiarias(@RequestParam(defaultValue = "30") int dias) {
        return dashboardService.ventasDiarias(dias);
    }

    @GetMapping("/dashboard/api/top-productos")
    @ResponseBody
    public List<Map<String, Object>> apiTopProductos(@RequestParam(defaultValue = "30") int dias,
                                                     @RequestParam(defaultValue = "5") int n) {
        return dashboardService.topProductos(dias, n);
    }

    @GetMapping("/dashboard/api/compras-diarias")
    @ResponseBody
    public List<Map<String, Object>> apiComprasDiarias(@RequestParam(defaultValue = "30") int dias) {
        logger.info("=== API compras-diarias llamada con dias={}", dias);
        
        // Implementación directa y simple
        java.time.LocalDate fin = java.time.LocalDate.now();
        java.time.LocalDate inicio = fin.minusDays(dias - 1L);
        
        // Obtener todas las compras del periodo
        List<merko.merko.Entity.Compra> compras = compraRepository.findAll().stream()
            .filter(c -> c.getFecha() != null && 
                        !c.getFecha().isBefore(inicio) && 
                        !c.getFecha().isAfter(fin))
            .toList();
        
        logger.info("=== Compras encontradas en el periodo: {}", compras.size());
        
        // Agrupar por fecha
        Map<java.time.LocalDate, Double> totalesPorFecha = new java.util.HashMap<>();
        for (merko.merko.Entity.Compra c : compras) {
            java.time.LocalDate fecha = c.getFecha();
            double total = c.getTotal();
            totalesPorFecha.merge(fecha, total, Double::sum);
            logger.debug("Compra ID {} - Fecha: {} - Total: {}", c.getId(), fecha, total);
        }
        
        // Crear resultado con todos los días
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ISO_DATE;
        for (java.time.LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("fecha", d.format(fmt));
            item.put("total", totalesPorFecha.getOrDefault(d, 0.0));
            result.add(item);
        }
        
        logger.info("=== API compras-diarias retorna {} registros", result.size());
        return result;
    }

    @GetMapping("/dashboard/api/top-productos-compras")
    @ResponseBody
    public List<Map<String, Object>> apiTopProductosCompras(@RequestParam(defaultValue = "30") int dias,
                                                            @RequestParam(defaultValue = "5") int n) {
        logger.info("=== API top-productos-compras llamada con dias={}, n={}", dias, n);
        
        // Implementación directa y simple
        java.time.LocalDate fin = java.time.LocalDate.now();
        java.time.LocalDate inicio = fin.minusDays(dias - 1L);
        
        // Obtener todos los detalles de compra del periodo
        List<merko.merko.Entity.DetalleCompra> detalles = detalleCompraRepository.findAll().stream()
            .filter(dc -> dc.getCompra() != null && 
                         dc.getCompra().getFecha() != null &&
                         !dc.getCompra().getFecha().isBefore(inicio) && 
                         !dc.getCompra().getFecha().isAfter(fin))
            .toList();
        
        logger.info("=== Detalles de compra encontrados en el periodo: {}", detalles.size());
        
        // Agrupar por producto y sumar cantidades
        Map<Long, Integer> cantidadPorProducto = new java.util.HashMap<>();
        Map<Long, String> nombrePorProducto = new java.util.HashMap<>();
        
        for (merko.merko.Entity.DetalleCompra dc : detalles) {
            if (dc.getProducto() != null) {
                Long prodId = dc.getProducto().getId();
                String prodNombre = dc.getProducto().getNombre();
                int cantidad = dc.getCantidad();
                
                cantidadPorProducto.merge(prodId, cantidad, Integer::sum);
                nombrePorProducto.put(prodId, prodNombre);
                
                logger.debug("Detalle - Producto: {} - Cantidad: {}", prodNombre, cantidad);
            }
        }
        
        // Ordenar por cantidad descendente y tomar los top N
        List<Map<String, Object>> result = cantidadPorProducto.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(n)
            .map(entry -> {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("productoId", entry.getKey());
                m.put("nombre", nombrePorProducto.get(entry.getKey()));
                m.put("cantidad", entry.getValue());
                return m;
            })
            .toList();
        
        logger.info("=== API top-productos-compras retorna {} productos", result.size());
        return result;
    }
}
