-- ============================================
-- CONSULTA 5: Resumen total
-- Copia solo esta consulta y ejecútala
-- ============================================
SELECT 
    (SELECT COUNT(*) FROM producto WHERE estado = 'ACTIVO') as productos_activos,
    (SELECT COUNT(*) FROM venta WHERE estado = 'ACTIVA') as total_ventas,
    (SELECT COUNT(*) FROM lote WHERE estado = 'ACTIVO') as lotes_activos,
    (SELECT COUNT(*) FROM proveedor) as total_proveedores;
