-- Script para recalcular el total de todas las compras basándose en la suma de precio_compra de detalle_compra
-- Este script actualiza el campo 'total' en la tabla 'compra' con la suma correcta de sus detalles
-- Fecha: 2025-11-21
-- Base de datos: merko

USE merko;

-- Paso 1: Verificar cuántas compras tienen detalles
SELECT 
    COUNT(DISTINCT c.id) as compras_con_detalles,
    COUNT(DISTINCT CASE WHEN dc.id IS NULL THEN c.id END) as compras_sin_detalles,
    COUNT(DISTINCT c.id) as total_compras
FROM compra c
LEFT JOIN detalle_compra dc ON c.id = dc.compra_id;

-- Paso 2: Ver ejemplos de compras con diferencias (antes de actualizar)
SELECT 
    c.id as compra_id,
    c.total as total_actual,
    COALESCE(SUM(dc.precio_compra), 0) as total_calculado,
    (c.total - COALESCE(SUM(dc.precio_compra), 0)) as diferencia,
    COUNT(dc.id) as num_detalles
FROM compra c
LEFT JOIN detalle_compra dc ON c.id = dc.compra_id
GROUP BY c.id, c.total
HAVING ABS(c.total - COALESCE(SUM(dc.precio_compra), 0)) > 0.01
LIMIT 20;

-- Paso 3: ACTUALIZAR el total de todas las compras que tienen detalles
-- Esta query actualiza el campo 'total' con la suma de precio_compra de detalle_compra
UPDATE compra c
SET c.total = (
    SELECT COALESCE(SUM(dc.precio_compra), 0)
    FROM detalle_compra dc
    WHERE dc.compra_id = c.id
)
WHERE c.id IN (
    SELECT DISTINCT compra_id 
    FROM detalle_compra
);

-- Paso 4: Verificar los cambios (después de actualizar)
SELECT 
    c.id as compra_id,
    c.fecha,
    c.total as total_actualizado,
    COALESCE(SUM(dc.precio_compra), 0) as suma_detalles,
    COUNT(dc.id) as num_detalles,
    (c.total - COALESCE(SUM(dc.precio_compra), 0)) as diferencia
FROM compra c
LEFT JOIN detalle_compra dc ON c.id = dc.compra_id
GROUP BY c.id, c.fecha, c.total
HAVING ABS(c.total - COALESCE(SUM(dc.precio_compra), 0)) > 0.01
LIMIT 20;

-- Paso 5: Resumen final
SELECT 
    COUNT(*) as total_compras_actualizadas,
    MIN(total) as total_minimo,
    MAX(total) as total_maximo,
    AVG(total) as total_promedio,
    SUM(total) as suma_total_compras
FROM compra
WHERE id IN (
    SELECT DISTINCT compra_id 
    FROM detalle_compra
);

-- NOTA: Las compras sin detalles en detalle_compra mantendrán su total original
-- Si quieres poner en 0 las compras sin detalles, descomenta la siguiente línea:
-- UPDATE compra SET total = 0 WHERE id NOT IN (SELECT DISTINCT compra_id FROM detalle_compra);
