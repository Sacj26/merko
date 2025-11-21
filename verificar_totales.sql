-- ============================================================
-- SCRIPT DE VERIFICACIÓN DE TOTALES
-- Base de datos: Merko
-- Fecha: 2025-11-20
-- ============================================================

-- ============================================================
-- PARTE 1: VERIFICAR COMPRAS
-- ============================================================

SELECT 
    '=== VERIFICACIÓN DE COMPRAS ===' AS seccion;

-- 1.1: Total usando campo 'total' (método INCORRECTO usado actualmente)
SELECT 
    'Compras - Total de campo total (INCORRECTO)' AS metodo,
    COALESCE(SUM(c.total), 0) AS total_calculado,
    COUNT(*) AS num_registros
FROM compra c;

-- 1.2: Total calculado desde cantidad * precio_unidad (método CORRECTO que acabamos de implementar)
SELECT 
    'Compras - Total calculado (cantidad * precioUnidad)' AS metodo,
    COALESCE(SUM(c.cantidad * c.precio_unidad), 0) AS total_calculado,
    COUNT(*) AS num_registros
FROM compra c;

-- 1.3: Comparación directa (diferencia entre ambos métodos)
SELECT 
    'Compras - DIFERENCIA' AS metodo,
    COALESCE(SUM(c.total), 0) - COALESCE(SUM(c.cantidad * c.precio_unidad), 0) AS diferencia,
    COALESCE(SUM(c.total), 0) AS total_campo,
    COALESCE(SUM(c.cantidad * c.precio_unidad), 0) AS total_calculado
FROM compra c;

-- 1.4: Ver compras con discrepancias (top 10)
SELECT 
    'Top 10 compras con discrepancia' AS descripcion,
    c.id,
    c.fecha,
    c.cantidad,
    c.precio_unidad,
    c.total AS total_almacenado,
    (c.cantidad * c.precio_unidad) AS total_calculado,
    (c.total - (c.cantidad * c.precio_unidad)) AS diferencia
FROM compra c
WHERE ABS(c.total - (c.cantidad * c.precio_unidad)) > 0.01
ORDER BY ABS(c.total - (c.cantidad * c.precio_unidad)) DESC
LIMIT 10;

-- ============================================================
-- PARTE 2: VERIFICAR VENTAS
-- ============================================================

SELECT 
    '=== VERIFICACIÓN DE VENTAS ===' AS seccion;

-- 2.1: Total usando campo 'total' (método ACTUAL en VentaRepository)
SELECT 
    'Ventas - Total de campo total (ACTUAL)' AS metodo,
    COALESCE(SUM(v.total), 0) AS total_calculado,
    COUNT(*) AS num_registros
FROM venta v
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL);

-- 2.2: Total calculado desde detalles (suma de cantidad * precio_unitario en detalle_venta)
SELECT 
    'Ventas - Total calculado desde detalles' AS metodo,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS total_calculado,
    COUNT(DISTINCT dv.venta_id) AS num_ventas,
    COUNT(*) AS num_detalles
FROM detalle_venta dv
INNER JOIN venta v ON dv.venta_id = v.id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL);

-- 2.3: Comparación directa ventas (diferencia)
SELECT 
    'Ventas - DIFERENCIA' AS metodo,
    (SELECT COALESCE(SUM(v.total), 0) FROM venta v WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)) 
    - 
    (SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) 
     FROM detalle_venta dv
     INNER JOIN venta v ON dv.venta_id = v.id
     WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)) AS diferencia,
    (SELECT COALESCE(SUM(v.total), 0) FROM venta v WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)) AS total_campo,
    (SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) 
     FROM detalle_venta dv
     INNER JOIN venta v ON dv.venta_id = v.id
     WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)) AS total_calculado_detalles;

-- 2.4: Ver ventas con discrepancias (top 10)
SELECT 
    'Top 10 ventas con discrepancia' AS descripcion,
    v.id,
    v.fecha,
    v.total AS total_almacenado,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS total_calculado_detalles,
    (v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) AS diferencia
FROM venta v
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
GROUP BY v.id, v.fecha, v.total
HAVING ABS(v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) > 0.01
ORDER BY ABS(v.total - COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)) DESC
LIMIT 10;

