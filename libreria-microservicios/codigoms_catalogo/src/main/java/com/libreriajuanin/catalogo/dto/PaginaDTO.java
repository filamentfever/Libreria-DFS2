package com.libreriajuanin.catalogo.dto;

import java.util.List;

/** Una página de resultados (HU-15) con aviso cuando no hay coincidencias (HU-31). */
public class PaginaDTO<T> {

    private List<T> items;
    private int pagina;
    private int tamano;
    private long total;
    private int totalPaginas;
    private boolean hayMas;
    private boolean sinResultados;
    private String mensaje;

    public PaginaDTO() {
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public boolean isHayMas() {
        return hayMas;
    }

    public void setHayMas(boolean hayMas) {
        this.hayMas = hayMas;
    }

    public boolean isSinResultados() {
        return sinResultados;
    }

    public void setSinResultados(boolean sinResultados) {
        this.sinResultados = sinResultados;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
