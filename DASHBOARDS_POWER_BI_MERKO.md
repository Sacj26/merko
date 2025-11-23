# 📊 ESTRUCTURA DE DASHBOARDS POWER BI - PROYECTO MERKO
## Objetivos Realistas Basados en el Modelo de Negocio

---

## 📋 CONTEXTO DEL PROYECTO MERKO

**Tipo de Negocio**: Sistema de gestión de inventario y ventas multi-sucursal  
**Modelo**: B2B (proveedores) y B2C (clientes finales)  
**Características**:
- Multi-sucursal (Branches)
- Gestión de proveedores y sus sucursales
- Control de inventario por producto y sucursal (product_branch)
- Gestión de lotes y fechas de vencimiento
- Sistema de compras y ventas con detalles
- Trazabilidad de movimientos de inventario
- Carrito de compras y eventos de usuario
- Sistema de envíos y pagos

**Estructura de Datos Principales**:
- `producto`: precio_compra, precio_venta, stock_minimo, punto_reorden, lead_time_dias
- `venta`: total, fecha, estado, channel, discount_amount, dispatch_date
- `detalle_venta`: cantidad, precio_unitario (por producto y sucursal)
- `compra`: total, fecha, cantidad, precio_unidad
- `detalle_compra`: cantidad, precio_unitario, precio_compra
- `product_branch`: stock por producto y sucursal
- `lote`: fecha_vencimiento, cantidad_disponible
- `cart_event`: timestamp, tipo_evento (add_to_cart, remove, checkout, etc.)
- `shipment`: fecha_envio, estado
- `movimiento_inventario`: tipo, cantidad, costo_unitario

---

## 🎯 PÁGINA 1: RESUMEN EJECUTIVO

### KPIs Principales:

#### 1. **Ventas del Mes vs Objetivo**
- **Objetivo Base**: **$15,000,000 COP mensual** (~$3,750 USD)
  - Cálculo: Promedio de ventas pequeño comercio en Colombia
  - Justificación: Sistema multi-sucursal en fase de crecimiento
- **Comparación**: 
  - Mes actual vs mes anterior (**Meta: +5% mensual**)
  - Mismo mes año anterior (**Meta: +15% anual**)
  - Q-o-Q (Quarter over Quarter): **+10-12%**
- **Fuente Datos**: `SUM(detalle_venta.cantidad * detalle_venta.precio_unitario) WHERE venta.estado = 'ACTIVA'`
- **Semáforo**:
  - 🟢 Verde: ≥ 100% del objetivo
  - 🟡 Amarillo: 85-99% del objetivo
  - 🔴 Rojo: < 85% del objetivo

#### 2. **Margen de Rentabilidad Global**
- **Objetivo**: **28-32%** (promedio ponderado)
  - Justificación: Comercio minorista colombiano típico (20-40%)
  - Productos perecederos: 15-25%
  - Productos no perecederos: 30-45%
  - Productos importados: 35-50%
- **Fórmula**: 
  ```
  Margen % = ((precio_venta - precio_compra) / precio_venta) * 100
  Por venta: ((SUM(dv.cantidad * dv.precio_unitario) - SUM(dv.cantidad * p.precio_compra)) / SUM(dv.cantidad * dv.precio_unitario)) * 100
  ```
- **Meta por Categoría**:
  - Alimentos básicos: 18-22%
  - Bebidas: 25-30%
  - Productos de limpieza: 30-35%
  - Tecnología/Electrónica: 15-20%
  - Ropa/Textil: 40-50%

#### 3. **Tasa de Cumplimiento de Pedidos**
- **Objetivo**: **≥ 92%** (realista para operación en crecimiento)
  - Meta a corto plazo (3 meses): 92%
  - Meta a mediano plazo (6 meses): 95%
  - Meta a largo plazo (12 meses): 97%
- **Fórmula**: 
  ```
  (COUNT(venta WHERE estado = 'ACTIVA' AND dispatch_date IS NOT NULL) / 
   COUNT(venta WHERE estado IN ('ACTIVA', 'PENDIENTE'))) * 100
  ```
- **Razones de Incumplimiento a Monitorear**:
  - Stock insuficiente (meta: < 3% de casos)
  - Productos vencidos/próximos a vencer (meta: < 1%)
  - Problemas logísticos (meta: < 2%)
  - Cancelaciones de cliente (meta: < 2%)

#### 4. **Rotación de Inventario**
- **Objetivo**: **8-10 veces al año** (0.67-0.83 veces/mes)
  - Productos perecederos: 15-20 veces/año (gestiona_lotes = TRUE, requiere_vencimiento = TRUE)
  - Productos no perecederos: 6-8 veces/año
  - Productos de baja rotación: 4-6 veces/año
- **Fórmula**: 
  ```
  Rotación Anual = Costo de Ventas Anual / Valor Promedio Inventario
  Costo Ventas = SUM(detalle_venta.cantidad * producto.precio_compra)
  Inventario Promedio = AVG(SUM(product_branch.stock * producto.precio_compra))
  ```
- **Días de Inventario**: **36-45 días** (360 / rotación)

### Visualizaciones:

1. **Tarjetas de KPIs con Indicador de Cumplimiento**
   - Valor actual vs objetivo
   - % de cumplimiento
   - Tendencia (↑ ↓ →)
   - Color semáforo

