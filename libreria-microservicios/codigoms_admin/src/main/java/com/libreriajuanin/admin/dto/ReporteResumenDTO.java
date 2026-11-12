package com.libreriajuanin.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReporteResumenDTO {

    private Long id;
    private String tipo;
    private BigDecimal ventasTotales;
    private long cantidadPedidos;
    private long productosActivos;
    private long usuariosRegistrados;
    private LocalDateTime generadoEn;

    public ReporteResumenDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public long getProductosActivos() {
        return productosActivos;
    }

    public void setProductosActivos(long productosActivos) {
        this.productosActivos = productosActivos;
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
