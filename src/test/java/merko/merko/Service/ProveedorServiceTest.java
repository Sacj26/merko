package merko.merko.Service;

import merko.merko.Entity.Branch;
import merko.merko.Entity.ContactPerson;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.ProductoRepository;
import merko.merko.Repository.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ProveedorServiceTest {

    @Test
    public void buildBranchesFromParams_parsesBranchesAndContacts() {
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        ProveedorRepository proveedorRepository = Mockito.mock(ProveedorRepository.class);
        ProveedorService service = new ProveedorService(proveedorRepository, productoRepository);

        Map<String, String> params = new HashMap<>();
        params.put("branches[0].nombre", "Sucursal Central");
        params.put("branches[0].direccion", "Av Principal 1");
        params.put("branches[0].telefono", "+59171234567");
        params.put("branches[0].ciudad", "La Paz");
        params.put("branches[0].pais", "Bolivia");
        params.put("branches[0].activo", "on");

        params.put("branches[0].contacts[0].nombre", "Contacto Uno");
        params.put("branches[0].contacts[0].telefono", "+59170001111");
        params.put("branches[0].contacts[0].email", "contacto@ejemplo.com");
        params.put("branches[0].contacts[0].isPrimary", "true");

        Proveedor p = new Proveedor();
        List<Branch> branches = service.buildBranchesFromParams(params, p);

        assertNotNull(branches);
        assertEquals(1, branches.size());

        Branch b = branches.get(0);
        assertEquals("Sucursal Central", b.getNombre());
        assertEquals("Av Principal 1", b.getDireccion());
        assertEquals("La Paz", b.getCiudad());
        assertEquals("Bolivia", b.getPais());
        assertTrue(Boolean.TRUE.equals(b.getActivo()));

        List<ContactPerson> contacts = b.getContacts();
        assertNotNull(contacts);
        assertEquals(1, contacts.size());

        ContactPerson c = contacts.get(0);
        assertEquals("Contacto Uno", c.getNombre());
        assertEquals("contacto@ejemplo.com", c.getEmail());
        assertTrue(Boolean.TRUE.equals(c.getIsPrimary()));
    }
}
