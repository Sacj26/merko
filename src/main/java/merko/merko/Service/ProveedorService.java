package merko.merko.Service;

import merko.merko.Entity.Producto;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;

    public ProveedorService(ProveedorRepository proveedorRepository, ProductoRepository productoRepository) {
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
    }

    public List<Proveedor> getAllProveedores() {
        return proveedorRepository.findAll();
    }

    public List<Proveedor> getProveedoresActivos() {
        return proveedorRepository.findAll().stream()
                .filter(p -> p.getActivo() != null && p.getActivo())
                .toList();
    }

    public Optional<Proveedor> getProveedorById(Long id) {
        return proveedorRepository.findById(id);
    }

    public Optional<Proveedor> getProveedorByNit(String nit) {
        return proveedorRepository.findAll().stream()
                .filter(p -> p.getNit() != null && p.getNit().equals(nit))
                .findFirst();
    }

    @Transactional
    public Proveedor saveProveedor(Proveedor proveedor) {
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor no puede estar vacío");
        }
        
        // Validar NIT único solo si es un proveedor nuevo o si el NIT cambió
        if (proveedor.getNit() != null) {
            Optional<Proveedor> proveedorExistente = getProveedorByNit(proveedor.getNit());
            if (proveedorExistente.isPresent() && 
                !proveedorExistente.get().getId().equals(proveedor.getId())) {
                throw new IllegalArgumentException("Ya existe un proveedor con este NIT/RUC");
            }
        }
        
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public void deleteProveedor(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new IllegalArgumentException("Proveedor con id " + id + " no existe");
        }
        proveedorRepository.deleteById(id);
    }

    @Transactional
    public Proveedor saveProveedorConProducto(Proveedor proveedor, Producto producto) {
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor no puede estar vacío");
        }

        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);
        producto.setProveedor(proveedorGuardado);
        productoRepository.save(producto);
        return proveedorGuardado;
    }

    @Transactional
    public Proveedor saveProveedorConMultiplesProductos(Proveedor proveedor, List<Producto> productos) {
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor no puede estar vacío");
        }
        
        // Validar NIT único (mismo criterio que saveProveedor)
        if (proveedor.getNit() != null) {
            Optional<Proveedor> proveedorExistente = getProveedorByNit(proveedor.getNit());
            if (proveedorExistente.isPresent() &&
                !proveedorExistente.get().getId().equals(proveedor.getId())) {
                throw new IllegalArgumentException("Ya existe un proveedor con este NIT/RUC");
            }
        }

        if (productos == null || productos.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos un producto");
        }

        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);
        
        for (Producto producto : productos) {
            producto.setProveedor(proveedorGuardado);
            productoRepository.save(producto);
        }
        
        return proveedorGuardado;
    }

    public String guardarImagenProducto(MultipartFile archivo) {
        try {

            String carpetaUploads = "src/main/resources/static/uploads/imagenes-productos/";
            Path carpetaPath = Paths.get(carpetaUploads);


            if (!Files.exists(carpetaPath)) {
                Files.createDirectories(carpetaPath);
            }


            String nombreOriginal = archivo.getOriginalFilename();
            String extension = "";

            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            } else {
                extension = ".png";
            }

            String nombreArchivo = System.currentTimeMillis() + extension;


            Path archivoPath = carpetaPath.resolve(nombreArchivo);


            Files.copy(archivo.getInputStream(), archivoPath, StandardCopyOption.REPLACE_EXISTING);


            return "/uploads/imagenes-productos/" + nombreArchivo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}