package merko.merko.dto;

import java.util.List;

public class VentaForm {

    private Long clienteId;
    private List<DetalleVentaForm> detalles;


    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<DetalleVentaForm> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaForm> detalles) {
        this.detalles = detalles;
    }
}
