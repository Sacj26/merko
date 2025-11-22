-- =====================================================================
-- Script: Ajustar precios de compra para valores realistas
-- Fecha: 2025-11-21
-- Descripción: Reduce precio_unitario en detalle_compra dividiendo por 10
--              para lograr ratio compras:ventas realista (~1:1 o 1.2:1)
--              Los productos se "compraron" muy caros, esto los normaliza
-- =====================================================================

-- PASO 1: Verificar estado actual
SELECT 'ANTES - Total Compras' as descripcion, 
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM compra
UNION ALL
SELECT 'ANTES - Total Ventas' as descripcion,
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM venta
UNION ALL
SELECT 'ANTES - Ratio (Compras/Ventas)' as descripcion,
       CONCAT(FORMAT((SELECT SUM(total) FROM compra) / (SELECT SUM(total) FROM venta), 2), ':1') as valor;

-- PASO 2: Ver distribución de precios antes del cambio
SELECT 
    'Precios actuales' as tipo,
    CONCAT('$', FORMAT(MIN(precio_unitario), 2)) as minimo,
    CONCAT('$', FORMAT(AVG(precio_unitario), 2)) as promedio,
    CONCAT('$', FORMAT(MAX(precio_unitario), 2)) as maximo,
    COUNT(*) as total_detalles
FROM detalle_compra;

-- PASO 3: Reducir precio_unitario y precio_compra dividiendo por 10
-- Esto hace que los productos tengan un costo de adquisición más realista
UPDATE detalle_compra
SET precio_unitario = precio_unitario / 10.0,
    precio_compra = precio_compra / 10.0;

-- PASO 4: Recalcular total de todas las compras basado en nuevos precios
UPDATE compra c
SET c.total = (
    SELECT COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0)
    FROM detalle_compra dc
    WHERE dc.compra_id = c.id
),
c.precio_unidad = c.precio_unidad / 10.0
WHERE c.id IN (SELECT DISTINCT compra_id FROM detalle_compra);

-- PASO 5: Verificar nuevos totales
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
       CONCAT('$', FORMAT((SELECT SUM(total) FROM venta) - (SELECT SUM(total) FROM compra), 2)) as valor
UNION ALL
SELECT 'DESPUÉS - Margen %' as descripcion,
       CONCAT(FORMAT(((SELECT SUM(total) FROM venta) - (SELECT SUM(total) FROM compra)) / (SELECT SUM(total) FROM venta) * 100, 2), '%') as valor;

-- PASO 6: Ver nueva distribución de precios
SELECT 
    'Precios nuevos' as tipo,
    CONCAT('$', FORMAT(MIN(precio_unitario), 2)) as minimo,
    CONCAT('$', FORMAT(AVG(precio_unitario), 2)) as promedio,
    CONCAT('$', FORMAT(MAX(precio_unitario), 2)) as maximo,
    COUNT(*) as total_detalles
FROM detalle_compra;

-- PASO 7: Verificar integridad COMPRAS - debe retornar 0 discrepancias
SELECT COUNT(*) as compras_con_discrepancia
FROM compra c
WHERE c.id IN (SELECT DISTINCT compra_id FROM detalle_compra)
  AND ABS(c.total - (
      SELECT COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0)
      FROM detalle_compra dc
      WHERE dc.compra_id = c.id
  )) > 0.01;

-- PASO 8: Verificar integridad VENTAS - debe seguir en 0
SELECT COUNT(*) as ventas_con_discrepancia
FROM venta v
WHERE v.id IN (SELECT DISTINCT venta_id FROM detalle_venta)
  AND ABS(v.total - (
      SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)
      FROM detalle_venta dv
      WHERE dv.venta_id = v.id
  )) > 0.01;

-- =====================================================================
-- RESULTADO ESPERADO:
-- - Total Compras: $36,534,913,761 (~$365B / 10)
-- - Total Ventas: $36,522,830,586 (sin cambios)
-- - Ratio: ~1:1 ✅ PERFECTO - ventas ≈ compras
-- - Ganancia: Cerca de break-even (pequeña pérdida ~$12M)
-- - Margen: ~0% (típico de datos sintéticos sin markup)
-- 
-- NOTA: Esto representa que compraste productos a precio justo
--       y los vendiste al mismo precio (sin ganancia ni pérdida)
--       Es mucho más realista que tener $365B en compras
-- =====================================================================

-- PASO 9 (OPCIONAL): Si quieres añadir un pequeño markup después
-- Descomenta estas líneas para simular 20% de ganancia:
-- UPDATE detalle_venta SET precio_unitario = precio_unitario * 1.2;
-- UPDATE venta v SET v.total = (
--     SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)
--     FROM detalle_venta dv WHERE dv.venta_id = v.id
-- );
