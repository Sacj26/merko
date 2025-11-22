package merko.merko.Entity;

public enum EstadoVenta {
    ACTIVA("Activa"),
    ANULADA("Anulada");
    
    private final String displayName;
    
    EstadoVenta(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