-- ============================================================
-- PARTE 3: CONTEO DE REGISTROS
-- ============================================================

SELECT 
    '=== CONTEO DE REGISTROS ===' AS seccion;

SELECT 
    'Compras' AS tabla,
    COUNT(*) AS total_registros
FROM compra
UNION ALL
SELECT 
    'Detalle Compra' AS tabla,
    COUNT(*) AS total_registros
FROM detalle_compra
UNION ALL
SELECT 
    'Ventas (ACTIVAS)' AS tabla,
    COUNT(*) AS total_registros
FROM venta
WHERE (estado = 'ACTIVA' OR estado IS NULL)
UNION ALL
SELECT 
    'Detalle Venta' AS tabla,
    COUNT(*) AS total_registros
FROM detalle_venta;

-- ============================================================
-- PARTE 4: TOTALES DEL MES ACTUAL (Dashboard)
-- ============================================================

SELECT 
    '=== TOTALES DEL MES ACTUAL (Como en Dashboard) ===' AS seccion;

-- 4.1: Compras del mes (método CORRECTO que implementamos)
SELECT 
    'Compras del mes (CORRECTO - cantidad * precio_unidad)' AS metodo,
    COALESCE(SUM(c.cantidad * c.precio_unidad), 0) AS total_mes,
    COUNT(*) AS num_compras
FROM compra c
WHERE c.fecha >= DATE_FORMAT(NOW(), '%Y-%m-01 00:00:00')
  AND c.fecha <= NOW();

-- 4.2: Compras del mes (método VIEJO - campo total)
SELECT 
    'Compras del mes (VIEJO - campo total)' AS metodo,
    COALESCE(SUM(c.total), 0) AS total_mes,
    COUNT(*) AS num_compras
FROM compra c
WHERE c.fecha >= DATE_FORMAT(NOW(), '%Y-%m-01 00:00:00')
  AND c.fecha <= NOW();

-- 4.3: Ventas del mes (método ACTUAL - campo total)
SELECT 
    'Ventas del mes (ACTUAL - campo total)' AS metodo,
    COALESCE(SUM(v.total), 0) AS total_mes,
    COUNT(*) AS num_ventas
FROM venta v
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
  AND v.fecha >= DATE_FORMAT(NOW(), '%Y-%m-01 00:00:00')
  AND v.fecha <= NOW();

-- 4.4: Ventas del mes (calculado desde detalles)
SELECT 
    'Ventas del mes (Calculado desde detalles)' AS metodo,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS total_mes,
    COUNT(DISTINCT v.id) AS num_ventas,
    COUNT(*) AS num_detalles
FROM venta v
INNER JOIN detalle_venta dv ON v.id = dv.venta_id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
  AND v.fecha >= DATE_FORMAT(NOW(), '%Y-%m-01 00:00:00')
  AND v.fecha <= NOW();

-- ============================================================
-- PARTE 5: RESUMEN EJECUTIVO
-- ============================================================

SELECT 
    '=== RESUMEN EJECUTIVO ===' AS seccion;

SELECT 
    'COMPRAS' AS tipo,
    'Campo total' AS metodo_calculo,
    COALESCE(SUM(c.total), 0) AS total_historico,
    COUNT(*) AS registros
FROM compra c
UNION ALL
SELECT 
    'COMPRAS' AS tipo,
    'Calculado (cantidad * precio)' AS metodo_calculo,
    COALESCE(SUM(c.cantidad * c.precio_unidad), 0) AS total_historico,
    COUNT(*) AS registros
FROM compra c
UNION ALL
SELECT 
    'VENTAS' AS tipo,
    'Campo total' AS metodo_calculo,
    COALESCE(SUM(v.total), 0) AS total_historico,
    COUNT(*) AS registros
FROM venta v
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL)
UNION ALL
SELECT 
    'VENTAS' AS tipo,
    'Calculado desde detalles' AS metodo_calculo,
    COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0) AS total_historico,
    COUNT(DISTINCT dv.venta_id) AS registros
FROM detalle_venta dv
INNER JOIN venta v ON dv.venta_id = v.id
WHERE (v.estado = 'ACTIVA' OR v.estado IS NULL);
