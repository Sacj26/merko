-- ============================================
-- CONSULTA 3: Stock Crítico
-- Copia solo esta consulta y ejecútala
-- ============================================
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
