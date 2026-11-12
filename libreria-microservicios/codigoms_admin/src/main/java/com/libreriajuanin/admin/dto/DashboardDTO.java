package com.libreriajuanin.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDTO {

    private BigDecimal ventasTotales;
    private long cantidadPedidos;
    private long pedidosPendientes;
    private Map<String, Long> pedidosPorEstado = new LinkedHashMap<>();
    private List<ProductoVendidoDTO> productosMasVendidos = new ArrayList<>();
    private EstadoStockDTO estadoStock;
    private long usuariosRegistrados;
    private LocalDateTime generadoEn;

    public DashboardDTO() {
    }

    public BigDecimal getVentasTotales() {
        return ventasTotales;
    }

    public void setVentasTotales(BigDecimal ventasTotales) {
        this.ventasTotales = ventasTotales;
    }

    public long getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(long cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }

    public long getPedidosPendientes() {
        return pedidosPendientes;
    }

    public void setPedidosPendientes(long pedidosPendientes) {
        this.pedidosPendientes = pedidosPendientes;
    }

    public Map<String, Long> getPedidosPorEstado() {
        return pedidosPorEstado;
    }

    public void setPedidosPorEstado(Map<String, Long> pedidosPorEstado) {
        this.pedidosPorEstado = pedidosPorEstado;
    }

    public List<ProductoVendidoDTO> getProductosMasVendidos() {
        return productosMasVendidos;
    }

    public void setProductosMasVendidos(List<ProductoVendidoDTO> productosMasVendidos) {
        this.productosMasVendidos = productosMasVendidos;
    }

    public EstadoStockDTO getEstadoStock() {
        return estadoStock;
    }

    public void setEstadoStock(EstadoStockDTO estadoStock) {
        this.estadoStock = estadoStock;
    }

    public long getUsuariosRegistrados() {
        return usuariosRegistrados;
    }

    public void setUsuariosRegistrados(long usuariosRegistrados) {
        this.usuariosRegistrados = usuariosRegistrados;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

}