2. **Gráfico de Líneas: Evolución Ventas Últimos 12 Meses**
   - Serie 1: Ventas reales (`SUM(venta.total)` por mes)
   - Serie 2: Objetivo lineal ($15M base + 5% mensual)
   - Serie 3: Mismo período año anterior (Year-over-Year)
   - Área sombreada: Rango objetivo (85%-115%)

3. **Gráfico de Barras: Top 10 Productos Más Vendidos**
   - Eje X: Producto (nombre + SKU)
   - Eje Y: Cantidad vendida (`SUM(detalle_venta.cantidad)`)
   - Color por margen: 
     - Verde: > 30%
     - Amarillo: 20-30%
     - Naranja: 10-20%
     - Rojo: < 10%
   - Tooltip: Total en COP, unidades, margen %

4. **Mapa: Ventas por Sucursal**
   - Burbujas por `branch.ciudad`
   - Tamaño: Total ventas
   - Color: Crecimiento vs mes anterior
   - Tooltip: Nombre sucursal, dirección, total ventas, # transacciones

---

## 💰 PÁGINA 2: ANÁLISIS DE VENTAS

### KPIs:

#### 1. **Ticket Promedio**
- **Objetivo**: **$85,000 COP** (~$21 USD)
  - Objetivo de crecimiento: **+8% anual** (inflación + valor agregado)
  - Meta mensual: +0.6% respecto al mes anterior
- **Fórmula**: 
  ```
  Ticket Promedio = SUM(venta.total) / COUNT(DISTINCT venta.id)
  ```
- **Segmentación**:
  - Por canal (online, tienda física, telefónico)
  - Por hora del día
  - Por día de semana
  - Por sucursal

#### 2. **Ventas por Canal**
- **Objetivo de Distribución** (evitar dependencia):
  - Tienda física: 50-60% (canal principal establecido)
  - Online/E-commerce: 25-35% (en crecimiento, meta +3% mensual)
  - Telefónico/WhatsApp: 10-15%
  - B2B/Mayorista: 5-10%
- **Meta**: Ningún canal > 65% (diversificación de riesgo)
- **Fuente**: `venta.channel`
- **Crecimiento Esperado Online**: +20% trimestral

#### 3. **Tasa de Devoluciones**
- **Objetivo Global**: **≤ 1.8%**
  - Por defecto de producto: ≤ 0.5%
  - Por error en pedido: ≤ 0.3%
  - Por arrepentimiento cliente: ≤ 0.8%
  - Por vencimiento/calidad: ≤ 0.2%
- **Fórmula**: 
  ```
  (COUNT(devolucion) / COUNT(detalle_venta)) * 100
  ```
- **Costo de Devoluciones**: ≤ 0.6% de ventas totales

#### 4. **Productos con Stock Crítico**
- **Objetivo**: **0 productos debajo de stock_mínimo**
- **Alertas**:
  - 🔴 Crítico: stock ≤ stock_minimo (acción inmediata)
  - 🟡 Advertencia: stock ≤ punto_reorden (generar orden de compra)
  - 🟢 Normal: stock > punto_reorden
- **Meta de Disponibilidad**: **95% de productos en stock normal**
- **Query**: 
  ```sql
  SELECT COUNT(*) FROM product_branch pb
  JOIN producto p ON pb.producto_id = p.id
  WHERE pb.stock <= p.stock_minimo
  ```

### Visualizaciones:

1. **Matriz: Ventas por Categoría y Mes**
   - Filas: Categorías de producto
   - Columnas: Meses
   - Valores: Total ventas + heatmap de intensidad
   - Totales: Por fila y columna

2. **Gráfico de Áreas Apiladas: Ventas por Canal en el Tiempo**
   - Eje X: Tiempo (diario/semanal/mensual)
   - Eje Y: Ventas en COP
   - Áreas: Por canal (venta.channel)
   - Línea adicional: % del canal principal

3. **Embudo de Conversión: Desde Cart Events hasta Venta Completada**
   - Etapa 1: Add to Cart (100%) - `cart_event.tipo_evento = 'add_to_cart'`
   - Etapa 2: Checkout iniciado (60-70%) - `tipo_evento = 'checkout_start'`
   - Etapa 3: Payment completado (50-60%) - `payment.estado = 'completed'`
   - Etapa 4: Venta confirmada (48-58%) - `venta.estado = 'ACTIVA'`
   - **Tasa de Conversión Objetivo**: **48-55%**
   - Mostrar tasa de abandono entre etapas

4. **Tabla: Análisis de Devoluciones**
   - Columnas: Producto, Cantidad Vendida, Devoluciones, Tasa %, Motivo Principal, Costo
   - Filtros: Por período, categoría, sucursal
   - Ordenar por: Tasa de devolución DESC
   - Resaltar: Productos con tasa > 3%

---

## 📦 PÁGINA 3: GESTIÓN DE INVENTARIO

### KPIs:

#### 1. **Valor del Inventario**
- **Objetivo de Optimización**: **$35,000,000 - $45,000,000 COP**
  - Justificación: 2.3-3 meses de costo de ventas
  - Evitar: Sobre-stock (capital inmovilizado) y Sub-stock (ventas perdidas)
- **Fórmula**: 
  ```
  Valor Inventario = SUM(product_branch.stock * producto.precio_compra)
  ```
