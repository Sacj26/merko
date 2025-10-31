-- ============================================
-- CONSULTA 4: Lotes próximos a vencer
-- Copia solo esta consulta y ejecútala
-- ============================================
SELECT 
    l.codigo_lote,
    p.nombre as producto,
    l.fecha_vencimiento,
    l.cantidad_disponible,
    l.estado
FROM lote l
LEFT JOIN producto p ON l.producto_id = p.id
WHERE l.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
  AND l.estado = 'ACTIVO'
  AND l.cantidad_disponible > 0
ORDER BY l.fecha_vencimiento ASC
LIMIT 10;
