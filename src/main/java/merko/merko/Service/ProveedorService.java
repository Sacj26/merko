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
import merko.merko.Entity.Branch;
import merko.merko.Entity.ContactPerson;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
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

    /**
     * Construye una lista de Branch (y sus ContactPerson) a partir de los parámetros
     * del formulario. Esta lógica fue extraída del controlador para facilitar pruebas
     * y futuras refactorizaciones.
     */
    public List<Branch> buildBranchesFromParams(Map<String, String> allParams, Proveedor proveedor) {
        List<Branch> branches = new ArrayList<>();
        int bIndex = 0;
        while (allParams.containsKey("branches[" + bIndex + "].nombre")) {
            Branch branch = new Branch();
            branch.setNombre(allParams.get("branches[" + bIndex + "].nombre"));
            branch.setDireccion(allParams.get("branches[" + bIndex + "].direccion"));
            branch.setTelefono(allParams.get("branches[" + bIndex + "].telefono"));
            branch.setCiudad(allParams.get("branches[" + bIndex + "].ciudad"));
            branch.setPais(allParams.get("branches[" + bIndex + "].pais"));
            branch.setActivo(allParams.get("branches[" + bIndex + "].activo") != null);

            List<ContactPerson> contacts = new ArrayList<>();
            int cIndex = 0;
            while (allParams.containsKey("branches[" + bIndex + "].contacts[" + cIndex + "].nombre")) {
                ContactPerson contact = new ContactPerson();
                contact.setNombre(allParams.get("branches[" + bIndex + "].contacts[" + cIndex + "].nombre"));
                contact.setRol(allParams.get("branches[" + bIndex + "].contacts[" + cIndex + "].rol"));
                contact.setTelefono(allParams.get("branches[" + bIndex + "].contacts[" + cIndex + "].telefono"));
                contact.setEmail(allParams.get("branches[" + bIndex + "].contacts[" + cIndex + "].email"));
                contact.setNotas(allParams.get("branches[" + bIndex + "].contacts[" + cIndex + "].notas"));
                String isPrimaryStr = allParams.get("branches[" + bIndex + "].contacts[" + cIndex + "].isPrimary");
                contact.setIsPrimary(isPrimaryStr != null && (isPrimaryStr.equalsIgnoreCase("true") || isPrimaryStr.equalsIgnoreCase("on")));
                contact.setBranch(branch);
                contacts.add(contact);
                cIndex++;
            }

            boolean foundPrimary = false;
            for (ContactPerson cp : contacts) {
                if (cp.getIsPrimary() != null && cp.getIsPrimary()) {
                    if (!foundPrimary) foundPrimary = true;
                    else cp.setIsPrimary(false);
                }
            }

            branch.setContacts(contacts);
            branch.setProveedor(proveedor);
            branches.add(branch);
            bIndex++;
        }
        return branches;
    }
}