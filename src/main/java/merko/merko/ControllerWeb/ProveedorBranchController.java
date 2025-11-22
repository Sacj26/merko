package merko.merko.ControllerWeb;

import merko.merko.Entity.Branch;
import merko.merko.Entity.Producto;
import merko.merko.Entity.ProductBranch;
import merko.merko.Entity.Proveedor;
import merko.merko.Entity.TipoAlmacenamiento;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.ProductBranchRepository;
import merko.merko.Service.ProductoService;
import merko.merko.Service.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/proveedores/{proveedorId}/sucursales")
public class ProveedorBranchController {

    private final ProveedorService proveedorService;
    private final BranchRepository branchRepository;
    private final ProductBranchRepository productBranchRepository;
    private final ProductoService productoService;

    public ProveedorBranchController(ProveedorService proveedorService, BranchRepository branchRepository, ProductBranchRepository productBranchRepository, ProductoService productoService) {
        this.proveedorService = proveedorService;
        this.branchRepository = branchRepository;
        this.productBranchRepository = productBranchRepository;
        this.productoService = productoService;
    }

    @GetMapping
    public String listar(@PathVariable Long proveedorId, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        List<Branch> branches = proveedor.getBranches();
        
        // Crear un mapa con el conteo de productos por sucursal
        java.util.Map<Long, Long> productCounts = new java.util.HashMap<>();
        int totalProductos = 0;
        int totalContactos = 0;
        
        for (Branch branch : branches) {
            long count = productBranchRepository.findByBranchId(branch.getId()).size();
            productCounts.put(branch.getId(), count);
            totalProductos += count;
            
            if (branch.getContacts() != null) {
                totalContactos += branch.getContacts().size();
            }
        }
        
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branches", branches);
        model.addAttribute("productCounts", productCounts);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalContactos", totalContactos);
        return "admin/proveedores/sucursales/index";
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
        model.addAttribute("accion", "/admin/proveedores/" + proveedorId + "/sucursales/" + branchId + "/agregar-productos");
        return "admin/proveedores/agregar-productos";
    }

