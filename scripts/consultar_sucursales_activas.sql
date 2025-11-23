-- ============================================
-- Script: Consultar Sucursales Activas
-- Descripción: Muestra todas las sucursales activas con información completa
-- Fecha: 2025-11-22
-- ============================================

-- Consulta básica de sucursales activas
SELECT 
    b.id AS sucursal_id,
    b.nombre AS sucursal_nombre,
    b.direccion,
    b.ciudad,
    b.pais,
    b.telefono,
    b.activo,
    p.id AS proveedor_id,
    p.nombre AS proveedor_nombre,
    p.email AS proveedor_email
FROM branch b
LEFT JOIN proveedor p ON b.proveedor_id = p.id
WHERE b.activo = TRUE
ORDER BY b.ciudad, b.nombre;

-- ============================================
-- Consulta con conteo de productos por sucursal
-- ============================================
SELECT 
    b.id AS sucursal_id,
    b.nombre AS sucursal_nombre,
    b.ciudad,
    b.direccion,
    p.nombre AS proveedor_nombre,
    COUNT(DISTINCT pb.producto_id) AS total_productos,
    SUM(pb.stock) AS stock_total,
    COUNT(DISTINCT cp.id) AS total_contactos
FROM branch b
LEFT JOIN proveedor p ON b.proveedor_id = p.id
LEFT JOIN product_branch pb ON b.id = pb.branch_id
LEFT JOIN contact_person cp ON b.id = cp.branch_id
WHERE b.activo = TRUE
GROUP BY b.id, b.nombre, b.ciudad, b.direccion, p.nombre
ORDER BY total_productos DESC, b.ciudad;

-- ============================================
-- Consulta con información de ventas y compras
-- ============================================
SELECT 
    b.id AS sucursal_id,
    b.nombre AS sucursal_nombre,
    b.ciudad,
    b.pais,
    b.telefono,
    p.nombre AS proveedor_nombre,
    -- Métricas de ventas
    COUNT(DISTINCT v.id) AS total_ventas,
    COALESCE(SUM(v.total), 0) AS monto_total_ventas,
    -- Métricas de compras
    COUNT(DISTINCT c.id) AS total_compras,
    COALESCE(SUM(c.total), 0) AS monto_total_compras,
    -- Inventario
    COUNT(DISTINCT pb.producto_id) AS productos_disponibles,
    SUM(pb.stock) AS stock_total
FROM branch b
LEFT JOIN proveedor p ON b.proveedor_id = p.id
LEFT JOIN venta v ON b.id = v.branch_id AND v.estado = 'ACTIVA'
LEFT JOIN compra c ON b.id = c.branch_id
LEFT JOIN product_branch pb ON b.id = pb.branch_id
WHERE b.activo = TRUE
GROUP BY b.id, b.nombre, b.ciudad, b.pais, b.telefono, p.nombre
ORDER BY monto_total_ventas DESC;

-- ============================================
-- Consulta con contactos de cada sucursal
-- ============================================
SELECT 
    b.id AS sucursal_id,
    b.nombre AS sucursal_nombre,
    b.ciudad,
    b.direccion,
    b.telefono AS telefono_sucursal,
    p.nombre AS proveedor_nombre,
    cp.nombre AS contacto_nombre,
    cp.cargo AS contacto_cargo,
    cp.email AS contacto_email,
    cp.telefono AS contacto_telefono
FROM branch b
LEFT JOIN proveedor p ON b.proveedor_id = p.id
LEFT JOIN contact_person cp ON b.id = cp.branch_id
WHERE b.activo = TRUE
ORDER BY b.ciudad, b.nombre, cp.nombre;

-- ============================================
-- Resumen ejecutivo de sucursales activas
-- ============================================
SELECT 
    COUNT(*) AS total_sucursales_activas,
    COUNT(DISTINCT ciudad) AS total_ciudades,
    COUNT(DISTINCT pais) AS total_paises,
    COUNT(DISTINCT proveedor_id) AS total_proveedores
FROM branch
WHERE activo = TRUE;

-- ============================================
-- Sucursales por ciudad
-- ============================================
SELECT 
    ciudad,
    pais,
    COUNT(*) AS cantidad_sucursales,
    GROUP_CONCAT(nombre SEPARATOR ', ') AS sucursales
FROM branch
WHERE activo = TRUE
GROUP BY ciudad, pais
ORDER BY cantidad_sucursales DESC, ciudad;

-- ============================================
-- Sucursales con stock crítico (productos bajo mínimo)
-- ============================================
SELECT 
    b.id AS sucursal_id,
    b.nombre AS sucursal_nombre,
    b.ciudad,
    COUNT(DISTINCT CASE 
        WHEN pb.stock <= p.stock_minimo THEN p.id 
    END) AS productos_stock_critico,
    COUNT(DISTINCT CASE 
        WHEN pb.stock <= p.punto_reorden THEN p.id 
    END) AS productos_punto_reorden,
    COUNT(DISTINCT pb.producto_id) AS total_productos,
    ROUND(
        COUNT(DISTINCT CASE WHEN pb.stock <= p.stock_minimo THEN p.id END) * 100.0 / 
        NULLIF(COUNT(DISTINCT pb.producto_id), 0), 
        2
    ) AS porcentaje_critico
FROM branch b
LEFT JOIN product_branch pb ON b.id = pb.branch_id
LEFT JOIN producto p ON pb.producto_id = p.id
WHERE b.activo = TRUE
GROUP BY b.id, b.nombre, b.ciudad
HAVING COUNT(DISTINCT pb.producto_id) > 0
ORDER BY porcentaje_critico DESC, productos_stock_critico DESC;

-- ============================================
-- Sucursales con mejor performance (últimos 30 días)
-- ============================================
SELECT 
    b.id AS sucursal_id,
    b.nombre AS sucursal_nombre,
    b.ciudad,
    COUNT(DISTINCT v.id) AS ventas_ultimos_30_dias,
    COALESCE(SUM(v.total), 0) AS total_ventas_30_dias,
    ROUND(AVG(v.total), 2) AS ticket_promedio,
    COUNT(DISTINCT v.cliente_id) AS clientes_unicos,
    COUNT(DISTINCT dv.producto_id) AS productos_vendidos
FROM branch b
LEFT JOIN venta v ON b.id = v.branch_id 
    AND v.estado = 'ACTIVA'
    AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE b.activo = TRUE
GROUP BY b.id, b.nombre, b.ciudad
ORDER BY total_ventas_30_dias DESC;
