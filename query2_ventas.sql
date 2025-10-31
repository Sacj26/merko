-- ============================================
-- CONSULTA 2: Ventas recientes
-- Copia solo esta consulta y ejecútala
-- ============================================
SELECT 
    id,
    fecha,
    total,
    estado
FROM venta
ORDER BY fecha DESC
LIMIT 10;
