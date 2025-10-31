-- ============================================
-- CONSULTA 1: Estado de Productos
-- Copia solo esta consulta y ejecútala
-- ============================================
SELECT 
    estado,
    COUNT(*) as total_productos
FROM producto
GROUP BY estado;