    @PostMapping("/{branchId}/agregar-productos")
    public String guardarProductosSucursal(
            @PathVariable Long proveedorId,
            @PathVariable Long branchId,
            @RequestParam Map<String, String> allParams,
            @RequestParam Map<String, MultipartFile> files,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));

        List<Producto> productos = new ArrayList<>();
        int index = 0;
        
        while (allParams.containsKey("productos[" + index + "].nombre")) {
            String nombre = allParams.get("productos[" + index + "].nombre");
            
            if (nombre != null && !nombre.trim().isEmpty()) {
                Producto producto = new Producto();
                producto.setNombre(nombre);
                producto.setDescripcion(allParams.get("productos[" + index + "].descripcion"));
                
                String sku = allParams.get("productos[" + index + "].sku");
                producto.setSku((sku != null && !sku.trim().isEmpty()) ? sku.trim() : null);
                String codigoBarras = allParams.get("productos[" + index + "].codigoBarras");
                producto.setCodigoBarras((codigoBarras != null && !codigoBarras.trim().isEmpty()) ? codigoBarras.trim() : null);
                String marca = allParams.get("productos[" + index + "].marca");
                producto.setMarca((marca != null && !marca.trim().isEmpty()) ? marca.trim() : null);

                String tipoStr = allParams.get("productos[" + index + "].tipo");
                if (tipoStr != null && !tipoStr.trim().isEmpty()) {
                    producto.setTipo(tipoStr.trim());
                }
                String estadoStr = allParams.get("productos[" + index + "].estado");
                if (estadoStr != null && !estadoStr.trim().isEmpty()) {
                    producto.setEstado(estadoStr.trim());
                }

                String unidadStr = allParams.get("productos[" + index + "].unidadMedida");
                if (unidadStr != null && !unidadStr.trim().isEmpty()) {
                    producto.setUnidadMedida(unidadStr.trim());
                }
                String contenidoNetoStr = allParams.get("productos[" + index + "].contenidoNeto");
                if (contenidoNetoStr != null && !contenidoNetoStr.trim().isEmpty()) {
                    try { producto.setContenidoNeto(Double.valueOf(contenidoNetoStr)); } catch (NumberFormatException ignored) {}
                }
                String contenidoUomStr = allParams.get("productos[" + index + "].contenidoUom");
                if (contenidoUomStr != null && !contenidoUomStr.trim().isEmpty()) {
                    producto.setContenidoUom(contenidoUomStr.trim());
                }
                
                try {
                    producto.setPrecioCompra(Double.parseDouble(allParams.get("productos[" + index + "].precioCompra")));
                    producto.setPrecioVenta(Double.parseDouble(allParams.get("productos[" + index + "].precioVenta")));
                } catch (NumberFormatException e) {
                    model.addAttribute("error", "Error en los valores numéricos del producto " + (index + 1));
                    model.addAttribute("proveedor", proveedor);
                    model.addAttribute("branch", branch);
                    return "admin/proveedores/agregar-productos";
                }

                String stockMinStr = allParams.get("productos[" + index + "].stockMinimo");
                if (stockMinStr != null && !stockMinStr.trim().isEmpty()) {
                    try { producto.setStockMinimo(Integer.valueOf(stockMinStr)); } catch (NumberFormatException ignored) {}
                }
                String puntoReordenStr = allParams.get("productos[" + index + "].puntoReorden");
                if (puntoReordenStr != null && !puntoReordenStr.trim().isEmpty()) {
                    try { producto.setPuntoReorden(Integer.valueOf(puntoReordenStr)); } catch (NumberFormatException ignored) {}
                }
                String leadTimeStr = allParams.get("productos[" + index + "].leadTimeDias");
                if (leadTimeStr != null && !leadTimeStr.trim().isEmpty()) {
                    try { producto.setLeadTimeDias(Integer.valueOf(leadTimeStr)); } catch (NumberFormatException ignored) {}
                }

                producto.setGestionaLotes(allParams.get("productos[" + index + "].gestionaLotes") != null);
                producto.setRequiereVencimiento(allParams.get("productos[" + index + "].requiereVencimiento") != null);
                String vidaUtilStr = allParams.get("productos[" + index + "].vidaUtilDias");
                if (vidaUtilStr != null && !vidaUtilStr.trim().isEmpty()) {
                    try { producto.setVidaUtilDias(Integer.valueOf(vidaUtilStr)); } catch (NumberFormatException ignored) {}
                }
                String almacStr = allParams.get("productos[" + index + "].almacenamiento");
                if (almacStr != null && !almacStr.trim().isEmpty()) {
                    try { producto.setAlmacenamiento(TipoAlmacenamiento.valueOf(almacStr)); } catch (IllegalArgumentException ignored) {}
                }
                String regSan = allParams.get("productos[" + index + "].registroSanitario");
                producto.setRegistroSanitario((regSan != null && !regSan.trim().isEmpty()) ? regSan.trim() : null);
                
                MultipartFile imagenArchivo = files.get("productos[" + index + "].imagenUrl");
                if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                    String rutaImagen = proveedorService.guardarImagenProducto(imagenArchivo);
                    producto.setImagenUrl(rutaImagen);
                }
                
                productoService.saveProducto(producto);
                productos.add(producto);

                // Crear relación ProductBranch con la sucursal
                ProductBranch pb = new ProductBranch();
                pb.setProducto(producto);
                pb.setBranch(branch);
                String stockStr = allParams.get("productos[" + index + "].stock");
                pb.setStock(stockStr != null && !stockStr.trim().isEmpty() ? Integer.parseInt(stockStr) : 0);
                productBranchRepository.save(pb);
            }
            index++;
        }

        if (productos.isEmpty()) {
            model.addAttribute("error", "Debe agregar al menos un producto");
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("branch", branch);
            return "admin/proveedores/agregar-productos";
        }

        redirectAttributes.addFlashAttribute("mensaje", productos.size() + " producto(s) agregado(s) a la sucursal " + branch.getNombre());
        return "redirect:/admin/proveedores/" + proveedorId + "/sucursales/" + branchId + "/productos";
    }

    @GetMapping("/{branchId}/productos")
    public String listarProductosSucursal(@PathVariable Long proveedorId,
                                         @PathVariable Long branchId,
                                         Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        
        List<ProductBranch> productBranches = productBranchRepository.findByBranchId(branchId);
        
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("branch", branch);
        model.addAttribute("productBranches", productBranches);
        
        return "admin/proveedores/sucursales/productos/index";
    }

    @PostMapping("/{branchId}/productos/{pbId}/eliminar")
    public String eliminarProductoDeSucursal(@PathVariable Long proveedorId,
                                            @PathVariable Long branchId,
                                            @PathVariable Long pbId,
                                            RedirectAttributes redirectAttributes) {
        productBranchRepository.deleteById(pbId);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado de la sucursal");
        return "redirect:/admin/proveedores/" + proveedorId + "/sucursales/" + branchId + "/productos";
    }
}
