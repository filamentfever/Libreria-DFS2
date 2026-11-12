package com.libreriajuanin.pedidos.dto;

import java.math.BigDecimal;

public class CotizacionEnvioDTO {

    private String comuna;
    private BigDecimal costo;
    private int diasMin;
    private int diasMax;
    private String mensajeEntrega;

    public CotizacionEnvioDTO() {
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public int getDiasMin() {
        return diasMin;
    }

    public void setDiasMin(int diasMin) {
        this.diasMin = diasMin;
    }

    public int getDiasMax() {
        return diasMax;
    }

    public void setDiasMax(int diasMax) {
        this.diasMax = diasMax;
    }

    public String getMensajeEntrega() {
        return mensajeEntrega;
    }

    public void setMensajeEntrega(String mensajeEntrega) {
        this.mensajeEntrega = mensajeEntrega;
    }

}
