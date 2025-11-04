package merko.merko.dto;

public class CompraItemDto {
    public Long productBranchId; // preferido
    public Long productoId; // alternativa si no se tiene productBranchId
    public Long branchId; // necesaria si se usa productoId
    public Integer cantidad;
}
