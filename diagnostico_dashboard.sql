-- ============================================
-- DIAGNÓSTICO DASHBOARD MERKO
-- Ejecuta estas consultas en tu base de datos
-- para entender por qué no aparecen los datos
-- ============================================

-- 1. ¿Cuántos productos tienes y cuál es su estado?
SELECT 
    estado,
    COUNT(*) as total,
    SUM(CASE WHEN stock_minimo IS NOT NULL THEN 1 ELSE 0 END) as con_stock_minimo,
    SUM(CASE WHEN stock < stock_minimo THEN 1 ELSE 0 END) as en_critico
FROM producto
GROUP BY estado;

-- 2. ¿Tienes ventas y de qué fechas?
SELECT 
    estado,
    DATE(fecha) as fecha,
    COUNT(*) as cantidad_ventas,
    SUM(total) as total_ventas
FROM venta
GROUP BY estado, DATE(fecha)
ORDER BY fecha DESC
LIMIT 10;

-- 3. ¿Cuántas ventas tienes HOY (2025-10-29)?
SELECT 
    COUNT(*) as ventas_hoy,
    SUM(total) as total_hoy
FROM venta
WHERE DATE(fecha) = '2025-10-29' 
  AND estado = 'ACTIVA';

-- 4. ¿Cuántas ventas tienes en OCTUBRE 2025?
SELECT 
    COUNT(*) as ventas_mes,
    SUM(total) as total_mes
FROM venta
WHERE fecha >= '2025-10-01' 
  AND fecha <= '2025-10-31'
  AND estado = 'ACTIVA';

-- 5. ¿Tienes lotes y cuándo vencen?
SELECT 
    COUNT(*) as total_lotes,
    SUM(CASE WHEN estado = 'ACTIVO' THEN 1 ELSE 0 END) as lotes_activos,
    SUM(CASE WHEN cantidad_disponible > 0 THEN 1 ELSE 0 END) as con_stock,
    MIN(fecha_vencimiento) as primer_vencimiento,
    MAX(fecha_vencimiento) as ultimo_vencimiento
FROM lote;

-- 6. ¿Lotes que vencen en los próximos 30 días?
SELECT 
    l.codigo_lote,
    p.nombre as producto,
    l.fecha_vencimiento,
    l.cantidad_disponible,
    l.estado
FROM lote l
JOIN producto p ON l.producto_id = p.id
WHERE l.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
  AND l.estado = 'ACTIVO'
  AND l.cantidad_disponible > 0
ORDER BY l.fecha_vencimiento ASC
LIMIT 10;

-- 7. Productos en stock crítico
SELECT 
    nombre,
    stock,
    stock_minimo,
    (stock - stock_minimo) as diferencia
FROM producto
WHERE stock_minimo IS NOT NULL 
  AND stock < stock_minimo
ORDER BY diferencia ASC
LIMIT 10;

-- 8. Top 5 productos vendidos (últimos 30 días)
SELECT 
    p.nombre,
    SUM(dv.cantidad) as unidades_vendidas,
    COUNT(DISTINCT v.id) as num_ventas
FROM detalle_venta dv
JOIN venta v ON dv.venta_id = v.id
JOIN producto p ON dv.producto_id = p.id
WHERE v.fecha >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
  AND v.estado = 'ACTIVA'
GROUP BY p.id, p.nombre
ORDER BY unidades_vendidas DESC
LIMIT 5;

-- 9. Resumen general de la base de datos
SELECT 
    'Productos' as tabla,
    COUNT(*) as total
FROM producto
UNION ALL
SELECT 'Ventas', COUNT(*) FROM venta
UNION ALL
SELECT 'Detalles Venta', COUNT(*) FROM detalle_venta
UNION ALL
SELECT 'Lotes', COUNT(*) FROM lote
UNION ALL
SELECT 'Proveedores', COUNT(*) FROM proveedor;
