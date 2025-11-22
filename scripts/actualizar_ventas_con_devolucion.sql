-- =====================================================================
-- Script: Actualizar estado de ventas que tienen devoluciones
-- Fecha: 2025-11-21
-- Descripción: Cambia a ANULADA las ventas que tienen devoluciones registradas
-- =====================================================================

-- Ver cuántas ventas tienen devoluciones
SELECT 
    COUNT(DISTINCT v.id) as ventas_con_devolucion,
    v.estado as estado_actual
FROM venta v
INNER JOIN devolucion d ON v.id = d.venta_id
GROUP BY v.estado;

-- Actualizar estado de ventas que tienen devoluciones a ANULADA
UPDATE venta v
INNER JOIN devolucion d ON v.id = d.venta_id
SET v.estado = 'ANULADA'
WHERE v.estado != 'ANULADA' OR v.estado IS NULL;

-- Verificar el resultado
SELECT 
    v.id as venta_id,
    v.fecha,
    v.estado,
    COUNT(d.id) as num_devoluciones
FROM venta v
INNER JOIN devolucion d ON v.id = d.venta_id
GROUP BY v.id, v.fecha, v.estado
ORDER BY v.id;

SELECT '✅ Estados de ventas con devoluciones actualizados a ANULADA' as resultado;
