package merko.merko.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import merko.merko.Entity.Branch;
import merko.merko.Entity.ContactPerson;
import merko.merko.Entity.Producto;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.ProductoProveedorRepository;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProveedorRepository;

/**
 * Servicio simplificado para Proveedores - BD: proveedor (id, nombre, contacto, email)
 */
@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final ProductoProveedorRepository productoProveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository, ProductoRepository productoRepository, ProductoProveedorRepository productoProveedorRepository) {
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.productoProveedorRepository = productoProveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> getAllProveedores() {
        List<Proveedor> proveedores = proveedorRepository.findAllWithBranches();
        // Forzar carga de contacts dentro de la transacción para evitar LazyInitializationException
        proveedores.forEach(p -> p.getBranches().forEach(b -> b.getContacts().size()));
        return proveedores;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> getProveedoresActivos() {
        // BD simplificada: no hay campo 'activo', retornar todos
        List<Proveedor> proveedores = proveedorRepository.findAllWithBranches();
        // Forzar carga de contacts dentro de la transacción
        proveedores.forEach(p -> p.getBranches().forEach(b -> b.getContacts().size()));
        return proveedores;
    }

    @Transactional(readOnly = true)
    public Optional<Proveedor> getProveedorById(Long id) {
        Optional<Proveedor> proveedor = proveedorRepository.findByIdWithBranches(id);
        // Forzar carga de contacts dentro de la transacción
        proveedor.ifPresent(p -> p.getBranches().forEach(b -> b.getContacts().size()));
        return proveedor;
    }

    public Optional<Proveedor> getProveedorByNombre(String nombre) {
        // BD simplificada: buscar por nombre en lugar de NIT
        return proveedorRepository.findAll().stream()
                .filter(p -> p.getNombre() != null && p.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    @Transactional
    public Proveedor saveProveedor(Proveedor proveedor) {
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor no puede estar vacío");
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
        Producto productoGuardado = productoRepository.save(producto);
        
        // Crear relación proveedor-producto
        merko.merko.Entity.ProductoProveedor pp = new merko.merko.Entity.ProductoProveedor();
        pp.setProducto(productoGuardado);
        pp.setProveedor(proveedorGuardado);
        productoProveedorRepository.save(pp);
        
        return proveedorGuardado;
    }

    @Transactional
    public Proveedor saveProveedorConMultiplesProductos(Proveedor proveedor, List<Producto> productos) {
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor no puede estar vacío");
        }

        if (productos == null || productos.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos un producto");
        }

        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);

        for (Producto producto : productos) {
            Producto productoGuardado = productoRepository.save(producto);
            merko.merko.Entity.ProductoProveedor pp = new merko.merko.Entity.ProductoProveedor();
            pp.setProducto(productoGuardado);
            pp.setProveedor(proveedorGuardado);
            productoProveedorRepository.save(pp);
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
            String extension = ".png";

            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            }

            String nombreArchivo = System.currentTimeMillis() + extension;
            Path archivoPath = carpetaPath.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), archivoPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/imagenes-productos/" + nombreArchivo;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construye una lista de Branch a partir de los parámetros del formulario.
     * BD simplificada: Branch solo tiene (id, nombre, direccion)
     * ContactPerson tiene FK a proveedor_id, no a branch_id
     */
    public List<Branch> buildBranchesFromParams(Map<String, String> allParams, Proveedor proveedor) {
        List<Branch> branches = new ArrayList<>();
        int bIndex = 0;
        
        while (allParams.containsKey("branches[" + bIndex + "].nombre")) {
            Branch branch = new Branch();
            branch.setNombre(allParams.get("branches[" + bIndex + "].nombre"));
            branch.setDireccion(allParams.get("branches[" + bIndex + "].direccion"));
            // BD simplificada: Branch no tiene telefono, ciudad, pais, activo, contacts, proveedor
            
            branches.add(branch);
            bIndex++;
        }
        
        return branches;
    }

    /**
     * Construye una lista de ContactPerson a partir de los parámetros del formulario.
     * BD simplificada: ContactPerson tiene (id, nombre, telefono, email, proveedor_id)
     */
    public List<ContactPerson> buildContactsFromParams(Map<String, String> allParams, Proveedor proveedor) {
        List<ContactPerson> contacts = new ArrayList<>();
        int cIndex = 0;
        
        while (allParams.containsKey("contacts[" + cIndex + "].nombre")) {
            ContactPerson contact = new ContactPerson();
            contact.setNombre(allParams.get("contacts[" + cIndex + "].nombre"));
            contact.setTelefono(allParams.get("contacts[" + cIndex + "].telefono"));
            contact.setEmail(allParams.get("contacts[" + cIndex + "].email"));
            // ContactPerson usa branch_id, no proveedor_id
            
            contacts.add(contact);
            cIndex++;
        }
        
        return contacts;
    }
}