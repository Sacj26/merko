-- Análisis profundo del problema
SELECT 
    'Compras' as tipo,
    COUNT(DISTINCT c.id) as num_registros,
    CONCAT('$', FORMAT(SUM(c.total), 2)) as total,
    CONCAT('$', FORMAT(AVG(c.total), 2)) as promedio_por_registro,
    CONCAT('$', FORMAT(MIN(c.total), 2)) as minimo,
    CONCAT('$', FORMAT(MAX(c.total), 2)) as maximo
FROM compra c
UNION ALL
SELECT 
    'Ventas' as tipo,
    COUNT(DISTINCT v.id) as num_registros,
    CONCAT('$', FORMAT(SUM(v.total), 2)) as total,
    CONCAT('$', FORMAT(AVG(v.total), 2)) as promedio_por_registro,
    CONCAT('$', FORMAT(MIN(v.total), 2)) as minimo,
    CONCAT('$', FORMAT(MAX(v.total), 2)) as maximo
FROM venta v;

-- Ver detalles por registro
SELECT 
    'Detalles Compra' as tipo,
    COUNT(*) as num_detalles,
    CONCAT('$', FORMAT(AVG(dc.precio_unitario), 2)) as precio_unitario_promedio,
    CONCAT('$', FORMAT(MAX(dc.precio_unitario), 2)) as precio_unitario_maximo,
    FORMAT(AVG(dc.cantidad), 2) as cantidad_promedio
FROM detalle_compra dc
UNION ALL
SELECT 
    'Detalles Venta' as tipo,
    COUNT(*) as num_detalles,
    CONCAT('$', FORMAT(AVG(dv.precio_unitario), 2)) as precio_unitario_promedio,
    CONCAT('$', FORMAT(MAX(dv.precio_unitario), 2)) as precio_unitario_maximo,
    FORMAT(AVG(dv.cantidad), 2) as cantidad_promedio
FROM detalle_venta dv;
