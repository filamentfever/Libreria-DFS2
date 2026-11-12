package com.libreriajuanin.admin.dto;

import java.time.LocalDateTime;

public class ReporteGeneralDTO {

    private Long reporteId;
    private LocalDateTime generadoEn;
    private DashboardDTO dashboard;
    private ReporteVentasDTO ventasUltimos30Dias;

    public ReporteGeneralDTO() {
    }

    public Long getReporteId() {
        return reporteId;
    }

    public void setReporteId(Long reporteId) {
        this.reporteId = reporteId;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

    public DashboardDTO getDashboard() {
        return dashboard;
    }

    public void setDashboard(DashboardDTO dashboard) {
        this.dashboard = dashboard;
    }

    public ReporteVentasDTO getVentasUltimos30Dias() {
        return ventasUltimos30Dias;
    }

    public void setVentasUltimos30Dias(ReporteVentasDTO ventasUltimos30Dias) {
        this.ventasUltimos30Dias = ventasUltimos30Dias;
    }

}
