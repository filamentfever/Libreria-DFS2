package com.libreriajuanin.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.admin.TestDataFactory;
import com.libreriajuanin.admin.client.CatalogoClient;
import com.libreriajuanin.admin.client.PedidosClient;
import com.libreriajuanin.admin.client.UsuariosClient;
import com.libreriajuanin.admin.dto.DashboardDTO;
import com.libreriajuanin.admin.dto.EstadoStockDTO;
import com.libreriajuanin.admin.dto.PedidoDTO;
import com.libreriajuanin.admin.dto.ProductoVendidoDTO;
import com.libreriajuanin.admin.dto.ReporteGeneralDTO;
import com.libreriajuanin.admin.dto.ReporteResumenDTO;
import com.libreriajuanin.admin.dto.ReporteVentasDTO;
import com.libreriajuanin.admin.exception.ReglaNegocioException;
import com.libreriajuanin.admin.exception.ServicioNoDisponibleException;
import com.libreriajuanin.admin.model.ReporteGenerado;
import com.libreriajuanin.admin.repository.ReporteGeneradoRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** AdminService no tiene datos propios: 1 repositorio + 3 clientes Feign, todos mockeados. */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ReporteGeneradoRepository reporteRepository;
    @Mock
    private PedidosClient pedidosClient;
    @Mock
    private CatalogoClient catalogoClient;
    @Mock
    private UsuariosClient usuariosClient;

    @InjectMocks
    private AdminService adminService;

    private final LocalDate hoy = LocalDate.now();

    /** Dos ventas confirmadas, un pedido cancelado y una transferencia sin comprobante. */
    private List<PedidoDTO> pedidosDeEjemplo() {
        return List.of(
                TestDataFactory.unPedido(1L, "NUEVO", true, hoy, "50000",
                        TestDataFactory.unItem(10L, "Cuaderno Miku", "19990", 2), TestDataFactory.unItem(11L, "Lapiz mina", "10020", 1)),
                TestDataFactory.unPedido(2L, "ENTREGADO", true, hoy.minusDays(1), "30000",
                        TestDataFactory.unItem(10L, "Cuaderno Miku", "19990", 1), TestDataFactory.unItem(12L, "Libro", "10010", 1)),
                TestDataFactory.unPedido(3L, "CANCELADO", true, hoy, "20000",
                        TestDataFactory.unItem(12L, "Libro", "10000", 2)),
                TestDataFactory.unPedido(4L, "PENDIENTE", false, hoy, "10000",
                        TestDataFactory.unItem(11L, "Lapiz mina", "10000", 1)));
    }

    // ------------------------------------------------------------ dashboard

    @Test
    void dashboard_consolidaPedidosCatalogoYUsuarios() {
        when(pedidosClient.listarPedidos()).thenReturn(pedidosDeEjemplo());
        when(catalogoClient.listarInventario()).thenReturn(List.of(
                TestDataFactory.unProducto(10L, "Cuaderno Miku", 12, true),
                TestDataFactory.unProducto(11L, "Lapiz mina", 3, true),
                TestDataFactory.unProducto(12L, "Libro", 0, false)));
        when(usuariosClient.total()).thenReturn(TestDataFactory.totalUsuarios(5));

        DashboardDTO d = adminService.dashboard();

        assertThat(d.getVentasTotales()).isEqualByComparingTo("80000"); // solo ventas confirmadas y no canceladas
        assertThat(d.getCantidadPedidos()).isEqualTo(4L);
        assertThat(d.getPedidosPendientes()).isEqualTo(2L); // NUEVO + PENDIENTE
        assertThat(d.getPedidosPorEstado().get("CANCELADO")).isEqualTo(1L);
        assertThat(d.getUsuariosRegistrados()).isEqualTo(5L);
        assertThat(d.getEstadoStock().getTotalProductos()).isEqualTo(3L);
        assertThat(d.getProductosMasVendidos().get(0).getNombre()).isEqualTo("Cuaderno Miku");
    }

    // ------------------------------------------------------------ ventas

    @Test
    void ventas_excluyeCanceladosYPagosSinConfirmar_yCalculaElTicketPromedio() {
        when(pedidosClient.listarPedidos()).thenReturn(pedidosDeEjemplo());

        ReporteVentasDTO r = adminService.ventas(null, null);

        assertThat(r.getCantidadPedidos()).isEqualTo(2L);
        assertThat(r.getVentasTotales()).isEqualByComparingTo("80000");
        assertThat(r.getTicketPromedio()).isEqualByComparingTo("40000");
        assertThat(r.getVentasPorDia()).hasSize(2);
        assertThat(r.getVentasPorDia().get(0).getFecha()).isEqualTo(hoy.minusDays(1)); // ordenado por fecha
    }

    @Test
    void ventas_filtraPorRangoDeFechasConExtremosIncluidos() {
        when(pedidosClient.listarPedidos()).thenReturn(pedidosDeEjemplo());

        ReporteVentasDTO r = adminService.ventas(hoy, hoy);

        assertThat(r.getCantidadPedidos()).isEqualTo(1L);
        assertThat(r.getVentasTotales()).isEqualByComparingTo("50000");
    }

    @Test
    void ventas_sinPedidos_devuelveCeros() {
        when(pedidosClient.listarPedidos()).thenReturn(List.of());

        ReporteVentasDTO r = adminService.ventas(null, null);

        assertThat(r.getCantidadPedidos()).isZero();
        assertThat(r.getVentasTotales()).isEqualByComparingTo("0");
        assertThat(r.getTicketPromedio()).isEqualByComparingTo("0");
    }

    @Test
    void ventas_rangoInvertido_lanzaExcepcion() {
        assertThatThrownBy(() -> adminService.ventas(hoy, hoy.minusDays(3)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("fecha inicial");
    }

    // ------------------------------------------------------------ más vendidos

    @Test
    void productosMasVendidos_ordenaPorUnidadesYRespetaElLimite() {
        when(pedidosClient.listarPedidos()).thenReturn(pedidosDeEjemplo());

        List<ProductoVendidoDTO> r = adminService.productosMasVendidos(2, null, null);

        assertThat(r).hasSize(2);
        assertThat(r.get(0).getNombre()).isEqualTo("Cuaderno Miku");
        assertThat(r.get(0).getUnidadesVendidas()).isEqualTo(3L); // 2 + 1
        assertThat(r.get(0).getIngresos()).isEqualByComparingTo("59970");
    }

    @Test
    void productosMasVendidos_ignoraLosPedidosCancelados() {
        when(pedidosClient.listarPedidos()).thenReturn(pedidosDeEjemplo());

        List<ProductoVendidoDTO> r = adminService.productosMasVendidos(10, null, null);

        // El Libro vendió 1 unidad en la venta válida; las 2 del pedido cancelado no cuentan
        assertThat(r.stream().filter(p -> p.getNombre().equals("Libro")).findFirst().get().getUnidadesVendidas()).isEqualTo(1L);
    }

    // ------------------------------------------------------------ stock

    @Test
    void estadoStock_clasificaAgotadosStockBajoYCriticos() {
        when(catalogoClient.listarInventario()).thenReturn(List.of(
                TestDataFactory.unProducto(1L, "Con stock", 12, true),
                TestDataFactory.unProducto(2L, "Stock bajo", 3, true),
                TestDataFactory.unProducto(3L, "Justo cinco", 5, true),
                TestDataFactory.unProducto(4L, "Agotado", 0, false)));

        EstadoStockDTO e = adminService.estadoStock();

        assertThat(e.getTotalProductos()).isEqualTo(4L);
        assertThat(e.getActivos()).isEqualTo(3L);
        assertThat(e.getAgotados()).isEqualTo(1L);
        assertThat(e.getStockBajo()).isEqualTo(1L);
        assertThat(e.getCriticos()).hasSize(2); // stock menor a 5: el de 3 y el agotado
    }

    // ------------------------------------------------------------ servicios caídos

    @Test
    void siPedidosNoResponde_lanzaServicioNoDisponible() {
        when(pedidosClient.listarPedidos()).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> adminService.ventas(null, null))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("servicio de pedidos");
    }

    @Test
    void siCatalogoNoResponde_lanzaServicioNoDisponible() {
        when(catalogoClient.listarInventario()).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> adminService.estadoStock())
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("catálogo");
    }

    // ------------------------------------------------------------ reporte general

    @Test
    void reporteGeneral_consolidaYGuardaUnaInstantaneaEnElHistorial() {
        when(pedidosClient.listarPedidos()).thenReturn(pedidosDeEjemplo());
        when(catalogoClient.listarInventario()).thenReturn(List.of(TestDataFactory.unProducto(10L, "Cuaderno Miku", 12, true)));
        when(usuariosClient.total()).thenReturn(TestDataFactory.totalUsuarios(7));
        when(reporteRepository.save(any(ReporteGenerado.class))).thenAnswer(inv -> {
            ReporteGenerado r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReporteGeneralDTO r = adminService.reporteGeneral();

        ArgumentCaptor<ReporteGenerado> captor = ArgumentCaptor.forClass(ReporteGenerado.class);
        verify(reporteRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("GENERAL");
        assertThat(captor.getValue().getVentasTotales()).isEqualByComparingTo("80000");
        assertThat(captor.getValue().getUsuariosRegistrados()).isEqualTo(7L);
        assertThat(r.getReporteId()).isEqualTo(1L);
        assertThat(r.getDashboard().getUsuariosRegistrados()).isEqualTo(7L);
        assertThat(r.getVentasUltimos30Dias().getCantidadPedidos()).isEqualTo(2L);
    }

    @Test
    void historial_devuelveLosReportesGuardados() {
        ReporteGenerado g = new ReporteGenerado();
        g.setId(1L);
        g.setTipo("GENERAL");
        g.setVentasTotales(new BigDecimal("80000"));
        g.setGeneradoEn(LocalDateTime.now());
        when(reporteRepository.findAllByOrderByGeneradoEnDesc()).thenReturn(List.of(g));

        List<ReporteResumenDTO> r = adminService.historial();

        assertThat(r).hasSize(1);
        assertThat(r.get(0).getTipo()).isEqualTo("GENERAL");
    }
}
