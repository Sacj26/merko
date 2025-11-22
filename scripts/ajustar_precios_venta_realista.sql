-- =====================================================================
-- Script: Ajustar precios de venta para simular markup realista
-- Fecha: 2025-11-21
-- Descripción: Aumenta precio_unitario en detalle_venta por factor 3.0
--              para lograr ratio compras:ventas más realista (~1.2:1)
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

-- PASO 2: Backup de precios originales (opcional - comentar si no necesitas)
-- CREATE TABLE IF NOT EXISTS detalle_venta_backup_precios AS
-- SELECT id, precio_unitario as precio_unitario_original 
-- FROM detalle_venta;

-- PASO 3: Aumentar precio_unitario por factor 3.0 (200% markup)
-- Esto simula que vendes al triple del costo de compra
UPDATE detalle_venta
SET precio_unitario = precio_unitario * 3.0;

-- PASO 4: Recalcular total de todas las ventas basado en nuevos precios
UPDATE venta v
SET v.total = (
    SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)
    FROM detalle_venta dv
    WHERE dv.venta_id = v.id
)
WHERE v.id IN (SELECT DISTINCT venta_id FROM detalle_venta);

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
       CONCAT('$', FORMAT((SELECT SUM(total) FROM venta) - (SELECT SUM(total) FROM compra), 2)) as valor;

-- PASO 6: Verificar integridad - debe retornar 0 discrepancias
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
-- - Total Compras: $365,349,137,606 (sin cambios)
-- - Total Ventas: $109,568,491,758 (~$36.5B * 3.0)
-- - Ratio: ~3.33:1 (más realista que 10:1)
-- - Ganancia Bruta: ~$255B negativo aún (necesitarías factor ~10 para break-even)
-- 
-- NOTA: Con factor 3.0 sigues con pérdidas en papel, pero el ratio es
--       más consistente con negocios reales que tienen markup 200-300%
-- =====================================================================
