package merko.merko.dto;

import java.util.List;

public class VentaForm {

    private Long clienteId;
    private Long proveedorId;
    private Long sucursalId;
    private List<DetalleVentaForm> detalles;


    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public List<DetalleVentaForm> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaForm> detalles) {
        this.detalles = detalles;
    }
}
