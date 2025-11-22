-- =====================================================================
-- Script: Recalcular campo precio_compra en detalle_compra
-- Fecha: 2025-11-21
-- Descripción: El campo precio_compra debe ser = cantidad * precio_unitario
--              Después del ajuste de cantidades, este campo quedó desactualizado
-- =====================================================================

-- PASO 1: Ver estado actual
SELECT 
    'Registros totales' as descripcion,
    COUNT(*) as cantidad
FROM detalle_compra
UNION ALL
SELECT 
    'Con precio_compra != cantidad*precioUnitario',
    COUNT(*)
FROM detalle_compra
WHERE ABS(precio_compra - (cantidad * precio_unitario)) > 0.01;

-- PASO 2: Ver ejemplos de discrepancias
SELECT 
    id,
    cantidad,
    CONCAT('$', FORMAT(precio_unitario, 2)) as precio_unitario,
    CONCAT('$', FORMAT(precio_compra, 2)) as precio_compra_actual,
    CONCAT('$', FORMAT(cantidad * precio_unitario, 2)) as precio_compra_correcto,
    CONCAT('$', FORMAT(ABS(precio_compra - (cantidad * precio_unitario)), 2)) as diferencia
FROM detalle_compra
WHERE ABS(precio_compra - (cantidad * precio_unitario)) > 0.01
LIMIT 10;

-- PASO 3: ACTUALIZAR precio_compra = cantidad * precio_unitario
UPDATE detalle_compra
SET precio_compra = cantidad * precio_unitario;

SELECT CONCAT('✅ Actualizados ', ROW_COUNT(), ' registros en detalle_compra') as resultado;

-- PASO 4: Verificar que ya no hay discrepancias
SELECT 
    COUNT(*) as registros_con_discrepancia
FROM detalle_compra
WHERE ABS(precio_compra - (cantidad * precio_unitario)) > 0.01;

-- PASO 5: Verificar totales finales
SELECT 
    'Total desde precio_compra' as metodo,
    CONCAT('$', FORMAT(SUM(precio_compra), 2)) as total
FROM detalle_compra
UNION ALL
SELECT 
    'Total desde cantidad*precio_unitario',
    CONCAT('$', FORMAT(SUM(cantidad * precio_unitario), 2))
FROM detalle_compra;

-- =====================================================================
-- RESULTADO ESPERADO:
-- - Todos los registros de detalle_compra con precio_compra correcto
-- - precio_compra = cantidad * precio_unitario
-- - 0 discrepancias
-- - Vista de detalles mostrará valores correctos
-- =====================================================================

SELECT '✅ CORRECCIÓN COMPLETADA - Campo precio_compra actualizado' as mensaje;
