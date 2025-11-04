package merko.merko.ControllerWeb;

import com.fasterxml.jackson.databind.ObjectMapper;
import merko.merko.Entity.Branch;
import merko.merko.Entity.ContactPerson;
import merko.merko.Entity.Proveedor;
import merko.merko.Repository.BranchRepository;
import merko.merko.Repository.ContactPersonRepository;
import merko.merko.Repository.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ProveedorBranchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ContactPersonRepository contactPersonRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    public void debugEndpoint_returnsBranchesWithContacts_fieldsPresent() throws Exception {
        // Crear proveedor
        Proveedor p = new Proveedor();
        p.setNombre("Proveedor Test");
        p.setNit("12345");
        p.setTelefono("+59170000000");
        p.setDireccion("Calle Falsa 123");
        p.setCiudad("La Paz");
        p.setPais("Bolivia");
        proveedorRepository.save(p);

        // Crear branch
        Branch b = new Branch();
        b.setNombre("Sucursal Central");
        b.setDireccion("Av. Principal 1");
        b.setTelefono("+59171234567");
        b.setCiudad("La Paz");
        b.setPais("Bolivia");
        b.setProveedor(p);
        branchRepository.save(b);

        // Crear contact
        ContactPerson c = new ContactPerson();
        c.setNombre("Contacto Uno");
        c.setTelefono("+59170001111");
        c.setEmail("contacto@ejemplo.com");
        c.setBranch(b);
        contactPersonRepository.save(c);

        // Llamar endpoint debug
        mockMvc.perform(get("/admin/proveedores/debug/" + p.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p.getId()))
                .andExpect(jsonPath("$.branches").isArray())
                .andExpect(jsonPath("$.branches[0].nombre").value("Sucursal Central"))
                .andExpect(jsonPath("$.branches[0].direccion").value("Av. Principal 1"))
                .andExpect(jsonPath("$.branches[0].telefono").value("+59171234567"))
                .andExpect(jsonPath("$.branches[0].ciudad").value("La Paz"))
                .andExpect(jsonPath("$.branches[0].pais").value("Bolivia"))
                .andExpect(jsonPath("$.branches[0].contacts").isArray())
                .andExpect(jsonPath("$.branches[0].contacts[0].nombre").value("Contacto Uno"));
    }
}
