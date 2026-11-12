package com.libreriajuanin.catalogo.controller;

import com.libreriajuanin.catalogo.dto.CategoriaDTO;
import com.libreriajuanin.catalogo.dto.FiltroCatalogo;
import com.libreriajuanin.catalogo.dto.PaginaDTO;
import com.libreriajuanin.catalogo.dto.ProductoDTO;
import com.libreriajuanin.catalogo.dto.ProductoRequestDTO;
import com.libreriajuanin.catalogo.dto.ResultadoImportacionDTO;
import com.libreriajuanin.catalogo.dto.StockRequestDTO;
import com.libreriajuanin.catalogo.service.ProductoService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ---------------------------------------------------------- catálogo público

    @GetMapping
    public PaginaDTO<ProductoDTO> listar(
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "subcategoria", required = false) String subcategoria,
            @RequestParam(name = "q", required = false) String buscar,
            @RequestParam(name = "precioMin", required = false) BigDecimal precioMin,
            @RequestParam(name = "precioMax", required = false) BigDecimal precioMax,
            @RequestParam(name = "marca", required = false) List<String> marcas,
            @RequestParam(name = "color", required = false) List<String> colores,
            @RequestParam(name = "orden", required = false) String orden,
            @RequestParam(name = "pagina", required = false) Integer pagina,
            @RequestParam(name = "tamano", required = false) Integer tamano) {
        FiltroCatalogo f = new FiltroCatalogo();
        f.setCategoria(categoria);
        f.setSubcategoria(subcategoria);
        f.setBuscar(buscar);
        f.setPrecioMin(precioMin);
        f.setPrecioMax(precioMax);
        f.setMarcas(marcas);
        f.setColores(colores);
        f.setOrden(orden);
        f.setPagina(pagina);
        f.setTamano(tamano);
        return productoService.listar(f);
    }

    @GetMapping("/categorias")
    public List<CategoriaDTO> categorias() {
        return productoService.categorias();
    }

    @GetMapping("/sugerencias")
    public List<String> sugerencias(@RequestParam(name = "q") String texto) {
        return productoService.sugerencias(texto);
    }

    @GetMapping("/{id}")
    public ProductoDTO obtener(@PathVariable("id") Long id) {
        return productoService.obtenerPorId(id);
    }

    @GetMapping("/slug/{slug}")
    public ProductoDTO obtenerPorSlug(@PathVariable("slug") String slug) {
        return productoService.obtenerPorSlug(slug);
    }

    @GetMapping("/{id}/relacionados")
    public List<ProductoDTO> relacionados(@PathVariable("id") Long id,
                                          @RequestParam(name = "limite", defaultValue = "4") int limite) {
        return productoService.relacionados(id, limite);
    }

    // ---------------------------------------------------------- panel admin

    @GetMapping("/admin")
    public List<ProductoDTO> buscarInventario(@RequestParam(name = "q", required = false) String texto) {
        return productoService.buscarInventario(texto);
    }

    @GetMapping("/criticos")
    public List<ProductoDTO> criticos() {
        return productoService.criticos();
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ProductoDTO actualizar(@PathVariable("id") Long id, @RequestBody ProductoRequestDTO dto) {
        return productoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ProductoDTO actualizarStock(@PathVariable("id") Long id, @RequestBody StockRequestDTO dto) {
        return productoService.actualizarStock(id, dto.getCantidad());
    }

    /** Lo llama codigoms_pedidos al confirmar una compra. */
    @PostMapping("/{id}/descontar-stock")
    public ProductoDTO descontarStock(@PathVariable("id") Long id, @RequestBody StockRequestDTO dto) {
        return productoService.descontarStock(id, dto.getCantidad());
    }

    /** Cuerpo: el texto del CSV (columnas SKU, Nombre, Precio, y opcionales Stock, Categoria, Marca). */
    @PostMapping(value = "/importar-csv", consumes = "text/plain")
    public ResultadoImportacionDTO importarCsv(@RequestBody String csv) {
        return productoService.importarCsv(csv);
    }
}
