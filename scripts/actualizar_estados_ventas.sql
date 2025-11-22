-- =====================================================================
-- Script: Actualizar estados de ventas NULL a ACTIVA
-- Fecha: 2025-11-21
-- Descripción: Actualiza todas las ventas con estado NULL a ACTIVA
-- =====================================================================

-- Ver cuántas ventas tienen estado NULL
SELECT COUNT(*) as ventas_sin_estado
FROM venta
WHERE estado IS NULL;

-- Actualizar ventas sin estado a ACTIVA
UPDATE venta 
SET estado = 'ACTIVA' 
WHERE estado IS NULL;

-- Verificar el resultado
SELECT 
    estado,
    COUNT(*) as cantidad
FROM venta
GROUP BY estado;

SELECT '✅ Estados de ventas actualizados correctamente' as resultado;