- **Por Sucursal**:
  - Sucursal Principal: 50-60% del inventario
  - Sucursales Secundarias: 20-25% c/u
  - Almacén Central: 10-15%
- **Rotación de Capital**: Meta 35-40 días

#### 2. **Productos Bajo Stock Mínimo**
- **Objetivo Máximo**: **≤ 5% del total de productos** (idealmente 0%)
  - Crítico: ≤ 2% (productos con stock = 0 o < 50% del stock_minimo)
  - Advertencia: 2-5% (productos entre 50-100% del stock_minimo)
- **Query**: 
  ```sql
  SELECT COUNT(DISTINCT pb.producto_id) * 100.0 / 
         (SELECT COUNT(DISTINCT producto_id) FROM product_branch)
  FROM product_branch pb
  JOIN producto p ON pb.producto_id = p.id
  WHERE pb.stock < p.stock_minimo
  ```
- **Meta de Respuesta**: Reabastecimiento en **lead_time_dias** o menos

#### 3. **Productos Próximos a Vencer**
- **Objetivo**:
  - **0 productos vencidos** (fecha_vencimiento < TODAY)
  - **≤ 3% próximos a vencer en 30 días** (fecha_vencimiento BETWEEN TODAY AND TODAY+30)
  - **≤ 8% próximos a vencer en 60 días**
- **Query**: 
  ```sql
  SELECT COUNT(*) FROM lote
  WHERE fecha_vencimiento BETWEEN CURRENT_DATE AND CURRENT_DATE + 30
  AND cantidad_disponible > 0
  ```
- **Acciones Preventivas**:
  - 60 días: Promociones preventivas (descuento 10-15%)
  - 30 días: Promociones agresivas (descuento 25-40%)
  - 15 días: Liquidación (descuento 50-70%)
  - Vencidos: Destrucción/donación
- **Meta de Merma por Vencimiento**: ≤ 0.8% del costo de inventario

#### 4. **Eficiencia de Almacenamiento**
- **Objetivo**: **75-85% de ocupación**
  - < 70%: Sub-utilización (costos fijos altos)
  - > 90%: Sobre-saturación (riesgo operativo)
- **Fórmula**: 
  ```
  (SUM(product_branch.stock) / Capacidad_Maxima_Ubicacion) * 100
  ```
- **Por Tipo de Almacenamiento**:
  - AMBIENTE: 80-85%
  - REFRIGERADO: 70-80% (productos perecederos, alta rotación)
  - CONGELADO: 65-75% (productos especializados)

### Visualizaciones:

1. **Mapa de Calor: Stock por Categoría y Sucursal**
   - Filas: Categorías
   - Columnas: Sucursales (branch.nombre)
   - Color: 
     - Verde oscuro: Stock > 120% del punto_reorden
     - Verde: 100-120% del punto_reorden
     - Amarillo: Stock entre stock_minimo y punto_reorden
     - Naranja: 50-100% del stock_minimo
     - Rojo: < 50% del stock_minimo
   - Valores: Unidades en stock

2. **Tabla Dinámica: Lotes Próximos a Vencer**
   - Columnas: Producto, Lote, Fecha Vencimiento, Días Restantes, Cantidad, Valor (cantidad * precio_compra), Sucursal, Acción Sugerida
   - Filtros: Por sucursal, categoría, días hasta vencimiento
   - Ordenar por: Días restantes ASC
   - Alertas visuales:
     - 🔴 < 15 días
     - 🟡 15-30 días
     - 🔵 31-60 días
   - Incluir: Sugerencia de descuento y margen resultante

3. **Gráfico de Dispersión: Stock vs Rotación**
   - Eje X: Rotación del producto (ventas últimos 90 días / stock promedio)
   - Eje Y: Valor del stock actual (stock * precio_compra)
   - Tamaño burbuja: Margen del producto
   - Color: Categoría
   - Cuadrantes:
     - **Alto Stock + Alta Rotación**: Óptimo (productos estrella)
     - **Bajo Stock + Alta Rotación**: Riesgo de quiebre (reabastecer)
     - **Alto Stock + Baja Rotación**: Sobre-stock (liquidar/promocionar)
     - **Bajo Stock + Baja Rotación**: Evaluar descontinuar

4. **Gráfico de Barras: Movimientos de Inventario por Tipo**
   - Categorías: 
     - COMPRA_ENTRADA
     - VENTA_SALIDA
     - AJUSTE_ENTRADA (devoluciones, correcciones positivas)
     - AJUSTE_SALIDA (mermas, pérdidas, correcciones negativas)
   - Valores: Cantidad y valor monetario
   - Por período: Diario, Semanal, Mensual
   - Incluir: Línea de tendencia y alertas de anomalías

---

## 🏭 PÁGINA 4: COMPRAS Y PROVEEDORES

### KPIs:

#### 1. **Cumplimiento de Proveedores**
- **Objetivo**: **≥ 88% entregas a tiempo** (plazo inicial realista)
  - Meta a 6 meses: 92%
  - Meta a 12 meses: 95%
- **Criterio**: Fecha entrega real ≤ (fecha_compra + lead_time_dias)
- **Fórmula**: 
  ```
  (COUNT(compra WHERE fecha_recepcion <= fecha_compra + lead_time_dias) / 
   COUNT(compra)) * 100
  ```
