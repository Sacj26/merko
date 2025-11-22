-- =====================================================================
-- Script: Verificar discrepancias entre compra.total y suma de detalles
-- Fecha: 2025-11-21
-- =====================================================================

-- PASO 1: Ver cuántas compras tienen discrepancias
SELECT 
    COUNT(*) as compras_con_discrepancia,
    CONCAT('$', FORMAT(SUM(ABS(c.total - subtotal_calculado)), 2)) as diferencia_total
FROM (
    SELECT 
        c.id,
        c.total,
        COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0) as subtotal_calculado
    FROM compra c
    LEFT JOIN detalle_compra dc ON c.id = dc.compra_id
    GROUP BY c.id, c.total
) sub
JOIN compra c ON sub.id = c.id
WHERE ABS(c.total - sub.subtotal_calculado) > 0.01;

-- PASO 2: Ver ejemplos de discrepancias (primeras 10)
SELECT 
    c.id,
    c.fecha,
    CONCAT('$', FORMAT(c.total, 2)) as total_almacenado,
    CONCAT('$', FORMAT(COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0), 2)) as suma_detalles,
    CONCAT('$', FORMAT(ABS(c.total - COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0)), 2)) as diferencia
FROM compra c
LEFT JOIN detalle_compra dc ON c.id = dc.compra_id
GROUP BY c.id, c.fecha, c.total
HAVING ABS(c.total - COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0)) > 0.01
ORDER BY diferencia DESC
LIMIT 10;

-- PASO 3: Ver totales actuales
SELECT 
    'Total según tabla compra' as descripcion,
    CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM compra
UNION ALL
SELECT 
    'Total calculado desde detalles',
    CONCAT('$', FORMAT(SUM(dc.cantidad * dc.precio_unitario), 2))
FROM detalle_compra dc;
