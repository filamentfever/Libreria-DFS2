package com.libreriajuanin.admin.service;

import com.libreriajuanin.admin.client.CatalogoClient;
import com.libreriajuanin.admin.client.PedidosClient;
import com.libreriajuanin.admin.client.UsuariosClient;
import com.libreriajuanin.admin.dto.DashboardDTO;
import com.libreriajuanin.admin.dto.EstadoStockDTO;
import com.libreriajuanin.admin.dto.ItemPedidoDTO;
import com.libreriajuanin.admin.dto.PedidoDTO;
import com.libreriajuanin.admin.dto.ProductoDTO;
import com.libreriajuanin.admin.dto.ProductoStockDTO;
import com.libreriajuanin.admin.dto.ProductoVendidoDTO;
import com.libreriajuanin.admin.dto.ReporteGeneralDTO;
import com.libreriajuanin.admin.dto.ReporteResumenDTO;
import com.libreriajuanin.admin.dto.ReporteVentasDTO;
import com.libreriajuanin.admin.dto.TotalUsuariosDTO;
import com.libreriajuanin.admin.dto.VentaDiaDTO;
import com.libreriajuanin.admin.exception.ReglaNegocioException;
import com.libreriajuanin.admin.exception.ServicioNoDisponibleException;
import com.libreriajuanin.admin.model.ReporteGenerado;
import com.libreriajuanin.admin.repository.ReporteGeneradoRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Métricas y reportes del panel admin (adminService.ts). No guarda datos de negocio propios:
 * consolida lo que informan pedidos, catálogo y usuarios (OpenFeign).
 */
@Service
@Transactional
public class AdminService {

    /** HU-67: un producto es crítico con stock menor a 5. */
    public static final int STOCK_CRITICO = 5;
    public static final int TOP_DASHBOARD = 5;
    public static final int DIAS_REPORTE_GENERAL = 30;

    private final ReporteGeneradoRepository reporteRepository;
    private final PedidosClient pedidosClient;
    private final CatalogoClient catalogoClient;
    private final UsuariosClient usuariosClient;

    public AdminService(ReporteGeneradoRepository reporteRepository, PedidosClient pedidosClient,
                        CatalogoClient catalogoClient, UsuariosClient usuariosClient) {
        this.reporteRepository = reporteRepository;
        this.pedidosClient = pedidosClient;
        this.catalogoClient = catalogoClient;
        this.usuariosClient = usuariosClient;
    }

    // ================================================================ dashboard

    /** Ventas totales, pedidos por estado, más vendidos, estado del stock y usuarios registrados. */
    @Transactional(readOnly = true)
    public DashboardDTO dashboard() {
        List<PedidoDTO> pedidos = pedidos();
        List<ProductoDTO> inventario = inventario();
        long usuarios = totalUsuarios();

        DashboardDTO d = new DashboardDTO();
        d.setVentasTotales(sumarVentas(pedidos.stream().filter(this::esVenta).toList()));
        d.setCantidadPedidos(pedidos.size());
        d.setPedidosPendientes(pedidos.stream().filter(p -> "NUEVO".equals(p.getEstado()) || "PENDIENTE".equals(p.getEstado())).count());
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (PedidoDTO p : pedidos) {
            porEstado.merge(p.getEstado(), 1L, Long::sum);
        }
        d.setPedidosPorEstado(porEstado);
        d.setProductosMasVendidos(masVendidos(pedidos, TOP_DASHBOARD, null, null));
        d.setEstadoStock(clasificarStock(inventario));
        d.setUsuariosRegistrados(usuarios);
        d.setGeneradoEn(LocalDateTime.now());
        return d;
    }

    // ================================================================ ventas

    /** Ventas confirmadas (pago aprobado y no canceladas) entre dos fechas, con el detalle por día. */
    @Transactional(readOnly = true)
    public ReporteVentasDTO ventas(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<PedidoDTO> ventas = pedidos().stream()
                .filter(this::esVenta)
                .filter(p -> enRango(p, desde, hasta))
                .toList();

        ReporteVentasDTO r = new ReporteVentasDTO();
        r.setDesde(desde);
        r.setHasta(hasta);
        r.setCantidadPedidos(ventas.size());
        r.setVentasTotales(sumarVentas(ventas));
        r.setTicketPromedio(ventas.isEmpty() ? BigDecimal.ZERO
                : r.getVentasTotales().divide(BigDecimal.valueOf(ventas.size()), 0, RoundingMode.HALF_UP));

        Map<LocalDate, List<PedidoDTO>> porDia = new TreeMap<>();
        for (PedidoDTO p : ventas) {
            porDia.computeIfAbsent(p.getFecha(), f -> new java.util.ArrayList<>()).add(p);
        }
        porDia.forEach((fecha, lista) -> {
            VentaDiaDTO dia = new VentaDiaDTO();
            dia.setFecha(fecha);
            dia.setPedidos(lista.size());
            dia.setTotal(sumarVentas(lista));
            r.getVentasPorDia().add(dia);
        });
        return r;
    }

    // ================================================================ más vendidos

    /** Productos más vendidos por unidades (desempata por ingresos). Ignora pedidos cancelados o sin pago. */
    @Transactional(readOnly = true)
    public List<ProductoVendidoDTO> productosMasVendidos(int limite, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        return masVendidos(pedidos(), limite < 1 ? TOP_DASHBOARD : limite, desde, hasta);
    }

    // ================================================================ stock

    /** Total, activos, agotados y con stock bajo (&lt; 5); lista de productos críticos. */
    @Transactional(readOnly = true)
    public EstadoStockDTO estadoStock() {
        return clasificarStock(inventario());
    }

