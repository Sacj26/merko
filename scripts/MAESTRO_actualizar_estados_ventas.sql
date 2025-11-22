-- =====================================================================
-- Script MAESTRO: Actualizar todos los estados de ventas
-- Fecha: 2025-11-21
-- Descripción: Ejecuta en orden:
--   1. Actualiza ventas NULL a ACTIVA
--   2. Actualiza ventas con devoluciones a ANULADA
-- =====================================================================

USE merko;

-- ============================================================
-- PASO 1: Ver estado actual de las ventas
-- ============================================================
SELECT 'ESTADO ACTUAL' as paso;

SELECT 
    COALESCE(estado, 'NULL') as estado,
    COUNT(*) as cantidad
FROM venta
GROUP BY estado
ORDER BY estado;

-- ============================================================
-- PASO 2: Actualizar ventas sin estado a ACTIVA
-- ============================================================
SELECT 'ACTUALIZANDO VENTAS NULL A ACTIVA' as paso;

UPDATE venta 
SET estado = 'ACTIVA' 
WHERE estado IS NULL;

SELECT CONCAT('✅ Actualizadas ', ROW_COUNT(), ' ventas a ACTIVA') as resultado;

-- ============================================================
-- PASO 3: Actualizar ventas con devoluciones a ANULADA
-- ============================================================
SELECT 'ACTUALIZANDO VENTAS CON DEVOLUCIONES A ANULADA' as paso;

-- Primero mostrar cuáles serán afectadas
SELECT 
    v.id,
    v.fecha,
    v.estado as estado_actual,
    COUNT(d.id) as num_devoluciones
FROM venta v
INNER JOIN devolucion d ON v.id = d.venta_id
WHERE v.estado != 'ANULADA'
GROUP BY v.id, v.fecha, v.estado;

-- Actualizar
UPDATE venta v
INNER JOIN devolucion d ON v.id = d.venta_id
SET v.estado = 'ANULADA'
WHERE v.estado != 'ANULADA';

SELECT CONCAT('✅ Actualizadas ', ROW_COUNT(), ' ventas con devoluciones a ANULADA') as resultado;

-- ============================================================
-- PASO 4: Verificar resultado final
-- ============================================================
SELECT 'ESTADO FINAL' as paso;

SELECT 
    estado,
    COUNT(*) as cantidad,
    CONCAT('$', FORMAT(SUM(total), 2)) as total_monto
FROM venta
GROUP BY estado
ORDER BY estado;

-- Resumen por estado
SELECT 
    'Total ventas' as descripcion,
    COUNT(*) as cantidad
FROM venta
UNION ALL
SELECT 
    'Ventas ACTIVAS',
    COUNT(*)
FROM venta
WHERE estado = 'ACTIVA'
UNION ALL
SELECT 
    'Ventas ANULADAS',
    COUNT(*)
FROM venta
WHERE estado = 'ANULADA'
UNION ALL
SELECT 
    'Ventas con NULL (no deberían existir)',
    COUNT(*)
FROM venta
WHERE estado IS NULL;

-- ============================================================
-- RESULTADO FINAL
-- ============================================================
SELECT '✅ PROCESO COMPLETADO - Todos los estados de ventas actualizados correctamente' as mensaje;
