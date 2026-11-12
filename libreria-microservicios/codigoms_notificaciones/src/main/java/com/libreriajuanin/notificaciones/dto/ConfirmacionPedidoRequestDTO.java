package com.libreriajuanin.notificaciones.dto;

import java.math.BigDecimal;

public class ConfirmacionPedidoRequestDTO {

    private String email;
    private String numeroPedido;
    private BigDecimal total;
    private String detalle;

    public ConfirmacionPedidoRequestDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

}
