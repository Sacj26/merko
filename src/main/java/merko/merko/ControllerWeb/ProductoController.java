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

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CompraService compraService;
    private final ProveedorService proveedorService;
    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    public ProductoController(ProductoService productoService, CompraService compraService, ProveedorService proveedorService) {
        this.productoService = productoService;
        this.compraService = compraService;
        this.proveedorService = proveedorService;
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
    public String mostrarFormularioNuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Nuevo Producto");
        model.addAttribute("accion", "/admin/productos/guardar");
        model.addAttribute("proveedores", proveedorService.getAllProveedores());
        return "admin/productos/form";
    }


    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  @RequestParam("imagen") MultipartFile imagen) {

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
        return "redirect:/admin/productos";
    }


    @GetMapping("/ver/{id}")
    public String verProducto(@PathVariable Long id, Model model) {
        Producto producto = productoService.getProductoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
        model.addAttribute("producto", producto);
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
