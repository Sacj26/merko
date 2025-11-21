-- ============================================================
-- VERIFICACIÓN DASHBOARD - NOVIEMBRE 2025
-- Fecha: 2025-11-20
-- ============================================================

SELECT '=== VERIFICACIÓN DATOS NOVIEMBRE 2025 ===' AS seccion;

-- ============================================================
-- COMPRAS NOVIEMBRE 2025
-- ============================================================

-- Método VIEJO (campo total) - Lo que mostraba antes: $5,173,411,850
SELECT 
    'COMPRAS Nov 2025 - VIEJO (campo total)' AS metodo,
    COALESCE(SUM(c.total), 0) AS total_mes,
    COUNT(*) AS num_compras,
    MIN(c.fecha) AS fecha_primera_compra,
    MAX(c.fecha) AS fecha_ultima_compra
FROM compra c
WHERE c.fecha >= '2025-11-01 00:00:00'
  AND c.fecha <= NOW();

-- Método CORRECTO (cantidad * precio_unidad) - Lo que debería mostrar ahora
SELECT 
    'COMPRAS Nov 2025 - CORRECTO (calculado)' AS metodo,
    COALESCE(SUM(c.cantidad * c.precio_unidad), 0) AS total_mes,
    COUNT(*) AS num_compras,
    MIN(c.fecha) AS fecha_primera_compra,
    MAX(c.fecha) AS fecha_ultima_compra
FROM compra c
WHERE c.fecha >= '2025-11-01 00:00:00'
  AND c.fecha <= NOW();

-- Ver compras de noviembre en detalle (primeras 20)
SELECT 
    'Primeras 20 compras de noviembre' AS descripcion,
    c.id,
    c.fecha,
    c.cantidad,
    c.precio_unidad,
    c.total AS total_almacenado,
    (c.cantidad * c.precio_unidad) AS total_calculado,
    (c.total - (c.cantidad * c.precio_unidad)) AS diferencia
FROM compra c
WHERE c.fecha >= '2025-11-01 00:00:00'
  AND c.fecha <= NOW()
ORDER BY c.fecha DESC
LIMIT 20;

-- ============================================================
-- VENTAS NOVIEMBRE 2025
-- ============================================================

-- Método VIEJO (campo total)
SELECT 
    'VENTAS Nov 2025 - VIEJO (campo total)' AS metodo,
    COALESCE(SUM(v.total), 0) AS total_mes,
    COUNT(*) AS num_ventas,
    MIN(v.fecha) AS fecha_primera_venta,
    MAX(v.fecha) AS fecha_ultima_venta
FROM venta v
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
  AND v.fecha >= '2025-11-01 00:00:00'
  AND v.fecha <= NOW();

-- Método CORRECTO (calculado desde detalles) - Lo que debería mostrar ahora
SELECT 
    'VENTAS Nov 2025 - CORRECTO (desde detalles)' AS metodo,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS total_mes,
    COUNT(DISTINCT v.id) AS num_ventas,
    COUNT(*) AS num_detalles,
    MIN(v.fecha) AS fecha_primera_venta,
    MAX(v.fecha) AS fecha_ultima_venta
FROM venta v
INNER JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
  AND v.fecha >= '2025-11-01 00:00:00'
  AND v.fecha <= NOW();

-- Ver ventas de noviembre en detalle (primeras 10)
SELECT 
    'Primeras 10 ventas de noviembre' AS descripcion,
    v.id,
    v.fecha,
    v.total AS total_almacenado,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS total_calculado_detalles,
    (v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) AS diferencia,
    COUNT(dv.id) AS num_items
FROM venta v
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
  AND v.fecha >= '2025-11-01 00:00:00'
  AND v.fecha <= NOW()
GROUP BY v.id, v.fecha, v.total
ORDER BY v.fecha DESC
LIMIT 10;

-- ============================================================
-- COMPARACIÓN CON LOS VALORES DEL DASHBOARD
-- ============================================================

SELECT '=== COMPARACIÓN DASHBOARD ===' AS seccion;

SELECT 
    'Dashboard reporta' AS fuente,
    5173411850 AS compras_mostradas,
    680500610 AS ventas_mostradas
UNION ALL
SELECT 
    'MySQL - Compras calculadas' AS fuente,
    COALESCE(SUM(c.cantidad * c.precio_unidad), 0) AS compras_calculadas,
    NULL AS ventas
FROM compra c
WHERE c.fecha >= '2025-11-01 00:00:00'
  AND c.fecha <= NOW()
UNION ALL
SELECT 
    'MySQL - Ventas calculadas' AS fuente,
    NULL AS compras,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS ventas_calculadas
FROM venta v
INNER JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
  AND v.fecha >= '2025-11-01 00:00:00'
  AND v.fecha <= NOW();

-- ============================================================
-- DISTRIBUCIÓN DE FECHAS
-- ============================================================

SELECT '=== DISTRIBUCIÓN DE FECHAS ===' AS seccion;

-- Ver cuántas compras hay por día en noviembre
SELECT 
    'Compras por día (Nov 2025)' AS tipo,
    DATE(c.fecha) AS fecha,
    COUNT(*) AS num_registros,
    COALESCE(SUM(c.cantidad * c.precio_unidad), 0) AS total_dia
FROM compra c
WHERE c.fecha >= '2025-11-01 00:00:00'
  AND c.fecha <= NOW()
GROUP BY DATE(c.fecha)
ORDER BY fecha DESC;

-- Ver cuántas ventas hay por día en noviembre
SELECT 
    'Ventas por día (Nov 2025)' AS tipo,
    DATE(v.fecha) AS fecha,
    COUNT(DISTINCT v.id) AS num_ventas,
    COUNT(dv.id) AS num_items,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS total_dia
FROM venta v
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
  AND v.fecha >= '2025-11-01 00:00:00'
  AND v.fecha <= NOW()
GROUP BY DATE(v.fecha)
ORDER BY fecha DESC;

-- ============================================================
-- VERIFICAR DATOS EXTRAÑOS
-- ============================================================

SELECT '=== VERIFICAR DATOS SOSPECHOSOS ===' AS seccion;

-- Compras con valores muy altos (> $1M)
SELECT 
    'Compras con total > $1,000,000' AS descripcion,
    c.id,
    c.fecha,
    c.cantidad,
    c.precio_unidad,
    (c.cantidad * c.precio_unidad) AS total_calculado,
    c.total AS total_almacenado
FROM compra c
WHERE c.fecha >= '2025-11-01 00:00:00'
  AND c.fecha <= NOW()
  AND (c.cantidad * c.precio_unidad) > 1000000
ORDER BY (c.cantidad * c.precio_unidad) DESC
LIMIT 20;

-- Ventas con valores muy altos (> $100k por venta)
SELECT 
    'Ventas con total > $100,000' AS descripcion,
    v.id,
    v.fecha,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS total_calculado,
    v.total AS total_almacenado,
    COUNT(dv.id) AS num_items
FROM venta v
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
  AND v.fecha >= '2025-11-01 00:00:00'
  AND v.fecha <= NOW()
GROUP BY v.id, v.fecha, v.total
HAVING COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) > 100000
ORDER BY total_calculado DESC
LIMIT 20;
