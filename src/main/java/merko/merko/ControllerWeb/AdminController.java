package merko.merko.ControllerWeb;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import merko.merko.Entity.Venta;
import merko.merko.Service.DashboardService;
import merko.merko.Service.ProductoService;
import merko.merko.Service.UsuarioService;
import merko.merko.Service.VentaService;

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
    private final merko.merko.Repository.VentaRepository ventaRepository;

    public AdminController(VentaService ventaService, ProductoService productoService, UsuarioService usuarioService, 
                          DashboardService dashboardService,
                          merko.merko.Repository.CompraRepository compraRepository,
                          merko.merko.Repository.DetalleCompraRepository detalleCompraRepository,
                          merko.merko.Repository.VentaRepository ventaRepository) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.dashboardService = dashboardService;
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.ventaRepository = ventaRepository;
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

        // Datos para el dashboard existentes (optimizado con count())
        long totalVentas = ventaService.countAll();
        long totalProductos = productoService.countAll();
        // BD simplificada: Usuario sin campo rol
        long totalClientes = usuarioService.countAll();

        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalClientes", totalClientes);

        // Últimas ventas (solo 5) - OPTIMIZADO: no carga todas las ventas
        List<Long> ventaIds = ventaRepository.findTop5IdsByOrderByFechaDesc();
        List<Venta> ultimasVentas = ventaIds.isEmpty() ? Collections.emptyList() : 
            ventaRepository.findByIdsWithAllRelations(ventaIds);
        model.addAttribute("ultimasVentas", ultimasVentas);

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
                    // Lote tiene FK a producto_id
                    Long productoId = (l.getProducto() != null) ? l.getProducto().getId() : null;
                    java.util.List<merko.merko.Entity.ProductBranch> pbs = (productoId != null) 
                        ? productBranchRepository.findByProductoId(productoId) 
                        : new java.util.ArrayList<>();
                    java.util.List<java.util.Map<String, Object>> branches = new java.util.ArrayList<>();
                    for (merko.merko.Entity.ProductBranch pb : pbs) {
                        java.util.Map<String, Object> b = new java.util.HashMap<>();
                        if (pb.getBranch() != null) {
                            b.put("id", pb.getBranch().getId());
                            b.put("nombre", pb.getBranch().getNombre());
                            b.put("direccion", pb.getBranch().getDireccion());
                            // BD simplificada: Branch solo tiene id, nombre, direccion
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
        
        // Obtener todas las compras del periodo (Compra.fecha ahora es LocalDateTime)
        List<merko.merko.Entity.Compra> compras = compraRepository.findAll().stream()
            .filter(c -> c.getFecha() != null)
            .filter(c -> {
                java.time.LocalDate fecha = c.getFecha().toLocalDate();
                return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
            })
            .toList();
        
        logger.info("=== Compras encontradas en el periodo: {}", compras.size());
        
        // Agrupar por fecha
        Map<java.time.LocalDate, Double> totalesPorFecha = new java.util.HashMap<>();
        for (merko.merko.Entity.Compra c : compras) {
            java.time.LocalDate fecha = c.getFecha().toLocalDate();
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
        
        // Convertir LocalDate a LocalDateTime para la consulta optimizada
        java.time.LocalDate fin = java.time.LocalDate.now();
        java.time.LocalDate inicio = fin.minusDays(dias - 1L);
        java.time.LocalDateTime start = inicio.atStartOfDay();
        java.time.LocalDateTime end = fin.atTime(23, 59, 59, 999_999_999);
        
        // OPTIMIZADO: Usar query SQL que filtra en la base de datos
        List<Object[]> rows = detalleCompraRepository.topProductosCompradosPorCantidad(start, end);
        
        logger.info("=== Productos encontrados: {}", rows.size());
        
        // Convertir resultado a formato esperado y tomar top N
        List<Map<String, Object>> resultado = rows.stream()
            .limit(n)
            .map(row -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("productoId", row[0]);
                map.put("nombre", row[1]);
                map.put("cantidad", ((Number) row[2]).intValue());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        
        logger.info("=== Top {} productos comprados devueltos", resultado.size());
        return resultado;
    }
}
