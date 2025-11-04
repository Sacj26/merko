package merko.merko.ControllerWeb;

import merko.merko.Entity.Producto;
import merko.merko.Entity.Proveedor;
import merko.merko.Service.CompraService;
import merko.merko.Service.ProductoService;
import merko.merko.Service.ProveedorService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
// imports above already include UUID and List

import merko.merko.dto.ProductCreateDto;
import merko.merko.dto.ProductBranchAssignDto;
import merko.merko.Service.ProductoApiService;
import merko.merko.Repository.ProductBranchRepository;

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CompraService compraService;
    private final ProveedorService proveedorService;
    private final ProductoApiService productoApiService;
    private final ProductBranchRepository productBranchRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    public ProductoController(ProductoService productoService, CompraService compraService, ProveedorService proveedorService, ProductoApiService productoApiService, ProductBranchRepository productBranchRepository) {
        this.productoService = productoService;
        this.compraService = compraService;
        this.proveedorService = proveedorService;
        this.productoApiService = productoApiService;
        this.productBranchRepository = productBranchRepository;
    }


    @GetMapping
    public String listarProductos(@RequestParam(value = "proveedorId", required = false) Long proveedorId,
                                  @RequestParam(value = "q", required = false) String query,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                  @RequestParam(value = "sort", defaultValue = "nombre") String sort,
                                  @RequestParam(value = "dir", defaultValue = "asc") String dir,
                                  Model model) {

        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Allow sorting by safe fields only
        List<String> allowedSort = List.of("nombre", "precioVenta", "stock", "id");
        if (!allowedSort.contains(sort)) {
            sort = "nombre";
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, sort));

        Page<Producto> pageProductos = productoService.getProductos(query, proveedorId, pageable);

        model.addAttribute("page", pageProductos);
        model.addAttribute("productos", pageProductos.getContent());
        model.addAttribute("compras", compraService.getAllCompras());
        // Para filtros
        List<Proveedor> proveedores = proveedorService.getAllProveedores();
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("selectedProveedorId", proveedorId);
        model.addAttribute("q", query);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("size", size);

        return "admin/productos/list";
    }

    // Exportación CSV preservando filtros básicos (proveedorId, q). Paginación ignorada intencionalmente
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(value = "proveedorId", required = false) Long proveedorId,
                                              @RequestParam(value = "q", required = false) String query) {
        // Obtener todos los productos y aplicar filtros básicos en memoria (suficiente para volúmenes pequeños/medios)
        List<Producto> productos = productoService.getAllProductos();
        if (proveedorId != null) {
            productos = productos.stream()
                    .filter(p -> p.getProveedor() != null && proveedorId.equals(p.getProveedor().getId()))
                    .collect(Collectors.toList());
        }
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            productos = productos.stream()
                    .filter(p -> (p.getNombre() != null && p.getNombre().toLowerCase().contains(q))
                              || (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Nombre,Proveedor,Descripción,Precio,Stock\n");
        for (Producto p : productos) {
            String proveedorNombre = (p.getProveedor() != null && p.getProveedor().getNombre() != null) ? p.getProveedor().getNombre() : "";
            String nombre = p.getNombre() != null ? p.getNombre() : "";
            String desc = p.getDescripcion() != null ? p.getDescripcion().replaceAll("[\r\n]", " ") : "";
                            sb.append(p.getId() != null ? p.getId() : "").append(',')
                                .append('"').append(nombre.replace("\"", "\"\"")).append('"').append(',')
                                .append('"').append(proveedorNombre.replace("\"", "\"\"")).append('"').append(',')
                                .append('"').append(desc.replace("\"", "\"\"")).append('"').append(',')
                                .append(p.getPrecioVenta()).append(',')
                                .append(p.getStock()).append('\n');
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(data);
    }


    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProducto(
            @RequestParam(value = "proveedorId", required = false) Long proveedorId,
            @RequestParam(value = "branchId", required = false) Long branchId,
            Model model) {

        Producto producto = new Producto();
        if (proveedorId != null) {
            proveedorService.getProveedorById(proveedorId).ifPresent(p -> producto.setProveedor(p));
        }

    model.addAttribute("producto", producto);
    model.addAttribute("titulo", "Nuevo Producto");
    // Si abrimos la vista desde proveedor/sucursal queremos enviar al endpoint batch
    model.addAttribute("accion", "/admin/productos/guardar-batch");
        model.addAttribute("proveedores", proveedorService.getAllProveedores());
        model.addAttribute("selectedProveedorId", proveedorId);
        model.addAttribute("selectedBranchId", branchId);
        // Si se proporciona proveedorId o branchId, reutilizamos el diseño de 'agregar-productos'
        // que usa el CSS/UX que solicitaste (misma vista que se usaba desde proveedores).
        if (proveedorId != null || branchId != null) {
            proveedorService.getProveedorById(proveedorId).ifPresent(p -> {
                model.addAttribute("proveedor", p);
                if (branchId != null && p.getBranches() != null) {
                    p.getBranches().stream()
                            .filter(b -> b.getId() != null && b.getId().equals(branchId))
                            .findFirst()
                            .ifPresent(b -> model.addAttribute("branch", b));
                }
            });
            // la plantilla 'admin/proveedores/agregar-productos' usa 'accion' como action del form
            return "admin/proveedores/agregar-productos";
        }

        return "admin/productos/form";
    }


    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  @RequestParam("imagen") MultipartFile imagen,
                                  @RequestParam(value = "branchId", required = false) Long branchId) {

        if (!imagen.isEmpty()) {
            try {
                String nombreArchivo = UUID.randomUUID() + "_" + imagen.getOriginalFilename();
                Path ruta = Paths.get("src/main/resources/static/images/" + nombreArchivo);
                Files.write(ruta, imagen.getBytes());

                producto.setImagenUrl("/images/" + nombreArchivo);
            } catch (IOException e) {
                log.error("Error al guardar imagen de producto", e);
            }
        }

        productoService.saveProducto(producto);

        // Nota: la asociación de un producto a una sucursal (product_branch) debe implementarse
        // mediante un servicio adicional. Por ahora, guardamos el producto y redirigimos a la lista.
        // Si se proporciona branchId, lo registramos en logs para seguimiento.
        if (branchId != null) {
            log.info("Producto creado con id={} y se recibió branchId={}", producto.getId(), branchId);
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/guardar-batch")
    public String guardarProductosBatch(MultipartHttpServletRequest request) {
        // Parse parameters productos[i].field
        Map<String, String[]> paramMap = request.getParameterMap();
        Pattern pattern = Pattern.compile("^productos\\[(\\d+)\\]\\.([a-zA-Z0-9_]+)$");
        Map<Integer, ProductCreateDto> indexed = new TreeMap<>();

        for (String name : paramMap.keySet()) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                int idx = Integer.parseInt(m.group(1));
                String field = m.group(2);
                String val = request.getParameter(name);
                ProductCreateDto dto = indexed.computeIfAbsent(idx, k -> new ProductCreateDto());
                switch (field) {
                    case "nombre": dto.nombre = val; break;
                    case "descripcion": dto.descripcion = val; break;
                    case "precioVenta":
                        try { dto.precioVenta = val != null && !val.isEmpty() ? new java.math.BigDecimal(val) : null; } catch (Exception ignored) {}
                        break;
                    case "precioCompra":
                        try { dto.precioCompra = val != null && !val.isEmpty() ? new java.math.BigDecimal(val) : null; } catch (Exception ignored) {}
                        break;
                    case "stock":
                        try { dto.stock = val != null && !val.isEmpty() ? Integer.parseInt(val) : null; } catch (Exception ignored) {}
                        break;
                    case "stockMinimo":
                        try { dto.stockMinimo = val != null && !val.isEmpty() ? Integer.parseInt(val) : null; } catch (Exception ignored) {}
                        break;
                    case "puntoReorden":
                        try { dto.puntoReorden = val != null && !val.isEmpty() ? Integer.parseInt(val) : null; } catch (Exception ignored) {}
                        break;
                    case "leadTimeDias":
                        try { dto.leadTimeDias = val != null && !val.isEmpty() ? Integer.parseInt(val) : null; } catch (Exception ignored) {}
                        break;
                    case "marca": dto.marca = val; break;
                    case "tipo": dto.tipo = val; break;
                    case "estado": dto.estado = val; break;
                    case "unidadMedida": dto.unidadMedida = val; break;
                    case "contenidoNeto":
                        try { dto.contenidoNeto = val != null && !val.isEmpty() ? Double.parseDouble(val) : null; } catch (Exception ignored) {}
                        break;
                    case "contenidoUoM": dto.contenidoUoM = val; break;
                    case "gestionaLotes": dto.gestionaLotes = "true".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val); break;
                    case "requiereVencimiento": dto.requiereVencimiento = "true".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val); break;
                    case "vidaUtilDias":
                        try { dto.vidaUtilDias = val != null && !val.isEmpty() ? Integer.parseInt(val) : null; } catch (Exception ignored) {}
                        break;
                    case "almacenamiento": dto.almacenamiento = val; break;
                    case "registroSanitario": dto.registroSanitario = val; break;
                    case "sku": dto.sku = val; break;
                    case "codigoBarras": dto.codigoBarras = val; break;
                    case "proveedorId":
                        try {
                            if (val != null && !val.isEmpty()) dto.proveedorId = Long.parseLong(val);
                        } catch (NumberFormatException ignored) {}
                        break;
                    default: break;
                }
            }
        }

        // Manejar archivos: productos[i].imagenUrl
        for (Map.Entry<String, MultipartFile> fileEntry : request.getFileMap().entrySet()) {
            String name = fileEntry.getKey();
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                int idx = Integer.parseInt(m.group(1));
                MultipartFile mf = fileEntry.getValue();
                if (mf != null && !mf.isEmpty()) {
                    try {
                        String nombreArchivo = UUID.randomUUID() + "_" + mf.getOriginalFilename();
                        Path ruta = Paths.get("src/main/resources/static/images/" + nombreArchivo);
                        Files.write(ruta, mf.getBytes());
                        ProductCreateDto dto = indexed.computeIfAbsent(idx, k -> new ProductCreateDto());
                        dto.imagenUrl = "/images/" + nombreArchivo;
                    } catch (IOException ex) {
                        log.error("Error al guardar imagen batch", ex);
                    }
                }
            }
        }

        // collect list ordered
        // Si se proporcionó proveedorId o branchId a nivel formulario, propagar a cada DTO
        String proveedorTop = request.getParameter("proveedorId");
        String branchTop = request.getParameter("branchId");
        for (Map.Entry<Integer, ProductCreateDto> e : indexed.entrySet()) {
            ProductCreateDto dto = e.getValue();
            if ((dto.proveedorId == null || dto.proveedorId == 0L) && proveedorTop != null && !proveedorTop.isEmpty()) {
                try { dto.proveedorId = Long.parseLong(proveedorTop); } catch (NumberFormatException ignored) {}
            }
            if (branchTop != null && !branchTop.isEmpty()) {
                // si hay stock para este producto, crear asignación por sucursal
                String stockParam = request.getParameter("productos[" + e.getKey() + "].stock");
                if (stockParam != null && !stockParam.isEmpty()) {
                    try {
                        ProductBranchAssignDto assign = new ProductBranchAssignDto();
                        assign.branchId = Long.parseLong(branchTop);
                        assign.stock = Integer.parseInt(stockParam);
                        dto.stockPorSucursal = java.util.Collections.singletonList(assign);
                    } catch (Exception ignored) {}
                }
            }
        }

        List<ProductCreateDto> dtos = indexed.values().stream().collect(Collectors.toList());

        if (!dtos.isEmpty()) {
            productoApiService.crearProductos(dtos);
        }

        return "redirect:/admin/productos";
    }


    @GetMapping("/ver/{id}")
    public String verProducto(@PathVariable Long id, Model model) {
        Producto producto = productoService.getProductoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
        model.addAttribute("producto", producto);
        // Agregar datos por sucursal si existen (ProductBranch)
        try {
            List<merko.merko.Entity.ProductBranch> pbs = productBranchRepository.findAll().stream()
                    .filter(pb -> pb.getProducto() != null && pb.getProducto().getId() != null && pb.getProducto().getId().equals(id))
                    .collect(Collectors.toList());
            model.addAttribute("productBranches", pbs);
        } catch (Exception ex) {
            log.debug("No se pudieron recuperar ProductBranch para producto {}: {}", id, ex.getMessage());
        }
        return "admin/productos/ver";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoService.getProductoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
        model.addAttribute("producto", producto);
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("accion", "/admin/productos/actualizar/" + id);
        model.addAttribute("proveedores", proveedorService.getAllProveedores());
        model.addAttribute("categorias", Arrays.asList("Electrónica", "Ropa", "Alimentos", "Hogar", "Deportes"));
        return "admin/productos/editar";
    }


    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(@PathVariable Long id,
                                     @ModelAttribute Producto productoActualizado) {
    productoService.updateProducto(id, productoActualizado);
        return "redirect:/admin/productos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        productoService.deleteProducto(id);
        return "redirect:/admin/productos";
    }

    // Nuevo: API para cargar productos por proveedor (JSON liviano)
    @GetMapping(value = "/por-proveedor/{proveedorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> productosPorProveedor(@PathVariable("proveedorId") Long proveedorId) {
        return productoService.getAllProductos().stream()
                .filter(p -> p.getProveedor() != null && p.getProveedor().getId() != null && p.getProveedor().getId().equals(proveedorId))
                .filter(p -> p.getEstado() == merko.merko.Entity.EstadoProducto.ACTIVO)
                // Permitir tanto materias primas como productos terminados
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("nombre", p.getNombre());
                    m.put("precioVenta", p.getPrecioVenta());
                    m.put("precioCompra", p.getPrecioCompra());
                    m.put("tipo", p.getTipo() != null ? p.getTipo().name() : null);
                    m.put("stock", p.getStock());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