- **Penalizaciones Sugeridas**:
  - 1-3 días retraso: Advertencia
  - 4-7 días retraso: Descuento 2-5% en próxima orden
  - > 7 días retraso: Evaluar cambio de proveedor
- **Seguimiento**: Por proveedor individual y por categoría de producto

#### 2. **Concentración de Proveedores**
- **Objetivo**: **Máximo 25-30% de compras a un solo proveedor**
  - Diversificación de riesgo
  - Poder de negociación
  - Continuidad operativa
- **Distribución Ideal**:
  - Top 3 proveedores: 50-60% del total
  - Proveedores 4-10: 30-40%
  - Resto: 10-15%
- **Fórmula**: 
  ```
  % Proveedor X = (SUM(compra.total WHERE proveedor_id = X) / 
                    SUM(compra.total)) * 100
  ```
- **Alerta**: Si un proveedor > 35% durante 3 meses consecutivos

#### 3. **Variación de Precios de Compra**
- **Objetivo**: **Variación ≤ 6% mensual** (considerando inflación colombiana ~5-6% anual)
  - Por producto: ≤ 8% mensual
  - Por categoría: ≤ 7% mensual
  - Inflación esperada: ~0.4-0.5% mensual
- **Fórmula**: 
  ```
  Variación % = ((precio_actual - precio_mes_anterior) / precio_mes_anterior) * 100
  ```
- **Alertas**:
  - 🟡 Variación 6-10%: Revisar justificación
  - 🔴 Variación > 10%: Requiere aprobación gerencia
- **Benchmark**: Comparar con IPC (Índice de Precios al Consumidor) Colombia

#### 4. **Días de Inventario (Stock Cover)**
- **Objetivo**: **30-42 días de cobertura**
  - Productos de alta rotación: 20-30 días
  - Productos de rotación media: 35-45 días
  - Productos de baja rotación: 45-60 días
- **Fórmula**: 
  ```
  Días Inventario = (Stock Actual / Consumo Diario Promedio)
  Consumo Diario = SUM(detalle_venta.cantidad últimos 30 días) / 30
  ```
- **Lead Time del Proveedor**: Considerar `producto.lead_time_dias`
- **Punto de Reorden Óptimo**: `consumo_diario * (lead_time_dias + 5 días buffer)`

### Visualizaciones:

1. **Tabla Ranking: Top Proveedores**
   - Columnas: Proveedor, # Órdenes, Volumen Comprado (unidades), Valor Total (COP), % del Total, Entregas a Tiempo %, Calificación
   - Ordenar por: Valor Total DESC
   - Semáforo de concentración:
     - 🟢 < 25%
     - 🟡 25-35%
     - 🔴 > 35%
   - Incluir: # de sucursales del proveedor activas

2. **Gráfico de Líneas: Evolución Precios de Compra - Productos Clave**
   - Eje X: Tiempo (mensual)
   - Eje Y: Precio de compra
   - Series: Top 5-10 productos por volumen de compra
   - Incluir: 
     - Línea de IPC Colombia para comparación
     - Banda de variación aceptable (±6%)
     - Anotaciones de eventos (cambios de proveedor, negociaciones)

3. **Treemap: Distribución de Compras**
   - Nivel 1: Proveedor (tamaño = valor total comprado)
   - Nivel 2: Categoría de productos (dentro de cada proveedor)
   - Color: Calificación del proveedor (escala verde-amarillo-rojo)
   - Tooltip: Nombre, valor, %, # productos, # órdenes

4. **Matriz: Contactos por Sucursal de Proveedor**
   - Filas: Proveedores
   - Columnas: Sucursales del proveedor (branch)
   - Valores: # de contactos (contact_person), estado (activo/inactivo)
   - Color: Disponibilidad de contactos
     - Verde: ≥ 2 contactos activos
     - Amarillo: 1 contacto activo
     - Rojo: Sin contactos activos
   - Incluir: Información de contacto en tooltip

---

## 💵 PÁGINA 5: RENTABILIDAD Y MÁRGENES

### KPIs:

#### 1. **Margen Bruto por Categoría**
- **Objetivos por Categoría** (ajustados a realidad colombiana):
  - **Alimentos Básicos (Granos, Harinas)**: 18-22%
  - **Lácteos y Derivados**: 20-25%
  - **Bebidas (No alcohólicas)**: 25-30%
  - **Bebidas Alcohólicas**: 30-40%
  - **Productos de Limpieza**: 28-35%
  - **Higiene Personal**: 30-38%
  - **Snacks y Confitería**: 35-45%
  - **Productos Empaquetados Importados**: 40-50%
  - **Electrónica/Tecnología**: 12-18%
  - **Ropa/Textil**: 45-55%
- **Fórmula**: 
  ```
  Margen Bruto % = ((SUM(dv.cantidad * dv.precio_unitario) - 
                     SUM(dv.cantidad * p.precio_compra)) / 
                    SUM(dv.cantidad * dv.precio_unitario)) * 100
  GROUP BY p.categoria_id
  ```

#### 2. **Productos con Margen Negativo o Crítico**
- **Objetivo Estricto**: **0 productos con margen negativo**
- **Alerta**: Productos con margen < 10% (excepto estratégicos)
- **Query**: 
  ```sql
  SELECT * FROM producto
  WHERE precio_venta < precio_compra  -- Margen negativo
     OR ((precio_venta - precio_compra) / precio_venta) < 0.10  -- Margen < 10%
  ```
