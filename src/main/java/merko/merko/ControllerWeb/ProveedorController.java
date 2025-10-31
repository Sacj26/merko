package merko.merko.ControllerWeb;

import jakarta.validation.Valid;
import merko.merko.Entity.Producto;
import merko.merko.Entity.TipoProducto;
import merko.merko.Entity.EstadoProducto;
import merko.merko.Entity.UnidadMedida;
import merko.merko.Entity.Almacenamiento;
import merko.merko.Entity.Proveedor;
import merko.merko.Service.ProductoService;
import merko.merko.Service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.getAllProveedores());
        return "admin/proveedores/list";
    }

    @GetMapping("/ver/{id}")
    public String verProveedor(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        model.addAttribute("proveedor", proveedor);
        return "admin/proveedores/ver";
    }

    @GetMapping("/nuevo")
    public String nuevoProveedor(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("producto", new Producto());
        return "admin/proveedores/form";
    }

    @PostMapping("/guardar-con-producto")
    public String guardarProveedorConProducto(
            @RequestParam Map<String, String> allParams,
            @RequestParam Map<String, MultipartFile> files,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // Construir objeto Proveedor manualmente desde parámetros
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(allParams.get("nombre"));
        proveedor.setNit(allParams.get("nit"));
        proveedor.setTelefono(allParams.get("telefono"));
        proveedor.setDireccion(allParams.get("direccion"));
        
        // Campos opcionales
        String email = allParams.get("email");
        proveedor.setEmail((email != null && !email.trim().isEmpty()) ? email : null);
        
        proveedor.setCiudad(allParams.get("ciudad"));
        proveedor.setPais(allParams.get("pais"));
        proveedor.setDescripcion(allParams.get("descripcion"));
        proveedor.setNombreContacto(allParams.get("nombreContacto"));
        proveedor.setCargoContacto(allParams.get("cargoContacto"));
        proveedor.setTelefonoContacto(allParams.get("telefonoContacto"));
        
        String emailContacto = allParams.get("emailContacto");
        proveedor.setEmailContacto((emailContacto != null && !emailContacto.trim().isEmpty()) ? emailContacto : null);
        
        proveedor.setFechaRegistro(LocalDate.now());
        proveedor.setActivo(true);

        // Validar campos obligatorios manualmente
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            model.addAttribute("error", "El nombre del proveedor es obligatorio");
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("producto", new Producto());
            return "admin/proveedores/form";
        }
        if (proveedor.getNit() == null || proveedor.getNit().trim().isEmpty()) {
            model.addAttribute("error", "El NIT/RUC es obligatorio");
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("producto", new Producto());
            return "admin/proveedores/form";
        }
        if (proveedor.getTelefono() == null || proveedor.getTelefono().trim().isEmpty()) {
            model.addAttribute("error", "El teléfono es obligatorio");
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("producto", new Producto());
            return "admin/proveedores/form";
        }
        if (proveedor.getDireccion() == null || proveedor.getDireccion().trim().isEmpty()) {
            model.addAttribute("error", "La dirección es obligatoria");
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("producto", new Producto());
            return "admin/proveedores/form";
        }

        // Procesar múltiples productos
        List<Producto> productos = new ArrayList<>();
        int index = 0;
        
        while (allParams.containsKey("productos[" + index + "].nombre")) {
            String nombre = allParams.get("productos[" + index + "].nombre");
            
            if (nombre != null && !nombre.trim().isEmpty()) {
                Producto producto = new Producto();
                producto.setNombre(nombre);
                producto.setDescripcion(allParams.get("productos[" + index + "].descripcion"));

                // Identificación comercial
                String sku = allParams.get("productos[" + index + "].sku");
                producto.setSku((sku != null && !sku.trim().isEmpty()) ? sku.trim() : null);
                String codigoBarras = allParams.get("productos[" + index + "].codigoBarras");
                producto.setCodigoBarras((codigoBarras != null && !codigoBarras.trim().isEmpty()) ? codigoBarras.trim() : null);
                String marca = allParams.get("productos[" + index + "].marca");
                producto.setMarca((marca != null && !marca.trim().isEmpty()) ? marca.trim() : null);

                // Enums: Tipo, Estado
                String tipoStr = allParams.get("productos[" + index + "].tipo");
                if (tipoStr != null && !tipoStr.trim().isEmpty()) {
                    try { producto.setTipo(TipoProducto.valueOf(tipoStr)); } catch (IllegalArgumentException ignored) {}
                }
                String estadoStr = allParams.get("productos[" + index + "].estado");
                if (estadoStr != null && !estadoStr.trim().isEmpty()) {
                    try { producto.setEstado(EstadoProducto.valueOf(estadoStr)); } catch (IllegalArgumentException ignored) {}
                }

                // Unidades y presentación
                String unidadStr = allParams.get("productos[" + index + "].unidadMedida");
                if (unidadStr != null && !unidadStr.trim().isEmpty()) {
                    try { producto.setUnidadMedida(UnidadMedida.valueOf(unidadStr)); } catch (IllegalArgumentException ignored) {}
                }
                String contenidoNetoStr = allParams.get("productos[" + index + "].contenidoNeto");
                if (contenidoNetoStr != null && !contenidoNetoStr.trim().isEmpty()) {
                    try { producto.setContenidoNeto(Double.valueOf(contenidoNetoStr)); } catch (NumberFormatException ignored) {}
                }
                String contenidoUomStr = allParams.get("productos[" + index + "].contenidoUoM");
                if (contenidoUomStr != null && !contenidoUomStr.trim().isEmpty()) {
                    try { producto.setContenidoUoM(UnidadMedida.valueOf(contenidoUomStr)); } catch (IllegalArgumentException ignored) {}
                }
                
                try {
                    producto.setPrecioCompra(Double.parseDouble(allParams.get("productos[" + index + "].precioCompra")));
                    producto.setPrecioVenta(Double.parseDouble(allParams.get("productos[" + index + "].precioVenta")));
                    producto.setStock(Integer.parseInt(allParams.get("productos[" + index + "].stock")));
                } catch (NumberFormatException e) {
                    model.addAttribute("error", "Error en los valores numéricos del producto " + (index + 1));
                    model.addAttribute("proveedor", proveedor);
                    model.addAttribute("producto", new Producto());
                    return "admin/proveedores/form";
                }
                // Reabastecimiento opcional
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

                // Lotes y vencimientos
                producto.setGestionaLotes(allParams.get("productos[" + index + "].gestionaLotes") != null);
                producto.setRequiereVencimiento(allParams.get("productos[" + index + "].requiereVencimiento") != null);
                String vidaUtilStr = allParams.get("productos[" + index + "].vidaUtilDias");
                if (vidaUtilStr != null && !vidaUtilStr.trim().isEmpty()) {
                    try { producto.setVidaUtilDias(Integer.valueOf(vidaUtilStr)); } catch (NumberFormatException ignored) {}
                }
                String almacStr = allParams.get("productos[" + index + "].almacenamiento");
                if (almacStr != null && !almacStr.trim().isEmpty()) {
                    try { producto.setAlmacenamiento(Almacenamiento.valueOf(almacStr)); } catch (IllegalArgumentException ignored) {}
                }
                String regSan = allParams.get("productos[" + index + "].registroSanitario");
                producto.setRegistroSanitario((regSan != null && !regSan.trim().isEmpty()) ? regSan.trim() : null);
                
                // Procesar imagen si existe
                MultipartFile imagenArchivo = files.get("productos[" + index + "].imagenUrl");
                if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                    String rutaImagen = proveedorService.guardarImagenProducto(imagenArchivo);
                    producto.setImagenUrl(rutaImagen);
                }
                
                // Asociar al proveedor en memoria (el service puede volver a asociar al persistir)
                producto.setProveedor(proveedor);
                productos.add(producto);
            }
            index++;
        }

        // Guardar proveedor solo o con productos según corresponda
        try {
            System.out.println("=== GUARDANDO PROVEEDOR ===");
            System.out.println("Nombre: " + proveedor.getNombre());
            System.out.println("NIT: " + proveedor.getNit());
            System.out.println("Productos a guardar: " + productos.size());

            if (productos.isEmpty()) {
                proveedorService.saveProveedor(proveedor);
                redirectAttributes.addFlashAttribute("mensaje", "Proveedor creado exitosamente");
            } else {
                proveedorService.saveProveedorConMultiplesProductos(proveedor, productos);
                redirectAttributes.addFlashAttribute("mensaje", "Proveedor creado con " + productos.size() + " producto(s) exitosamente");
            }
            System.out.println("=== PROVEEDOR GUARDADO EXITOSAMENTE ===");
        } catch (IllegalArgumentException ex) {
            System.out.println("=== ERROR AL GUARDAR ===");
            System.out.println("ERROR: " + ex.getMessage());
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("producto", new Producto());
            return "admin/proveedores/form";
        } catch (DataIntegrityViolationException ex) {
            System.out.println("=== ERROR DE INTEGRIDAD DE DATOS ===");
            model.addAttribute("error", "No se pudo guardar el proveedor: verifique que el NIT no esté duplicado y los datos sean válidos.");
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("producto", new Producto());
            return "admin/proveedores/form";
        } catch (Exception ex) {
            System.out.println("=== ERROR INESPERADO ===");
            model.addAttribute("error", "Error inesperado: " + ex.getMessage());
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("producto", new Producto());
            return "admin/proveedores/form";
        }

        return "redirect:/admin/proveedores";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProveedor(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("producto", new Producto());
        return "admin/proveedores/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarProveedor(
            @PathVariable Long id,
            @Valid @ModelAttribute Proveedor proveedor,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("producto", new Producto());
            return "admin/proveedores/form";
        }

        // Verificar que el proveedor existe
        Proveedor proveedorExistente = proveedorService.getProveedorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        // Mantener la fecha de registro original
        proveedor.setId(id);
        proveedor.setFechaRegistro(proveedorExistente.getFechaRegistro());
        
        // Mantener los productos existentes
        proveedor.setProductos(proveedorExistente.getProductos());

        proveedorService.saveProveedor(proveedor);
        redirectAttributes.addFlashAttribute("mensaje", "Proveedor actualizado exitosamente");
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id) {
        proveedorService.deleteProveedor(id);
        return "redirect:/admin/proveedores";
    }

    @PostMapping("/toggle-estado/{id}")
    @ResponseBody
    public String toggleEstadoProveedor(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.getProveedorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        
        proveedor.setActivo(!proveedor.getActivo());
        proveedorService.saveProveedor(proveedor);
        
        return proveedor.getActivo() ? "Activado" : "Desactivado";
    }

    @GetMapping("/nuevoo")
    public String mostrarFormularioAgregarProducto(Model model) {
        model.addAttribute("proveedores", proveedorService.getAllProveedores());
        return "admin/proveedores/registrar-producto-a-proveedor";
    }

    @GetMapping("/agregar-productos/{id}")
    public String mostrarFormularioAgregarProductosAProveedor(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.getProveedorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        model.addAttribute("proveedor", proveedor);
        return "admin/proveedores/agregar-productos";
    }

    @PostMapping("/agregar-productos/{id}")
    public String agregarProductosAProveedor(
            @PathVariable Long id,
            @RequestParam Map<String, String> allParams,
            @RequestParam Map<String, MultipartFile> files,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Proveedor proveedor = proveedorService.getProveedorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        // Procesar múltiples productos
        List<Producto> productos = new ArrayList<>();
        int index = 0;
        
        while (allParams.containsKey("productos[" + index + "].nombre")) {
            String nombre = allParams.get("productos[" + index + "].nombre");
            
            if (nombre != null && !nombre.trim().isEmpty()) {
                Producto producto = new Producto();
                producto.setNombre(nombre);
                producto.setDescripcion(allParams.get("productos[" + index + "].descripcion"));
                producto.setProveedor(proveedor);
                
                // Identificación comercial
                String sku = allParams.get("productos[" + index + "].sku");
                producto.setSku((sku != null && !sku.trim().isEmpty()) ? sku.trim() : null);
                String codigoBarras = allParams.get("productos[" + index + "].codigoBarras");
                producto.setCodigoBarras((codigoBarras != null && !codigoBarras.trim().isEmpty()) ? codigoBarras.trim() : null);
                String marca = allParams.get("productos[" + index + "].marca");
                producto.setMarca((marca != null && !marca.trim().isEmpty()) ? marca.trim() : null);

                // Enums: Tipo, Estado
                String tipoStr = allParams.get("productos[" + index + "].tipo");
                if (tipoStr != null && !tipoStr.trim().isEmpty()) {
                    try { producto.setTipo(TipoProducto.valueOf(tipoStr)); } catch (IllegalArgumentException ignored) {}
                }
                String estadoStr = allParams.get("productos[" + index + "].estado");
                if (estadoStr != null && !estadoStr.trim().isEmpty()) {
                    try { producto.setEstado(EstadoProducto.valueOf(estadoStr)); } catch (IllegalArgumentException ignored) {}
                }

                // Unidades y presentación
                String unidadStr = allParams.get("productos[" + index + "].unidadMedida");
                if (unidadStr != null && !unidadStr.trim().isEmpty()) {
                    try { producto.setUnidadMedida(UnidadMedida.valueOf(unidadStr)); } catch (IllegalArgumentException ignored) {}
                }
                String contenidoNetoStr = allParams.get("productos[" + index + "].contenidoNeto");
                if (contenidoNetoStr != null && !contenidoNetoStr.trim().isEmpty()) {
                    try { producto.setContenidoNeto(Double.valueOf(contenidoNetoStr)); } catch (NumberFormatException ignored) {}
                }
                String contenidoUomStr = allParams.get("productos[" + index + "].contenidoUoM");
                if (contenidoUomStr != null && !contenidoUomStr.trim().isEmpty()) {
                    // Nota: el form envía texto libre, no enum
                    // Si quieres mapear a UnidadMedida enum, agregar lógica aquí
                }
                
                try {
                    producto.setPrecioCompra(Double.parseDouble(allParams.get("productos[" + index + "].precioCompra")));
                    producto.setPrecioVenta(Double.parseDouble(allParams.get("productos[" + index + "].precioVenta")));
                    producto.setStock(Integer.parseInt(allParams.get("productos[" + index + "].stock")));
                } catch (NumberFormatException e) {
                    model.addAttribute("error", "Error en los valores numéricos del producto " + (index + 1));
                    model.addAttribute("proveedor", proveedor);
                    return "admin/proveedores/agregar-productos";
                }

                // Reabastecimiento opcional
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

                // Lotes y vencimientos (checkboxes y campos relacionados)
                producto.setGestionaLotes(allParams.get("productos[" + index + "].gestionaLotes") != null);
                producto.setRequiereVencimiento(allParams.get("productos[" + index + "].requiereVencimiento") != null);
                String vidaUtilStr = allParams.get("productos[" + index + "].vidaUtilDias");
                if (vidaUtilStr != null && !vidaUtilStr.trim().isEmpty()) {
                    try { producto.setVidaUtilDias(Integer.valueOf(vidaUtilStr)); } catch (NumberFormatException ignored) {}
                }
                String almacStr = allParams.get("productos[" + index + "].almacenamiento");
                if (almacStr != null && !almacStr.trim().isEmpty()) {
                    try { producto.setAlmacenamiento(Almacenamiento.valueOf(almacStr)); } catch (IllegalArgumentException ignored) {}
                }
                String regSan = allParams.get("productos[" + index + "].registroSanitario");
                producto.setRegistroSanitario((regSan != null && !regSan.trim().isEmpty()) ? regSan.trim() : null);
                
                // Procesar imagen si existe
                MultipartFile imagenArchivo = files.get("productos[" + index + "].imagenUrl");
                if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                    String rutaImagen = proveedorService.guardarImagenProducto(imagenArchivo);
                    producto.setImagenUrl(rutaImagen);
                }
                
                productoService.saveProducto(producto);
                productos.add(producto);
            }
            index++;
        }

        if (productos.isEmpty()) {
            model.addAttribute("error", "Debe agregar al menos un producto");
            model.addAttribute("proveedor", proveedor);
            return "admin/proveedores/agregar-productos";
        }

        redirectAttributes.addFlashAttribute("mensaje", productos.size() + " producto(s) agregado(s) exitosamente al proveedor " + proveedor.getNombre());
        return "redirect:/admin/proveedores";
    }


    @PostMapping("/agregar-producto")
    public String guardarProductoDeProveedor(
            @RequestParam Long proveedorId,
            @RequestParam("productoNombre") String nombreProducto,
            @RequestParam("productoDescripcion") String descripcion,
            @RequestParam("precioCompra") double precioCompra,
            @RequestParam("precioVenta") double precioVenta,
            @RequestParam("stock") int stock,
            @RequestParam(value = "imagenUrl", required = false) MultipartFile imagenArchivo
    ) {
        try {
            Proveedor proveedor = proveedorService.getProveedorById(proveedorId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

            Producto producto = new Producto();
            producto.setNombre(nombreProducto);
            producto.setDescripcion(descripcion);
            producto.setPrecioCompra(precioCompra);
            producto.setPrecioVenta(precioVenta);
            producto.setStock(stock);
            producto.setProveedor(proveedor);

            if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                String rutaImagen = proveedorService.guardarImagenProducto(imagenArchivo);
                producto.setImagenUrl(rutaImagen);
            }

            productoService.saveProducto(producto);
            return "redirect:/admin/productos";

        } catch (Exception e) {
            return "redirect:/admin/proveedores/registrar-producto?error";
        }
    }
}
