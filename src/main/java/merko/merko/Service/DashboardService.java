package merko.merko.Service;

import merko.merko.Entity.*;
import merko.merko.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;

    @Autowired
    public DashboardService(VentaRepository ventaRepository,
                            DetalleVentaRepository detalleVentaRepository,
                            ProductoRepository productoRepository,
                            LoteRepository loteRepository,
                            CompraRepository compraRepository,
                            DetalleCompraRepository detalleCompraRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
    }

    public Map<String, Object> kpis() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        logger.info("Calculando KPIs para fecha: {}, inicio mes: {}", hoy, inicioMes);

        Double totalHoy = ventaRepository.sumTotalBetweenAndEstado(hoy, hoy, EstadoVenta.ACTIVA);
        Double totalMes = ventaRepository.sumTotalBetweenAndEstado(inicioMes, hoy, EstadoVenta.ACTIVA);
        long ventasHoy = ventaRepository.countByFechaBetweenAndEstado(hoy, hoy, EstadoVenta.ACTIVA);
        double ticketPromedio = ventasHoy == 0 ? 0 : (totalHoy != null ? totalHoy : 0) / ventasHoy;
        long productosActivos = productoRepository.countByEstado(EstadoProducto.ACTIVO);

    Double comprasMes = compraRepository.sumTotalBetween(inicioMes, hoy);

    logger.info("KPIs calculados - totalHoy: {}, totalMes: {}, ventasHoy: {}, ticketPromedio: {}, productosActivos: {}, comprasMes: {}", 
            totalHoy, totalMes, ventasHoy, ticketPromedio, productosActivos, comprasMes);

        Map<String, Object> m = new HashMap<>();
        m.put("totalHoy", Optional.ofNullable(totalHoy).orElse(0d));
        m.put("totalMes", Optional.ofNullable(totalMes).orElse(0d));
        m.put("ventasHoy", ventasHoy);
        m.put("ticketPromedio", ticketPromedio);
        m.put("productosActivos", productosActivos);
        m.put("comprasMes", Optional.ofNullable(comprasMes).orElse(0d));
        return m;
    }

    public List<Map<String, Object>> ventasDiarias(int dias) {
        LocalDate fin = LocalDate.now();
        LocalDate inicio = fin.minusDays(dias - 1L);
        List<Object[]> rows = ventaRepository.dailyTotalsBetween(inicio, fin, EstadoVenta.ACTIVA);
        Map<LocalDate, Double> map = new HashMap<>();
        for (Object[] r : rows) {
            LocalDate fecha = (LocalDate) r[0];
            Double total = (Double) r[1];
            map.put(fecha, Optional.ofNullable(total).orElse(0d));
        }
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        List<Map<String, Object>> out = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            Map<String, Object> item = new HashMap<>();
            item.put("fecha", d.format(fmt));
            item.put("total", map.getOrDefault(d, 0d));
            out.add(item);
        }
        return out;
    }

    public List<Map<String, Object>> topProductos(int dias, int n) {
        LocalDate fin = LocalDate.now();
        LocalDate inicio = fin.minusDays(dias - 1L);
        List<Object[]> rows = detalleVentaRepository.topProductosPorCantidad(inicio, fin, EstadoVenta.ACTIVA);
        return rows.stream().limit(n).map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productoId", r[0]);
            m.put("nombre", r[1]);
            Number qty = (Number) r[2];
            m.put("cantidad", qty == null ? 0 : qty.intValue());
            return m;
        }).collect(Collectors.toList());
    }

    // Compras
    public List<Map<String, Object>> comprasDiarias(int dias) {
        LocalDate fin = LocalDate.now();
        LocalDate inicio = fin.minusDays(dias - 1L);
        logger.info("Consultando compras diarias desde {} hasta {}", inicio, fin);
        List<Object[]> rows = compraRepository.dailyTotalsBetween(inicio, fin);
        logger.info("Compras diarias encontradas: {} filas", rows.size());
        Map<LocalDate, Double> map = new HashMap<>();
        for (Object[] r : rows) {
            LocalDate fecha = (LocalDate) r[0];
            Double total = (Double) r[1];
            logger.debug("Compra en fecha {} con total {}", fecha, total);
            map.put(fecha, Optional.ofNullable(total).orElse(0d));
        }
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        List<Map<String, Object>> out = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            Map<String, Object> item = new HashMap<>();
            item.put("fecha", d.format(fmt));
            item.put("total", map.getOrDefault(d, 0d));
            out.add(item);
        }
        logger.info("Resultado comprasDiarias: {} días", out.size());
        return out;
    }

    public List<Map<String, Object>> topProductosCompras(int dias, int n) {
        LocalDate fin = LocalDate.now();
        LocalDate inicio = fin.minusDays(dias - 1L);
        logger.info("Consultando top productos comprados desde {} hasta {}", inicio, fin);
        List<Object[]> rows = detalleCompraRepository.topProductosCompradosPorCantidad(inicio, fin);
        logger.info("Top productos comprados encontrados: {} productos", rows.size());
        return rows.stream().limit(n).map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productoId", r[0]);
            m.put("nombre", r[1]);
            Number qty = (Number) r[2];
            m.put("cantidad", qty == null ? 0 : qty.intValue());
            logger.debug("Producto: {} - Cantidad: {}", r[1], qty);
            return m;
        }).collect(Collectors.toList());
    }

    public List<Producto> stockCritico(int limit) {
        List<Producto> result = productoRepository.findStockCritico(PageRequest.of(0, Math.max(limit, 1)));
        logger.info("Stock crítico encontrado: {} productos", result.size());
        return result;
    }

    public List<Lote> proximosVencimientos(int dias, int limit) {
        LocalDate inicio = LocalDate.now();
        LocalDate fin = inicio.plusDays(dias);
        List<Lote> list = loteRepository.findExpiringBetween(inicio, fin, EstadoLote.ACTIVO);
        logger.info("Lotes próximos a vencer en {} días: {}", dias, list.size());
        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }
}
