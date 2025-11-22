-- =====================================================================
-- Script: ROLLBACK - Restaurar cantidades originales
-- Fecha: 2025-11-21
-- Descripción: Revierte el ajuste de cantidades
-- =====================================================================

-- PASO 1: Restaurar cantidades en detalle_compra
UPDATE detalle_compra dc
INNER JOIN detalle_compra_cantidades_backup_20251121 bk ON dc.id = bk.id
SET dc.cantidad = bk.cantidad_original;

SELECT CONCAT('✅ Restauradas ', ROW_COUNT(), ' cantidades en detalle_compra') as resultado;

-- PASO 2: Restaurar totales en compra
UPDATE compra c
INNER JOIN compra_totales_backup_20251121 bk ON c.id = bk.id
SET c.cantidad = bk.cantidad_original,
    c.total = bk.total_original;

SELECT CONCAT('✅ Restaurados ', ROW_COUNT(), ' totales en compra') as resultado;

-- PASO 3: Verificar restauración
SELECT 'RESTAURADO - Total Compras' as descripcion,
       CONCAT('$', FORMAT(SUM(total), 2)) as valor
FROM compra
UNION ALL
SELECT 'RESTAURADO - Cantidad promedio',
       FORMAT(AVG(dc.cantidad), 2)
FROM detalle_compra dc;

SELECT '✅ ROLLBACK COMPLETADO - Cantidades originales restauradas' as mensaje;
