package merko.merko.dto;

import java.util.List;

public class CompraForm {

    private Long proveedorId;
    private Long sucursalId;
    private List<DetalleCompraForm> detalles;

    
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

    public List<DetalleCompraForm> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompraForm> detalles) {
        this.detalles = detalles;
    }
}
