-- =====================================================================
-- Script: Ajustar CANTIDADES de compra para valores realistas
-- Fecha: 2025-11-21
-- Descripción: El problema NO son los precios, sino las CANTIDADES
--              Compras: 118.66 unidades promedio
--              Ventas: 10.49 unidades promedio
--              Esto reduce cantidades de compra dividiendo por 10
-- =====================================================================

-- PASO 1: Verificar estado actual
SELECT 
    'ANTES - Compras' as tipo,
    COUNT(*) as num_detalles,
    FORMAT(SUM(cantidad), 0) as cantidad_total,
    FORMAT(AVG(cantidad), 2) as cantidad_promedio,
    CONCAT('$', FORMAT(SUM(cantidad * precio_unitario), 2)) as valor_total
FROM detalle_compra
UNION ALL
SELECT 
    'ANTES - Ventas' as tipo,
    COUNT(*) as num_detalles,
    FORMAT(SUM(cantidad), 0) as cantidad_total,
    FORMAT(AVG(cantidad), 2) as cantidad_promedio,
    CONCAT('$', FORMAT(SUM(cantidad * precio_unitario), 2)) as valor_total
FROM detalle_venta;

-- PASO 2: Reducir cantidades en detalle_compra dividiendo por 10
-- Garantizamos mínimo 1 unidad
UPDATE detalle_compra
SET cantidad = GREATEST(1, FLOOR(cantidad / 10.0));

SELECT CONCAT('✅ Cantidades ajustadas en ', ROW_COUNT(), ' detalles de compra') as resultado;

-- PASO 3: Recalcular totales de compras basado en nuevas cantidades
UPDATE compra c
SET c.total = (
    SELECT COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0)
    FROM detalle_compra dc
    WHERE dc.compra_id = c.id
),
c.cantidad = (
    SELECT COALESCE(SUM(dc.cantidad), 0)
    FROM detalle_compra dc
    WHERE dc.compra_id = c.id
)
WHERE c.id IN (SELECT DISTINCT compra_id FROM detalle_compra);

SELECT CONCAT('✅ Totales recalculados en ', ROW_COUNT(), ' compras') as resultado;

-- PASO 4: Verificar nuevas cantidades
SELECT 
    'DESPUÉS - Compras' as tipo,
    COUNT(*) as num_detalles,
    FORMAT(SUM(cantidad), 0) as cantidad_total,
    FORMAT(AVG(cantidad), 2) as cantidad_promedio,
    CONCAT('$', FORMAT(SUM(cantidad * precio_unitario), 2)) as valor_total
FROM detalle_compra
UNION ALL
SELECT 
    'DESPUÉS - Ventas' as tipo,
    COUNT(*) as num_detalles,
    FORMAT(SUM(cantidad), 0) as cantidad_total,
    FORMAT(AVG(cantidad), 2) as cantidad_promedio,
    CONCAT('$', FORMAT(SUM(cantidad * precio_unitario), 2)) as valor_total
FROM detalle_venta;

-- PASO 5: Ver totales finales
SELECT 'DESPUÉS - Total Compras' as descripcion,
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM compra
UNION ALL
SELECT 'DESPUÉS - Total Ventas' as descripcion,
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM venta
UNION ALL
SELECT 'DESPUÉS - Ratio (Compras/Ventas)' as descripcion,
       CONCAT(FORMAT((SELECT SUM(total) FROM compra) / (SELECT SUM(total) FROM venta), 2), ':1') as valor
UNION ALL
SELECT 'DESPUÉS - Ganancia Bruta' as descripcion,
       CONCAT('$', FORMAT((SELECT SUM(total) FROM venta) - (SELECT SUM(total) FROM compra), 2)) as valor;

-- PASO 6: Verificar integridad
SELECT COUNT(*) as discrepancias_compras
FROM compra c
WHERE c.id IN (SELECT DISTINCT compra_id FROM detalle_compra)
  AND ABS(c.total - (
      SELECT COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0)
      FROM detalle_compra dc
      WHERE dc.compra_id = c.id
  )) > 0.01;

-- =====================================================================
-- RESULTADO ESPERADO:
-- - Cantidad promedio compras: ~11.87 (era 118.66) ✅ Similar a ventas
-- - Total Compras: ~$36.5B (era $365B)
-- - Total Ventas: $36.5B (sin cambios)
-- - Ratio: ~1:1 ✅ PERFECTO
-- - Precios unitarios: SIN CAMBIOS ✅
-- 
-- LÓGICA: Ahora compras cantidades similares a las que vendes
--         Los precios permanecen realistas
--         El inventario es manejable
-- =====================================================================

SELECT '✅ AJUSTE COMPLETADO - Cantidades normalizadas, precios intactos' as mensaje;
