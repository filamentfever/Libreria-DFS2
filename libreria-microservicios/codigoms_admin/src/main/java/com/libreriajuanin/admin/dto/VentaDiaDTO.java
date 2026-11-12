package com.libreriajuanin.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VentaDiaDTO {

    private LocalDate fecha;
    private long pedidos;
    private BigDecimal total;

    public VentaDiaDTO() {
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public long getPedidos() {
        return pedidos;
    }

    public void setPedidos(long pedidos) {
        this.pedidos = pedidos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

}
