package com.libreriajuanin.notificaciones.dto;

public class ResultadoColaDTO {

    private int procesadas;
    private int enviadas;
    private int fallidas;

    public ResultadoColaDTO() {
    }

    public int getProcesadas() {
        return procesadas;
    }

    public void setProcesadas(int procesadas) {
        this.procesadas = procesadas;
    }

    public int getEnviadas() {
        return enviadas;
    }

    public void setEnviadas(int enviadas) {
        this.enviadas = enviadas;
    }

    public int getFallidas() {
        return fallidas;
    }

    public void setFallidas(int fallidas) {
        this.fallidas = fallidas;
    }

}
