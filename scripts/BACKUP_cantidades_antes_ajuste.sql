-- =====================================================================
-- Script: BACKUP de cantidades antes del ajuste
-- Fecha: 2025-11-21
-- Descripción: Guarda las cantidades originales de compras
-- =====================================================================

-- Crear tabla de backup para cantidades
DROP TABLE IF EXISTS detalle_compra_cantidades_backup_20251121;

CREATE TABLE detalle_compra_cantidades_backup_20251121 AS
SELECT 
    id,
    compra_id,
    cantidad as cantidad_original,
    precio_unitario,
    (cantidad * precio_unitario) as subtotal_original
FROM detalle_compra;

SELECT CONCAT('✅ Backup cantidades creado: ', COUNT(*), ' registros') as resultado
FROM detalle_compra_cantidades_backup_20251121;

-- Backup de totales de compra
DROP TABLE IF EXISTS compra_totales_backup_20251121;

CREATE TABLE compra_totales_backup_20251121 AS
SELECT 
    id,
    cantidad as cantidad_original,
    total as total_original
FROM compra;

SELECT CONCAT('✅ Backup totales compra creado: ', COUNT(*), ' registros') as resultado
FROM compra_totales_backup_20251121;

SELECT '🎯 BACKUP COMPLETADO - Puedes ejecutar el ajuste de cantidades' as mensaje;
