package com.libreriajuanin.resenas.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResumenValoracionDTO {

    private Long idProducto;
    private Double promedio;
    private long cantidad;
    private Map<Integer, Long> distribucion = new LinkedHashMap<>();

    public ResumenValoracionDTO() {
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }

    public long getCantidad() {
        return cantidad;
    }

    public void setCantidad(long cantidad) {
        this.cantidad = cantidad;
    }

    public Map<Integer, Long> getDistribucion() {
        return distribucion;
    }

    public void setDistribucion(Map<Integer, Long> distribucion) {
        this.distribucion = distribucion;
    }

}
