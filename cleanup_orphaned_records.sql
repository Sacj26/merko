-- Script para limpiar registros huérfanos en product_branch
-- Estos son registros que referencian productos que ya no existen

-- 1. Primero, ver cuántos registros huérfanos hay
SELECT COUNT(*) as registros_huerfanos
FROM product_branch 
WHERE producto_id NOT IN (SELECT id FROM producto);

-- 2. Ver los detalles de los registros huérfanos (opcional)
SELECT pb.id, pb.producto_id, pb.branch_id, pb.stock
FROM product_branch pb
WHERE pb.producto_id NOT IN (SELECT id FROM producto);

-- 3. Eliminar los registros huérfanos
DELETE FROM product_branch 
WHERE producto_id NOT IN (SELECT id FROM producto);

-- 4. Verificar que se limpiaron correctamente
SELECT COUNT(*) as registros_huerfanos_restantes
FROM product_branch 
WHERE producto_id NOT IN (SELECT id FROM producto);