- **Excepciones Permitidas** (productos estratégicos/gancho):
  - Máximo 5 productos con margen < 10%
  - Máximo 2% del volumen de ventas total
  - Requieren aprobación mensual
- **Acciones**:
  - Revisar precio de venta
  - Negociar mejor precio de compra
  - Evaluar descontinuar

#### 3. **ROI por Sucursal**
- **Objetivo**: **≥ 18% anual** (≥ 1.5% mensual)
  - Sucursales establecidas (> 1 año): 20-25% anual
  - Sucursales nuevas (< 1 año): 10-15% anual (fase de inversión)
- **Fórmula**: 
  ```
  ROI % = ((Ingresos - Costos Operativos) / Costos Operativos) * 100
  
  Ingresos = SUM(venta.total WHERE branch_id = X)
  Costos = SUM(compra.total WHERE branch_id = X) + 
           costos_fijos_mensuales + gastos_operativos
  ```
- **Costos Fijos Estimados** (Colombia):
  - Arriendo: $1,500,000 - $3,000,000 COP/mes
  - Servicios: $400,000 - $700,000 COP/mes
  - Nómina: $3,000,000 - $6,000,000 COP/mes
  - Total estimado: $5,000,000 - $10,000,000 COP/mes por sucursal

#### 4. **Análisis Pareto (80/20)**
- **Objetivo**: Validar principio de Pareto en ventas
  - **80% de ventas** provienen del **20% de productos** ✓
  - Si no se cumple: Revisar estrategia de portafolio
- **Fórmula**: 
  ```
  -- Ordenar productos por ventas DESC
  -- Calcular % acumulado
  -- Identificar punto donde % acumulado = 80%
  ```
- **Insights**:
  - Productos Top 20%: Proteger stock, promocionar
  - Productos Bottom 30%: Evaluar descontinuar si margen bajo

### Visualizaciones:

1. **Gráfico de Cascada: Desglose Margen Bruto a Neto**
   - Inicio: Ventas Totales (100%)
   - (-) Costo de Ventas = **Margen Bruto**
   - (-) Descuentos y Promociones
   - (-) Devoluciones
   - (-) Mermas y Pérdidas = **Margen Bruto Ajustado**
   - (-) Gastos Operativos (arriendo, servicios, nómina)
   - (-) Gastos Administrativos
   - (-) Gastos Financieros
   - = **Margen Neto**
   - Mostrar % en cada etapa

2. **Scatter Plot: Precio vs Volumen Vendido**
   - Eje X: Precio de venta unitario (log scale)
   - Eje Y: Volumen vendido (unidades)
   - Tamaño: Margen bruto en COP
   - Color: Categoría
   - Línea de tendencia: Elasticidad precio-demanda
   - Cuadrantes:
     - **Alto Precio + Alto Volumen**: Productos premium exitosos
     - **Bajo Precio + Alto Volumen**: Productos básicos/gancho
     - **Alto Precio + Bajo Volumen**: Evaluar reducir precio o mejorar marketing
     - **Bajo Precio + Bajo Volumen**: Candidatos a descontinuar

3. **Gráfico de Barras Apiladas: Rentabilidad por Sucursal y Categoría**
   - Eje X: Sucursales (branch.nombre)
   - Eje Y: Rentabilidad en COP
   - Apilado: Por categoría de producto
   - Incluir línea de objetivo de rentabilidad
   - Filtro: Por período

4. **Curva de Pareto: Contribución Acumulada por Producto**
   - Eje X: Productos (ordenados por ventas DESC)
   - Eje Y Primario (Barras): Ventas por producto
   - Eje Y Secundario (Línea): % acumulado de ventas
   - Marcar: 
     - Línea vertical en 20% de productos
     - Línea horizontal en 80% de ventas
     - Zona ABC: 
       - A (0-80%): Productos críticos
       - B (80-95%): Productos importantes
       - C (95-100%): Productos opcionales

---

## 👥 PÁGINA 6: COMPORTAMIENTO DEL CLIENTE

### KPIs:

#### 1. **Tasa de Conversión**
- **Objetivo**: **≥ 2.8%** (realista para e-commerce Colombia)
  - Tienda física: 45-60% (visitantes que compran)
  - Online/E-commerce: 2.5-3.5%
  - WhatsApp/Telefónico: 15-25%
- **Fórmula**: 
  ```
  Tasa Conversión = (COUNT(DISTINCT venta.id) / 
                     COUNT(DISTINCT cart_event.usuario_id WHERE tipo_evento = 'add_to_cart')) * 100
  ```
- **Por Canal**:
  - Online Desktop: 3.0-4.0%
  - Online Mobile: 1.8-2.5%
  - App Móvil: 4.0-6.0%
- **Benchmarks Colombia**: E-commerce promedio 2.5%

#### 2. **Productos Más Abandonados en Carrito**
- **Objetivo**: **Reducir abandono 15% trimestral**
- **Top 10 Productos con Mayor Tasa de Abandono**
- **Fórmula**: 
  ```
  Tasa Abandono Producto = 
    (COUNT(cart_event WHERE tipo_evento = 'add_to_cart' AND producto_id = X) - 
     COUNT(detalle_venta WHERE producto_id = X)) / 
    COUNT(cart_event WHERE tipo_evento = 'add_to_cart' AND producto_id = X) * 100
  ```
