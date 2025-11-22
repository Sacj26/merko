-- Script para crear detalles faltantes en compras sin registros en detalle_compra
-- Este script crea detalles automáticos basándose en la información de la tabla compra
-- Fecha: 2025-11-21
-- Base de datos: merko

USE merko;

-- ============================================================
-- PASO 1: ANALIZAR COMPRAS SIN DETALLES
-- ============================================================

-- Ver cuántas compras no tienen detalles
SELECT 
    COUNT(*) as compras_sin_detalles,
    MIN(fecha) as fecha_mas_antigua,
    MAX(fecha) as fecha_mas_reciente,
    SUM(total) as suma_totales,
    AVG(total) as promedio_total
FROM compra c
WHERE c.id NOT IN (SELECT DISTINCT compra_id FROM detalle_compra);

-- Ver ejemplos de compras sin detalles (primeras 20)
SELECT 
    c.id,
    c.fecha,
    c.cantidad,
    c.precio_unidad,
    c.total,
    c.branch_id,
    b.nombre as sucursal,
    p.nombre as proveedor
FROM compra c
LEFT JOIN branch b ON c.branch_id = b.id
LEFT JOIN proveedor p ON b.proveedor_id = p.id
WHERE c.id NOT IN (SELECT DISTINCT compra_id FROM detalle_compra)
ORDER BY c.fecha DESC
LIMIT 20;

-- ============================================================
-- PASO 2: ESTRATEGIA PARA ASIGNAR PRODUCTOS
-- ============================================================
-- Vamos a usar el producto más común del proveedor de la sucursal
-- Si no hay sucursal/proveedor, usaremos un producto genérico activo

-- Ver distribución de productos por proveedor (para validar la estrategia)
SELECT 
    p.id as proveedor_id,
    p.nombre as proveedor,
    prod.id as producto_id,
    prod.nombre as producto,
    COUNT(*) as veces_comprado
FROM detalle_compra dc
INNER JOIN compra c ON dc.compra_id = c.id
INNER JOIN producto prod ON dc.producto_id = prod.id
INNER JOIN branch b ON c.branch_id = b.id
INNER JOIN proveedor p ON b.proveedor_id = p.id
GROUP BY p.id, p.nombre, prod.id, prod.nombre
ORDER BY p.id, veces_comprado DESC
LIMIT 50;

-- ============================================================
-- PASO 3: CREAR DETALLES FALTANTES (EJECUTAR CON CUIDADO)
-- ============================================================

-- IMPORTANTE: Este INSERT crea detalles para compras sin detalles
-- La lógica:
-- 1. Si la compra tiene branch con proveedor, usa el producto más común de ese proveedor
-- 2. Si no, usa el producto más común de todos los detalles
-- 3. Si no hay ninguno, usa cualquier producto que exista
-- 4. El detalle tendrá: cantidad de la compra, precio_unitario y precio_compra = total

INSERT INTO detalle_compra (compra_id, producto_id, cantidad, precio_unitario, precio_compra)
SELECT 
    c.id as compra_id,
    -- Seleccionar producto con múltiples fallbacks, VERIFICANDO QUE EXISTA
    COALESCE(
        -- Opción 1: Producto más vendido del proveedor (que EXISTA en tabla producto)
        (
            SELECT dc2.producto_id
            FROM detalle_compra dc2
            INNER JOIN compra c2 ON dc2.compra_id = c2.id
            INNER JOIN branch b2 ON c2.branch_id = b2.id
            INNER JOIN producto p ON dc2.producto_id = p.id  -- VERIFICAR que existe
            WHERE b2.proveedor_id = b.proveedor_id
            GROUP BY dc2.producto_id
            ORDER BY COUNT(*) DESC
            LIMIT 1
        ),
        -- Opción 2: Producto más comprado en general (que EXISTA en tabla producto)
        (
            SELECT dc2.producto_id
            FROM detalle_compra dc2
            INNER JOIN producto p ON dc2.producto_id = p.id  -- VERIFICAR que existe
            GROUP BY dc2.producto_id
            ORDER BY COUNT(*) DESC
            LIMIT 1
        ),
        -- Opción 3: Cualquier producto activo que exista
        (SELECT id FROM producto WHERE activo = TRUE ORDER BY id LIMIT 1),
        -- Opción 4: Cualquier producto que exista (aunque no esté activo)
        (SELECT id FROM producto ORDER BY id LIMIT 1)
    ) as producto_id,
    c.cantidad,
    c.precio_unidad,
    c.total as precio_compra
FROM compra c
LEFT JOIN branch b ON c.branch_id = b.id
WHERE c.id NOT IN (SELECT DISTINCT compra_id FROM detalle_compra)
    AND c.cantidad > 0
    AND c.total > 0
    -- IMPORTANTE: Solo insertar si tenemos un producto válido
    AND EXISTS (SELECT 1 FROM producto LIMIT 1);

-- ============================================================
-- PASO 4: VERIFICAR LOS DETALLES CREADOS
-- ============================================================

-- Ver cuántos detalles se crearon
SELECT 
    COUNT(*) as detalles_creados,
    MIN(compra_id) as primera_compra,
    MAX(compra_id) as ultima_compra
FROM detalle_compra
WHERE id > (
    SELECT MAX(id) - 100000 FROM detalle_compra  -- Últimos registros creados
);

-- Verificar que ahora todas las compras tienen detalles
SELECT 
    COUNT(DISTINCT c.id) as total_compras,
    COUNT(DISTINCT dc.compra_id) as compras_con_detalles,
    COUNT(DISTINCT c.id) - COUNT(DISTINCT dc.compra_id) as compras_sin_detalles
FROM compra c
LEFT JOIN detalle_compra dc ON c.id = dc.compra_id;

-- Ver ejemplos de los detalles creados (últimos 20)
SELECT 
    dc.id as detalle_id,
    dc.compra_id,
    c.fecha,
    prod.nombre as producto,
    dc.cantidad,
    dc.precio_unitario,
    dc.precio_compra,
    c.total as total_compra,
    (dc.precio_compra - c.total) as diferencia
FROM detalle_compra dc
INNER JOIN compra c ON dc.compra_id = c.id
INNER JOIN producto prod ON dc.producto_id = prod.id
WHERE dc.id > (SELECT MAX(id) - 100 FROM detalle_compra)
ORDER BY dc.id DESC
LIMIT 20;

-- ============================================================
-- PASO 5: VALIDAR INTEGRIDAD
-- ============================================================

-- Verificar que los totales coincidan después de crear detalles
SELECT 
    c.id as compra_id,
    c.fecha,
    c.total as total_compra,
    COALESCE(SUM(dc.precio_compra), 0) as suma_detalles,
    (c.total - COALESCE(SUM(dc.precio_compra), 0)) as diferencia,
    COUNT(dc.id) as num_detalles
FROM compra c
LEFT JOIN detalle_compra dc ON c.id = dc.compra_id
GROUP BY c.id, c.fecha, c.total
HAVING ABS(c.total - COALESCE(SUM(dc.precio_compra), 0)) > 0.01
LIMIT 20;

-- Resumen final
SELECT 
    'Compras totales' as metrica,
    COUNT(*) as valor
FROM compra
UNION ALL
SELECT 
    'Compras con detalles',
    COUNT(DISTINCT compra_id)
FROM detalle_compra
UNION ALL
SELECT 
    'Detalles totales',
    COUNT(*)
FROM detalle_compra
UNION ALL
SELECT 
    'Productos distintos en detalles',
    COUNT(DISTINCT producto_id)
FROM detalle_compra;
