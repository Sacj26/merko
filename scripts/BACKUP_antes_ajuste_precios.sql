-- =====================================================================
-- Script: BACKUP antes de ajustar precios de compra
-- Fecha: 2025-11-21
-- Descripción: Crea tablas de respaldo con los valores originales
--              EJECUTAR ESTE SCRIPT PRIMERO antes del ajuste
-- =====================================================================

-- PASO 1: Crear tabla de backup para detalle_compra
DROP TABLE IF EXISTS detalle_compra_backup_20251121;

CREATE TABLE detalle_compra_backup_20251121 AS
SELECT 
    id,
    compra_id,
    producto_id,
    cantidad,
    precio_unitario as precio_unitario_original,
    precio_compra as precio_compra_original
FROM detalle_compra;

SELECT CONCAT('✅ Backup detalle_compra creado: ', COUNT(*), ' registros') as resultado
FROM detalle_compra_backup_20251121;

-- PASO 2: Crear tabla de backup para compra
DROP TABLE IF EXISTS compra_backup_20251121;

CREATE TABLE compra_backup_20251121 AS
SELECT 
    id,
    total as total_original,
    precio_unidad as precio_unidad_original,
    cantidad
FROM compra;

SELECT CONCAT('✅ Backup compra creado: ', COUNT(*), ' registros') as resultado
FROM compra_backup_20251121;

-- PASO 3: Verificar que los backups se crearon correctamente
SELECT 
    'detalle_compra_backup_20251121' as tabla,
    COUNT(*) as registros,
    CONCAT('$', FORMAT(SUM(precio_unitario_original), 2)) as suma_precios_unitarios,
    CONCAT('$', FORMAT(SUM(precio_compra_original), 2)) as suma_precios_compra
FROM detalle_compra_backup_20251121
UNION ALL
SELECT 
    'compra_backup_20251121' as tabla,
    COUNT(*) as registros,
    CONCAT('$', FORMAT(SUM(total_original), 2)) as suma_totales,
    CONCAT('$', FORMAT(SUM(precio_unidad_original), 2)) as suma_precios_unidad
FROM compra_backup_20251121;

-- =====================================================================
-- RESULTADO ESPERADO:
-- - 2 tablas de backup creadas
-- - detalle_compra_backup_20251121: 132,048 registros
-- - compra_backup_20251121: ~60,000 registros
-- 
-- IMPORTANTE: Estas tablas contienen TODOS los valores originales
--             Si algo sale mal, puedes restaurarlos con el script de ROLLBACK
-- =====================================================================

SELECT '🎯 BACKUP COMPLETADO - Ahora puedes ejecutar el script de ajuste' as mensaje;
