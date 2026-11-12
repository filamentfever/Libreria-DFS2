package com.libreriajuanin.admin.dto;

import java.util.ArrayList;
import java.util.List;

public class EstadoStockDTO {

    private long totalProductos;
    private long activos;
    private long agotados;
    private long stockBajo;
    private List<ProductoStockDTO> criticos = new ArrayList<>();

    public EstadoStockDTO() {
    }

    public long getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(long totalProductos) {
        this.totalProductos = totalProductos;
    }

    public long getActivos() {
        return activos;
    }

    public void setActivos(long activos) {
        this.activos = activos;
    }

    public long getAgotados() {
        return agotados;
    }

    public void setAgotados(long agotados) {
        this.agotados = agotados;
    }

    public long getStockBajo() {
        return stockBajo;
    }

    public void setStockBajo(long stockBajo) {
        this.stockBajo = stockBajo;
    }

    public List<ProductoStockDTO> getCriticos() {
        return criticos;
    }

    public void setCriticos(List<ProductoStockDTO> criticos) {
        this.criticos = criticos;
    }

}
