package com.libreriajuanin.admin.controller;

import com.libreriajuanin.admin.dto.DashboardDTO;
import com.libreriajuanin.admin.dto.EstadoStockDTO;
import com.libreriajuanin.admin.dto.ProductoVendidoDTO;
import com.libreriajuanin.admin.dto.ReporteGeneralDTO;
import com.libreriajuanin.admin.dto.ReporteResumenDTO;
import com.libreriajuanin.admin.dto.ReporteVentasDTO;
import com.libreriajuanin.admin.service.AdminService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public DashboardDTO dashboard() {
        return adminService.dashboard();
    }

    /** GET /api/admin/ventas?desde=2026-03-01&hasta=2026-03-31 (fechas opcionales) */
    @GetMapping("/ventas")
    public ReporteVentasDTO ventas(
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return adminService.ventas(desde, hasta);
    }

    @GetMapping("/productos-mas-vendidos")
    public List<ProductoVendidoDTO> masVendidos(
            @RequestParam(name = "limite", defaultValue = "10") int limite,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return adminService.productosMasVendidos(limite, desde, hasta);
    }

    @GetMapping("/stock")
    public EstadoStockDTO stock() {
        return adminService.estadoStock();
    }

    /** Reporte general consolidado; además queda guardado en el historial. */
    @GetMapping("/reporte-general")
    public ReporteGeneralDTO reporteGeneral() {
        return adminService.reporteGeneral();
    }

    @GetMapping("/reportes/historial")
    public List<ReporteResumenDTO> historial() {
        return adminService.historial();
    }
}
