-- Script para recalcular el campo total en la tabla venta
-- Este script actualiza venta.total calculándolo desde la suma de detalle_venta
-- Fecha: 2025-11-21
-- Base de datos: merko

USE merko;

-- ============================================================
-- PASO 1: VERIFICACIÓN PREVIA - Ver el estado actual
-- ============================================================

-- Contar cuántas ventas tienen discrepancias
SELECT 
    COUNT(*) as ventas_con_discrepancia,
    SUM(ABS(v.total - COALESCE(suma_detalles, 0))) as suma_total_diferencias
FROM venta v
LEFT JOIN (
    SELECT venta_id, SUM(cantidad * precio_unitario) as suma_detalles
    FROM detalle_venta
    GROUP BY venta_id
) dv ON v.id = dv.venta_id
WHERE ABS(v.total - COALESCE(dv.suma_detalles, 0)) > 0.01;

-- ============================================================
-- PASO 2: EJEMPLOS DE DISCREPANCIAS - Primeros 10 casos
-- ============================================================

SELECT 
    v.id,
    v.fecha,
    v.total as total_almacenado,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) as total_calculado,
    (v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) as diferencia,
    COUNT(dv.id) as num_detalles
FROM venta v
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
GROUP BY v.id, v.fecha, v.total
HAVING ABS(v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) > 0.01
ORDER BY ABS(v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) DESC
LIMIT 10;

-- ============================================================
-- PASO 3: RECALCULAR TOTALES (EJECUTAR CON CUIDADO)
-- ============================================================

-- ADVERTENCIA: Este UPDATE modificará el campo total de todas las ventas
-- Se recomienda hacer un BACKUP antes de ejecutar:
-- CREATE TABLE venta_backup_20251121 AS SELECT * FROM venta;

UPDATE venta v
SET v.total = (
    SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)
    FROM detalle_venta dv
    WHERE dv.venta_id = v.id
)
WHERE v.id IN (
    SELECT DISTINCT venta_id 
    FROM detalle_venta
);

-- ============================================================
-- PASO 4: VALIDACIÓN POST-UPDATE
-- ============================================================

-- Verificar que ya no hay discrepancias (debería devolver 0 filas)
SELECT 
    v.id,
    v.total as total_almacenado,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) as total_calculado,
    (v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) as diferencia
FROM venta v
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
GROUP BY v.id, v.total
HAVING ABS(v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) > 0.01
LIMIT 10;

-- ============================================================
-- PASO 5: RESUMEN FINAL
-- ============================================================

-- Estadísticas finales
SELECT 
    'Ventas totales' as metrica,
    COUNT(*) as valor
FROM venta
UNION ALL
SELECT 
    'Ventas con detalles',
    COUNT(DISTINCT venta_id)
FROM detalle_venta
UNION ALL
SELECT 
    'Detalles totales',
    COUNT(*)
FROM detalle_venta
UNION ALL
SELECT 
    'Total ventas (suma de totales)',
    ROUND(SUM(total), 2)
FROM venta
WHERE estado = 'ACTIVA' OR estado IS NULL
UNION ALL
SELECT 
    'Total ventas (desde detalles)',
    ROUND(SUM(dv.cantidad * dv.precio_unitario), 2)
FROM venta v
JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE v.estado = 'ACTIVA' OR v.estado IS NULL;