- **Causas Comunes**:
  - Precio elevado (comparación con competencia)
  - Falta de stock (actualizar en tiempo real)
  - Costos de envío altos
  - Proceso de checkout complejo
- **Meta**: Tasa de abandono promedio < 65%

#### 3. **Frecuencia de Compra**
- **Objetivo**: 
  - **Clientes Nuevos**: 1.2 compras/mes (primeros 3 meses)
  - **Clientes Recurrentes**: 2.5-3.5 compras/mes
  - **Clientes VIP**: ≥ 4 compras/mes
- **Fórmula**: 
  ```
  Frecuencia = COUNT(venta WHERE cliente_id = X) / 
               MONTHS_BETWEEN(MAX(venta.fecha), MIN(venta.fecha))
  ```
- **Segmentación RFM**:
  - **Recency**: Última compra < 30 días
  - **Frequency**: ≥ 3 compras últimos 90 días
  - **Monetary**: Valor promedio > $100,000 COP
- **Crecimiento Esperado**: +12% anual en frecuencia

#### 4. **Clientes Activos**
- **Objetivo**: **Crecimiento neto +8% mensual**
  - Clientes nuevos: +12% mensual
  - Churn rate: < 5% mensual
- **Definición Cliente Activo**: ≥ 1 compra en últimos 90 días
- **Fórmula**: 
  ```
  COUNT(DISTINCT venta.cliente_id 
        WHERE venta.fecha >= CURRENT_DATE - 90)
  ```
- **Segmentos**:
  - Nuevos (< 3 meses): 25-30%
  - Activos (3-12 meses): 40-50%
  - Leales (> 12 meses): 20-35%
- **Reactivación**: Clientes inactivos 90-180 días (campañas de retorno)

### Visualizaciones:

1. **Embudo de Conversión Detallado**
   - **Nivel 1**: Sesiones únicas (100%) - `COUNT(DISTINCT cart_event.usuario_id)`
   - **Nivel 2**: Add to Cart (25-35%) - `tipo_evento = 'add_to_cart'`
   - **Nivel 3**: View Cart (18-25%) - `tipo_evento = 'view_cart'`
   - **Nivel 4**: Checkout Start (12-18%) - `tipo_evento = 'checkout_start'`
   - **Nivel 5**: Payment Info (8-14%) - `payment.estado = 'pending'`
   - **Nivel 6**: Purchase Complete (2.8-3.5%) - `venta.estado = 'ACTIVA'`
   - Mostrar: % de caída entre etapas y tiempo promedio en cada etapa

