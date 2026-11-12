package com.libreriajuanin.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteVentasDTO {

    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal ventasTotales;
    private long cantidadPedidos;
    private BigDecimal ticketPromedio;
    private List<VentaDiaDTO> ventasPorDia = new ArrayList<>();

    public ReporteVentasDTO() {
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
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

    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(BigDecimal ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }

    public List<VentaDiaDTO> getVentasPorDia() {
        return ventasPorDia;
    }

    public void setVentasPorDia(List<VentaDiaDTO> ventasPorDia) {
        this.ventasPorDia = ventasPorDia;
    }

}
