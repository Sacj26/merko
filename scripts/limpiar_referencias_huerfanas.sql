-- Script para limpiar referencias huérfanas en producto_proveedor
-- Ejecutar este script para eliminar relaciones a productos que ya no existen

-- Ver cuántas referencias huérfanas hay
SELECT COUNT(*) as referencias_huerfanas
FROM producto_proveedor pp
LEFT JOIN producto p ON pp.producto_id = p.id
WHERE p.id IS NULL;

-- Eliminar referencias huérfanas
DELETE pp FROM producto_proveedor pp
LEFT JOIN producto p ON pp.producto_id = p.id
WHERE p.id IS NULL;

-- Verificar que se eliminaron
SELECT COUNT(*) as referencias_restantes
FROM producto_proveedor;

SELECT CONCAT('✅ Referencias huérfanas eliminadas correctamente') as resultado;