2. **Heatmap: Actividad por Día y Hora**
   - Eje X: Hora del día (0-23)
   - Eje Y: Día de semana (Lunes-Domingo)
   - Color: Intensidad (# transacciones o valor ventas)
   - Insights:
     - Identificar horarios pico
     - Optimizar staffing
     - Programar mantenimientos en horas valle
     - Campañas promocionales en horas estratégicas

3. **Tabla: Análisis de Abandono por Producto**
   - Columnas: 
     - Producto
     - # Veces Agregado al Carrito
     - # Veces Comprado
     - Tasa de Abandono %
     - Precio Promedio
     - Categoría
     - Motivo Principal (si disponible)
   - Ordenar: Tasa de abandono DESC
   - Resaltar: Productos con abandono > 75% y alto valor
   - Filtros: Categoría, rango de precio, período

4. **Gráfico de Cohortes: Retención de Clientes**
   - Filas: Mes de registro del cliente
   - Columnas: Meses desde registro (0, 1, 2, 3... 12)
   - Valores: % de clientes que realizaron compra ese mes
   - Color: Escala verde (alta retención) a rojo (baja retención)
   - Ejemplo:
     - Cohorte Enero 2024: 
       - Mes 0 (enero): 100%
       - Mes 1 (febrero): 45%
       - Mes 2 (marzo): 32%
       - Mes 3 (abril): 28%
       - ...
   - Meta Retención Mes 3: > 25%
   - Meta Retención Mes 12: > 15%

---

## 🚚 PÁGINA 7: LOGÍSTICA Y ENVÍOS

### KPIs:

#### 1. **Tiempo Promedio de Envío**
- **Objetivo**: **≤ 60 horas** (2.5 días) para Bogotá y ciudades principales
  - Bogotá ciudad: ≤ 24 horas
  - Sabana de Bogotá: ≤ 48 horas
  - Otras ciudades principales: ≤ 72 horas
  - Ciudades intermedias: ≤ 96 horas (4 días)
  - Zonas rurales: ≤ 120 horas (5 días)
- **Fórmula**: 
  ```
  AVG(TIMESTAMPDIFF(HOUR, venta.fecha, shipment.fecha_envio))
  ```
- **Meta de Mejora**: -10% trimestral hasta alcanzar objetivo

#### 2. **Tiempo de Despacho Interno (Processing Time)**
- **Objetivo**: **≤ 18 horas** (mismo día si pedido antes de 2pm)
  - Pedidos antes de 14:00: Despacho mismo día (≤ 8 horas)
  - Pedidos después de 14:00: Despacho día siguiente (≤ 24 horas)
- **Fórmula**: 
  ```
  AVG(TIMESTAMPDIFF(HOUR, venta.fecha, venta.dispatch_date))
  ```
- **Por Volumen de Pedido**:
  - 1-5 items: ≤ 4 horas
  - 6-15 items: ≤ 12 horas
  - > 15 items: ≤ 24 horas
- **Meta**: 85% de pedidos despachados en ≤ 18 horas

#### 3. **Tasa de Entregas a Tiempo (On-Time Delivery)**
- **Objetivo**: **≥ 90%** (SLA inicial)
  - Objetivo a 6 meses: ≥ 93%
  - Objetivo a 12 meses: ≥ 95%
- **Criterio SLA**: 
  ```
  A Tiempo = (shipment.fecha_envio - venta.fecha) <= promised_delivery_time
  ```
- **Promised Delivery Time** (según destino):
  - Bogotá: 24 horas
  - Ciudades principales: 48 horas
  - Resto: 72-96 horas
- **Penalizaciones por Retraso**:
  - 1 día: Descuento 5% o envío gratis próxima compra
  - 2-3 días: Descuento 10%
  - > 3 días: Reembolso parcial o total

#### 4. **Eficiencia por Transportista (Carrier)**
- **KPIs por Carrier**:
  - **On-Time %**: ≥ 92%
  - **Costo por Envío**: Benchmark por zona
  - **Tasa de Incidencias**: ≤ 2% (pérdidas, daños)
  - **Rating Clientes**: ≥ 4.2/5.0
- **Distribución Objetivo**:
  - Carrier Principal: 50-60% (balance costo-calidad)
  - Carrier Secundario: 25-30% (backup y zonas específicas)
  - Carriers Alternativos: 10-20% (casos especiales)
- **Review Trimestral**: Evaluar performance y negociar tarifas

### Visualizaciones:

1. **Gráfico de Líneas: Evolución Tiempos de Envío**
   - Eje X: Tiempo (semanal/mensual)
   - Series:
     - Tiempo Despacho Interno (promedio y mediana)
     - Tiempo en Tránsito (promedio y mediana)
     - Tiempo Total (promedio y mediana)
   - Líneas de objetivo (metas)
   - Banda de variabilidad (percentil 25-75)
   - Anotaciones: Eventos que afectaron tiempos (festivos, paros)

2. **Barras Comparativas: Desempeño por Carrier**
   - Grupos: Carriers (nombre del transportista)
   - Métricas (barras agrupadas):
     - % Entregas a Tiempo
     - Costo Promedio por Envío (COP)
     - Rating Cliente (escala 1-5)
     - % Incidencias
   - Color: 
     - Verde: Cumple objetivo
     - Amarillo: Casi cumple
     - Rojo: No cumple
   - Incluir: # de envíos totales (volumen)

3. **Mapa: Rutas y Destinos Frecuentes**
   - Burbujas: Ciudades/Destinos
   - Tamaño: Volumen de envíos
   - Color: Tiempo promedio de entrega vs SLA
     - Verde: Dentro de SLA
     - Amarillo: +1 día del SLA
     - Rojo: +2 días o más
   - Líneas: Rutas principales (desde sucursales)
   - Filtros: Por carrier, período, valor del pedido

4. **Tabla: Envíos con Retrasos**
   - Columnas:
     - ID Envío
     - Fecha Venta
     - Fecha Despacho
     - Fecha Entrega Prometida
     - Fecha Entrega Real
     - Días de Retraso
     - Carrier
     - Destino
     - Causa
     - Compensación
   - Ordenar: Días de retraso DESC
   - Filtros: Carrier, destino, rango de fechas
   - Incluir: Total compensaciones pagadas en período
   - Resaltar: Envíos con > 3 días de retraso

---

## 📊 MÉTRICAS ADICIONALES SUGERIDAS

### Página Adicional: Análisis Financiero (Opcional)

#### KPIs Financieros:
1. **Flujo de Caja Operativo**: ≥ $8,000,000 COP mensual
2. **Punto de Equilibrio**: ≤ 65% de capacidad de ventas
3. **EBITDA Margin**: ≥ 12%
4. **Working Capital**: 45-60 días
5. **Razón Corriente**: ≥ 1.5 (activos corrientes / pasivos corrientes)

### Página Adicional: Análisis de Producto (Opcional)

#### KPIs de Producto:
1. **Tiempo de Introducción de Nuevos Productos**: ≤ 45 días
2. **Tasa de Éxito de Nuevos Productos**: ≥ 65% (ventas > objetivo en 90 días)
3. **Tasa de Canibalización**: ≤ 15% (nuevo producto afecta ventas de existentes)
4. **SKU Productivos**: ≥ 80% de SKUs con ventas últimos 90 días

---

## 🔧 CONSIDERACIONES TÉCNICAS PARA POWER BI

### Fuentes de Datos:
```sql
-- Conexión a base de datos MySQL/PostgreSQL
Server: [tu_servidor]
Database: merko
Authentication: [credenciales]

-- Tablas principales:
- producto
- venta
- detalle_venta
- compra
- detalle_compra
- product_branch
- lote
- proveedor
- branch
- categoria
- usuario
- cart_event
- shipment
- payment
- movimiento_inventario
```

### Relaciones del Modelo:
1. **producto** ←→ **detalle_venta** (1:N)
2. **producto** ←→ **product_branch** (1:N)
3. **producto** ←→ **lote** (1:N)
4. **venta** ←→ **detalle_venta** (1:N)
5. **branch** ←→ **venta** (N:1)
6. **branch** ←→ **compra** (N:1)
7. **proveedor** ←→ **branch** (1:N)
8. **categoria** ←→ **producto** (1:N)
9. **usuario** ←→ **venta** (1:N)
10. **venta** ←→ **shipment** (1:N)

### Medidas DAX Clave:

```dax
// Ventas Totales
Ventas Totales = 
SUMX(
    detalle_venta,
    detalle_venta[cantidad] * detalle_venta[precio_unitario]
)

// Margen Bruto %
Margen Bruto % = 
DIVIDE(
    [Ventas Totales] - [Costo Total Ventas],
    [Ventas Totales],
    0
) * 100

// Costo Total Ventas
Costo Total Ventas = 
SUMX(
    detalle_venta,
    detalle_venta[cantidad] * RELATED(producto[precio_compra])
)

// Ticket Promedio
Ticket Promedio = 
DIVIDE(
    [Ventas Totales],
    DISTINCTCOUNT(venta[id]),
    0
)

// Rotación Inventario
Rotación Inventario = 
DIVIDE(
    [Costo Total Ventas],
    AVERAGE([Valor Inventario]),
    0
)

// Productos Bajo Stock
Productos Bajo Stock = 
COUNTROWS(
    FILTER(
        product_branch,
        product_branch[stock] <= RELATED(producto[stock_minimo])
    )
)

// Tasa Conversión
Tasa Conversión = 
DIVIDE(
    DISTINCTCOUNT(venta[id]),
    CALCULATE(
        DISTINCTCOUNT(cart_event[usuario_id]),
        cart_event[tipo_evento] = "add_to_cart"
    ),
    0
) * 100

// YoY Growth
YoY Growth = 
VAR CurrentYear = [Ventas Totales]
VAR PreviousYear = 
    CALCULATE(
        [Ventas Totales],
        SAMEPERIODLASTYEAR('Calendar'[Date])
    )
RETURN
    DIVIDE(CurrentYear - PreviousYear, PreviousYear, 0) * 100
```

### Filtros Globales Recomendados:
- **Período**: Selector de fecha (fecha_inicio, fecha_fin)
- **Sucursal**: Multi-select de branches
- **Categoría**: Multi-select de categorías
- **Proveedor**: Multi-select (para páginas de compras)
- **Estado**: Filtro de estados (ACTIVA, PENDIENTE, CANCELADA)

### Actualizaciones:
- **Frecuencia**: Diaria (00:00 AM)
- **Incremental**: Solo últimos 90 días para performance
- **Histórico**: Mantener 24 meses de datos

---

## 🎯 RESUMEN DE OBJETIVOS CLAVE

| KPI | Objetivo | Período |
|-----|----------|---------|
| **Ventas Mensuales** | $15,000,000 COP | Mensual |
| **Margen Bruto Global** | 28-32% | Anual |
| **Cumplimiento Pedidos** | ≥ 92% | Mensual |
| **Rotación Inventario** | 8-10 veces/año | Anual |
| **Ticket Promedio** | $85,000 COP | Mensual |
| **Tasa Conversión** | ≥ 2.8% | Mensual |
| **Productos Stock Crítico** | ≤ 5% | Semanal |
| **Productos Próximos Vencer** | ≤ 3% (30 días) | Semanal |
| **Cumplimiento Proveedores** | ≥ 88% | Mensual |
| **Concentración Proveedor** | ≤ 30% | Trimestral |
| **ROI Sucursal** | ≥ 18% anual | Anual |
| **Tasa Devoluciones** | ≤ 1.8% | Mensual |
| **Tiempo Envío** | ≤ 60 horas | Semanal |
| **On-Time Delivery** | ≥ 90% | Mensual |
| **Crecimiento Clientes Activos** | +8% mensual | Mensual |

---

## 📈 ROADMAP DE IMPLEMENTACIÓN

### Fase 1 (Mes 1): Fundamentos
- ✅ Conectar fuentes de datos
- ✅ Crear modelo de relaciones
- ✅ Implementar Página 1 (Resumen Ejecutivo)
- ✅ Implementar Página 3 (Inventario - crítico)
- ✅ Capacitación equipo básica

### Fase 2 (Mes 2): Expansión
- ✅ Implementar Página 2 (Análisis Ventas)
- ✅ Implementar Página 4 (Compras y Proveedores)
- ✅ Configurar alertas automáticas
- ✅ Optimizar performance

### Fase 3 (Mes 3): Refinamiento
- ✅ Implementar Página 5 (Rentabilidad)
- ✅ Implementar Página 6 (Comportamiento Cliente)
- ✅ Implementar Página 7 (Logística)
- ✅ Ajustar objetivos con datos reales

### Fase 4 (Mes 4+): Mejora Continua
- ✅ Añadir análisis predictivo
- ✅ Integrar más fuentes (redes sociales, competencia)
- ✅ Automatizar reportes ejecutivos
- ✅ Implementar ML para forecasting

---

**Nota Final**: Estos objetivos están calibrados para un negocio de retail multi-sucursal en Colombia en fase de crecimiento. Ajusta según:
- Tamaño real del negocio
- Datos históricos disponibles
- Contexto económico actual
- Capacidad operativa
- Benchmarks del sector específico

**Revisión**: Trimestral para ajustar objetivos según performance real y cambios del mercado.
