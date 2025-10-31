package merko.merko.dto;

import java.util.List;

public class CompraForm {

    private Long proveedorId;
    private List<DetalleCompraForm> detalles;

    
    public Long getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }

    public List<DetalleCompraForm> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompraForm> detalles) {
        this.detalles = detalles;
    }
}
