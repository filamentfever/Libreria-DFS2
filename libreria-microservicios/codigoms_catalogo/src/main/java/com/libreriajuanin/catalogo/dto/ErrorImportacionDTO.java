package com.libreriajuanin.catalogo.dto;

public class ErrorImportacionDTO {

    private int linea;
    private String motivo;

    public ErrorImportacionDTO() {
    }

    public ErrorImportacionDTO(int linea, String motivo) {
        this.linea = linea;
        this.motivo = motivo;
    }

    public int getLinea() {
        return linea;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

}