    // ================================================================ reporte general

    /** Consolida dashboard + ventas de los últimos 30 días y guarda una instantánea en el historial. */
    public ReporteGeneralDTO reporteGeneral() {
        DashboardDTO dashboard = dashboard();
        LocalDate hoy = LocalDate.now();
        ReporteVentasDTO ventas = ventas(hoy.minusDays(DIAS_REPORTE_GENERAL - 1L), hoy);

        ReporteGenerado snapshot = new ReporteGenerado();
        snapshot.setTipo("GENERAL");
        snapshot.setVentasTotales(dashboard.getVentasTotales());
        snapshot.setCantidadPedidos(dashboard.getCantidadPedidos());
        snapshot.setProductosActivos(dashboard.getEstadoStock().getActivos());
        snapshot.setUsuariosRegistrados(dashboard.getUsuariosRegistrados());
        snapshot.setGeneradoEn(dashboard.getGeneradoEn());
        ReporteGenerado guardado = reporteRepository.save(snapshot);

        ReporteGeneralDTO r = new ReporteGeneralDTO();
        r.setReporteId(guardado.getId());
        r.setGeneradoEn(dashboard.getGeneradoEn());
        r.setDashboard(dashboard);
        r.setVentasUltimos30Dias(ventas);
        return r;
    }

    @Transactional(readOnly = true)
    public List<ReporteResumenDTO> historial() {
        return reporteRepository.findAllByOrderByGeneradoEnDesc().stream().map(this::aResumen).toList();
    }

    // ================================================================ cálculos internos

    /** Una venta es un pedido con el pago confirmado que no fue cancelado. */
    boolean esVenta(PedidoDTO p) {
        return p.isPagoConfirmado() && !"CANCELADO".equals(p.getEstado());
    }

    private boolean enRango(PedidoDTO p, LocalDate desde, LocalDate hasta) {
        return (desde == null || !p.getFecha().isBefore(desde)) && (hasta == null || !p.getFecha().isAfter(hasta));
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la final");
        }
    }

    private BigDecimal sumarVentas(List<PedidoDTO> pedidos) {
        return pedidos.stream().map(PedidoDTO::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ProductoVendidoDTO> masVendidos(List<PedidoDTO> pedidos, int limite, LocalDate desde, LocalDate hasta) {
        Map<Long, ProductoVendidoDTO> acumulado = new LinkedHashMap<>();
        for (PedidoDTO p : pedidos) {
            if (!esVenta(p) || !enRango(p, desde, hasta)) continue;
            for (ItemPedidoDTO item : p.getItems()) {
                ProductoVendidoDTO v = acumulado.computeIfAbsent(item.getProductoId(), id -> {
                    ProductoVendidoDTO nuevo = new ProductoVendidoDTO();
                    nuevo.setProductoId(id);
                    nuevo.setNombre(item.getNombreProducto());
                    nuevo.setIngresos(BigDecimal.ZERO);
                    return nuevo;
                });
                v.setUnidadesVendidas(v.getUnidadesVendidas() + item.getCantidad());
                v.setIngresos(v.getIngresos().add(item.getSubtotal()));
            }
        }
        return acumulado.values().stream()
                .sorted(Comparator.comparingLong(ProductoVendidoDTO::getUnidadesVendidas).reversed()
                        .thenComparing(Comparator.comparing(ProductoVendidoDTO::getIngresos).reversed()))
                .limit(limite)
                .toList();
    }

    private EstadoStockDTO clasificarStock(List<ProductoDTO> inventario) {
        EstadoStockDTO e = new EstadoStockDTO();
        e.setTotalProductos(inventario.size());
        e.setActivos(inventario.stream().filter(ProductoDTO::isActivo).count());
        e.setAgotados(inventario.stream().filter(p -> p.getStock() == 0).count());
        e.setStockBajo(inventario.stream().filter(p -> p.getStock() > 0 && p.getStock() < STOCK_CRITICO).count());
        for (ProductoDTO p : inventario) {
            if (p.getStock() < STOCK_CRITICO) {
                ProductoStockDTO c = new ProductoStockDTO();
                c.setId(p.getId());
                c.setNombre(p.getNombre());
                c.setSku(p.getSku());
                c.setStock(p.getStock());
                c.setActivo(p.isActivo());
                e.getCriticos().add(c);
            }
        }
        return e;
    }

    private ReporteResumenDTO aResumen(ReporteGenerado r) {
        ReporteResumenDTO dto = new ReporteResumenDTO();
        dto.setId(r.getId());
        dto.setTipo(r.getTipo());
        dto.setVentasTotales(r.getVentasTotales());
        dto.setCantidadPedidos(r.getCantidadPedidos());
        dto.setProductosActivos(r.getProductosActivos());
        dto.setUsuariosRegistrados(r.getUsuariosRegistrados());
        dto.setGeneradoEn(r.getGeneradoEn());
        return dto;
    }

    // ================================================================ llamadas a otros servicios

    private List<PedidoDTO> pedidos() {
        try {
            List<PedidoDTO> lista = pedidosClient.listarPedidos();
            return lista == null ? List.of() : lista;
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("No se pudo consultar el servicio de pedidos");
        }
    }

    private List<ProductoDTO> inventario() {
        try {
            List<ProductoDTO> lista = catalogoClient.listarInventario();
            return lista == null ? List.of() : lista;
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("No se pudo consultar el servicio de catálogo");
        }
    }

    private long totalUsuarios() {
        try {
            TotalUsuariosDTO t = usuariosClient.total();
            return t == null ? 0 : t.getTotal();
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("No se pudo consultar el servicio de usuarios");
        }
    }
}
