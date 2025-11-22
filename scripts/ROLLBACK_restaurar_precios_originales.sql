-- =====================================================================
-- Script: ROLLBACK - Restaurar valores originales de precios de compra
-- Fecha: 2025-11-21
-- Descripción: Revierte todos los cambios del script de ajuste
--              SOLO ejecutar si te arrepientes del cambio
-- =====================================================================

-- VERIFICAR que existen las tablas de backup
SELECT 
    CASE 
        WHEN COUNT(*) = 2 THEN '✅ Tablas de backup encontradas - Puedes continuar'
        ELSE '❌ ERROR: Faltan tablas de backup - NO ejecutar este script'
    END as verificacion
FROM information_schema.tables
WHERE table_schema = 'merko' 
  AND table_name IN ('detalle_compra_backup_20251121', 'compra_backup_20251121');

-- PASO 1: Ver estado ACTUAL (después del ajuste)
SELECT 'ACTUAL - Total Compras' as descripcion, 
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM compra
UNION ALL
SELECT 'ACTUAL - Total Ventas' as descripcion,
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM venta;

-- PASO 2: RESTAURAR valores originales en detalle_compra
UPDATE detalle_compra dc
INNER JOIN detalle_compra_backup_20251121 bk ON dc.id = bk.id
SET dc.precio_unitario = bk.precio_unitario_original,
    dc.precio_compra = bk.precio_compra_original;

SELECT CONCAT('✅ Restaurados ', ROW_COUNT(), ' registros en detalle_compra') as resultado;

-- PASO 3: RESTAURAR valores originales en compra
UPDATE compra c
INNER JOIN compra_backup_20251121 bk ON c.id = bk.id
SET c.total = bk.total_original,
    c.precio_unidad = bk.precio_unidad_original;

SELECT CONCAT('✅ Restaurados ', ROW_COUNT(), ' registros en compra') as resultado;

-- PASO 4: Verificar restauración - debe coincidir con ANTES
SELECT 'RESTAURADO - Total Compras' as descripcion,
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM compra
UNION ALL
SELECT 'RESTAURADO - Total Ventas' as descripcion,
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM venta
UNION ALL
SELECT 'RESTAURADO - Ratio (Compras/Ventas)' as descripcion,
       CONCAT(FORMAT((SELECT SUM(total) FROM compra) / (SELECT SUM(total) FROM venta), 2), ':1') as valor;

-- PASO 5: Verificar integridad
SELECT COUNT(*) as discrepancias_compras
FROM compra c
WHERE c.id IN (SELECT DISTINCT compra_id FROM detalle_compra)
  AND ABS(c.total - (
      SELECT COALESCE(SUM(dc.cantidad * dc.precio_unitario), 0)
      FROM detalle_compra dc
      WHERE dc.compra_id = c.id
  )) > 0.01;

-- PASO 6 (OPCIONAL): Eliminar tablas de backup si todo está OK
-- Descomenta estas líneas SOLO si confirmaste que la restauración funcionó:
-- DROP TABLE IF EXISTS detalle_compra_backup_20251121;
-- DROP TABLE IF EXISTS compra_backup_20251121;
-- SELECT '🗑️ Tablas de backup eliminadas' as mensaje;

-- =====================================================================
-- RESULTADO ESPERADO:
-- - Total Compras: $365,349,137,606 (valor ORIGINAL restaurado)
-- - Total Ventas: $36,522,830,586 (sin cambios - nunca se tocó)
-- - Ratio: 10:1 (vuelve al estado inicial)
-- - Discrepancias: 0
-- 
-- IMPORTANTE: Las tablas de backup permanecen hasta que las elimines
--             manualmente (paso 6) para mayor seguridad
-- =====================================================================

SELECT '✅ ROLLBACK COMPLETADO - Valores originales restaurados' as mensaje;
