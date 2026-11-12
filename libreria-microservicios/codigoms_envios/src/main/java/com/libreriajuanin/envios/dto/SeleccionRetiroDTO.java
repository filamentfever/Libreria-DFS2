package com.libreriajuanin.envios.dto;

import java.math.BigDecimal;

public class SeleccionRetiroDTO {

    private Long puntoId;
    private String sucursal;
    private BigDecimal costoEnvio;
    private String direccionTemporal;
    private String horario;

    public SeleccionRetiroDTO() {
    }

    public Long getPuntoId() {
        return puntoId;
    }

    public void setPuntoId(Long puntoId) {
        this.puntoId = puntoId;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public String getDireccionTemporal() {
        return direccionTemporal;
    }

    public void setDireccionTemporal(String direccionTemporal) {
        this.direccionTemporal = direccionTemporal;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

}
