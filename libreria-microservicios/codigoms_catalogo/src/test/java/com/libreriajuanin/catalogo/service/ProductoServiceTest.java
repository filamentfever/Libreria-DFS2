package com.libreriajuanin.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.catalogo.TestDataFactory;
import com.libreriajuanin.catalogo.dto.FiltroCatalogo;
import com.libreriajuanin.catalogo.dto.PaginaDTO;
import com.libreriajuanin.catalogo.dto.ProductoDTO;
import com.libreriajuanin.catalogo.dto.ProductoRequestDTO;
import com.libreriajuanin.catalogo.dto.ResultadoImportacionDTO;
import com.libreriajuanin.catalogo.exception.RecursoNoEncontradoException;
import com.libreriajuanin.catalogo.exception.ReglaNegocioException;
import com.libreriajuanin.catalogo.exception.ValidacionException;
import com.libreriajuanin.catalogo.model.Producto;
import com.libreriajuanin.catalogo.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto cuaderno;
    private Producto lapiz;
    private Producto resma;
    private Producto libro;
    private Producto estuche;

    @BeforeEach
    void datos() {
        cuaderno = TestDataFactory.unProducto(1L, "Cuaderno Miku", "Cuadernos", "Universitarios", "Torre", "19990", 12, "Celeste,Turquesa", 5);
        lapiz = TestDataFactory.unProducto(2L, "Lapiz mina", "Lápices", "Grafito", "Faber-Castell", "12500", 3, "Negro,Gris", 40);
        resma = TestDataFactory.unProducto(3L, "Resma de hojas", "Papeles", "Resmas", "Proarte", "8990", 25, "Blanco", 60);
        libro = TestDataFactory.unProducto(4L, "Libro", "Libros", "Literatura", "Editorial Aura", "24000", 8, "", 10);
        estuche = TestDataFactory.unProducto(5L, "Estuche", "Accesorios", "Estuches", "Proarte", "15750", 7, "Rosado,Negro", 90);
    }

    private void catalogoActivo() {
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(cuaderno, lapiz, resma, libro, estuche));
    }

    // ----------------------------------------------------- listar y filtros

    @Test
    void listar_sinFiltros_devuelveTodosLosActivos() {
        catalogoActivo();

        PaginaDTO<ProductoDTO> pagina = productoService.listar(new FiltroCatalogo());

        assertThat(pagina.getItems()).hasSize(5);
        assertThat(pagina.getTotal()).isEqualTo(5);
        assertThat(pagina.isSinResultados()).isFalse();
    }

    @Test
    void listar_filtraPorCategoria() {
        catalogoActivo();
        FiltroCatalogo f = new FiltroCatalogo();
        f.setCategoria("Accesorios");

        PaginaDTO<ProductoDTO> pagina = productoService.listar(f);

        assertThat(pagina.getItems()).hasSize(1);
        assertThat(pagina.getItems().get(0).getNombre()).isEqualTo("Estuche");
    }

    @Test
    void listar_filtraPorSubcategoria() {
        catalogoActivo();
        FiltroCatalogo f = new FiltroCatalogo();
        f.setCategoria("Lápices");
        f.setSubcategoria("Grafito");

        PaginaDTO<ProductoDTO> pagina = productoService.listar(f);

        assertThat(pagina.getItems()).hasSize(1);
        assertThat(pagina.getItems().get(0).getSubcategoria()).isEqualTo("Grafito");
    }

    @Test
    void listar_busquedaIgnoraMayusculasYTildes() {
        catalogoActivo();
        FiltroCatalogo f = new FiltroCatalogo();
        f.setBuscar("LÁPIZ");

        PaginaDTO<ProductoDTO> pagina = productoService.listar(f);

        assertThat(pagina.getItems()).hasSize(1);
        assertThat(pagina.getItems().get(0).getNombre()).isEqualTo("Lapiz mina");
    }

    @Test
    void listar_filtraPorRangoDePrecio() {
        catalogoActivo();
        FiltroCatalogo f = new FiltroCatalogo();
        f.setPrecioMin(new BigDecimal("10000"));
        f.setPrecioMax(new BigDecimal("20000"));

        PaginaDTO<ProductoDTO> pagina = productoService.listar(f);

        assertThat(pagina.getItems()).hasSize(3); // cuaderno, lápiz y estuche
        assertThat(pagina.getItems()).allMatch(p -> p.getPrecio().compareTo(new BigDecimal("10000")) >= 0
                && p.getPrecio().compareTo(new BigDecimal("20000")) <= 0);
    }

    @Test
    void listar_filtraPorMarcaYPorColor() {
        catalogoActivo();
        FiltroCatalogo porMarca = new FiltroCatalogo();
        porMarca.setMarcas(List.of("Proarte"));
        FiltroCatalogo porColor = new FiltroCatalogo();
        porColor.setColores(List.of("Rosado"));

        assertThat(productoService.listar(porMarca).getItems()).hasSize(2); // resma y estuche
        assertThat(productoService.listar(porColor).getItems()).hasSize(1);
    }

    @Test
    void listar_ordenaPorPrecioAscendenteYDescendente() {
        catalogoActivo();
        FiltroCatalogo asc = new FiltroCatalogo();
        asc.setOrden("precio-asc");
        FiltroCatalogo desc = new FiltroCatalogo();
        desc.setOrden("precio-desc");

        List<ProductoDTO> ascendente = productoService.listar(asc).getItems();
        List<ProductoDTO> descendente = productoService.listar(desc).getItems();

        assertThat(ascendente.get(0).getNombre()).isEqualTo("Resma de hojas");
        assertThat(descendente.get(0).getNombre()).isEqualTo("Libro");
    }

    @Test
    void listar_ordenaPorNombre() {
        catalogoActivo();
        FiltroCatalogo f = new FiltroCatalogo();
        f.setOrden("nombre-asc");

        List<ProductoDTO> items = productoService.listar(f).getItems();

        assertThat(items.get(0).getNombre()).isEqualTo("Cuaderno Miku");
        assertThat(items.get(items.size() - 1).getNombre()).isEqualTo("Resma de hojas");
    }

    @Test
    void listar_paginaLosResultados() {
        catalogoActivo();
        FiltroCatalogo f = new FiltroCatalogo();
        f.setTamano(2);
        f.setPagina(2);

        PaginaDTO<ProductoDTO> pagina = productoService.listar(f);

        assertThat(pagina.getItems()).hasSize(2);
        assertThat(pagina.getPagina()).isEqualTo(2);
        assertThat(pagina.getTotalPaginas()).isEqualTo(3);
        assertThat(pagina.isHayMas()).isTrue();
    }

    @Test
    void listar_sinCoincidencias_avisaConMensaje() {
        catalogoActivo();
        FiltroCatalogo f = new FiltroCatalogo();
        f.setBuscar("zzzzzz");

        PaginaDTO<ProductoDTO> pagina = productoService.listar(f);

        assertThat(pagina.isSinResultados()).isTrue();
        assertThat(pagina.getItems()).isEmpty();
        assertThat(pagina.getMensaje()).contains("No se encontraron resultados");
    }

    // ----------------------------------------------------- etiqueta "Nuevo", sugerencias

    @Test
    void aDTO_nuevoSoloSiIngresoHace15DiasOMenos() {
        Producto justo = TestDataFactory.unProducto();
        justo.setFechaIngreso(LocalDate.now().minusDays(15));
        Producto viejo = TestDataFactory.unProducto();
        viejo.setFechaIngreso(LocalDate.now().minusDays(16));

        assertThat(productoService.aDTO(justo).isNuevo()).isTrue();
        assertThat(productoService.aDTO(viejo).isNuevo()).isFalse();
    }

    @Test
    void sugerencias_conMenosDeTresCaracteres_noDevuelveNada() {
        assertThat(productoService.sugerencias("cu")).isEmpty();
    }

    @Test
    void sugerencias_desdeTresCaracteres_devuelveNombresQueCoinciden() {
        catalogoActivo();

        assertThat(productoService.sugerencias("cua")).containsExactly("Cuaderno Miku");
    }

    // ----------------------------------------------------- detalle y relacionados

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    void obtenerPorSlug_productoInactivo_noTieneFichaPublica() {
        when(productoRepository.findBySlugAndActivoTrue("miku")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorSlug("miku"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void relacionados_sonDeLaMismaCategoriaSinElPropioProducto() {
        Producto otroCuaderno = TestDataFactory.unProducto(6L, "Croquera", "Cuadernos", "Croqueras", "Torre", "5000", 5, "", 5);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuaderno));
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(cuaderno, lapiz, otroCuaderno));

        List<ProductoDTO> resultado = productoService.relacionados(1L, 4);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Croquera");
    }

    // ----------------------------------------------------- inventario (admin)

    @Test
    void criticos_sonLosDeStockMenorACinco() {
        Producto justoCinco = TestDataFactory.unProducto(7L, "Cinco", "Papeles", "Resmas", "X", "100", 5, "", 1);
        when(productoRepository.findAll()).thenReturn(List.of(cuaderno, lapiz, justoCinco));

        List<ProductoDTO> criticos = productoService.criticos();

        assertThat(criticos).hasSize(1); // solo el lápiz (stock 3); 5 ya no es crítico
        assertThat(criticos.get(0).getNombre()).isEqualTo("Lapiz mina");
    }

    @Test
    void buscarInventario_porNombreOSku_incluyeInactivos() {
        lapiz.setActivo(false);
        when(productoRepository.findAll()).thenReturn(List.of(cuaderno, lapiz));

        assertThat(productoService.buscarInventario("lapiz")).hasSize(1);
        assertThat(productoService.buscarInventario("sku-1")).hasSize(1);
        assertThat(productoService.buscarInventario("")).hasSize(2);
    }

    @Test
    void actualizarStock_aCero_dejaElProductoInactivo() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuaderno));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO resultado = productoService.actualizarStock(1L, 0);

        assertThat(resultado.getStock()).isZero();
        assertThat(resultado.isActivo()).isFalse();
        assertThat(cuaderno.isOcultoPorStock()).isTrue();
    }

    @Test
    void actualizarStock_alReponer_reactivaElProductoOcultoPorElSistema() {
        cuaderno.setStock(0);
        cuaderno.setActivo(false);
        cuaderno.setOcultoPorStock(true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuaderno));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO resultado = productoService.actualizarStock(1L, 6);

        assertThat(resultado.isActivo()).isTrue();
        assertThat(cuaderno.isOcultoPorStock()).isFalse();
    }

    @Test
    void actualizarStock_negativo_lanzaExcepcion() {
        assertThatThrownBy(() -> productoService.actualizarStock(1L, -3))
                .isInstanceOf(ReglaNegocioException.class);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void descontarStock_restaLaCantidad() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuaderno)); // stock 12
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.descontarStock(1L, 2);

        assertThat(cuaderno.getStock()).isEqualTo(10);
        assertThat(cuaderno.isActivo()).isTrue();
    }

    @Test
    void descontarStock_lasUltimasUnidades_ocultaElProducto() {
        when(productoRepository.findById(2L)).thenReturn(Optional.of(lapiz)); // stock 3
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.descontarStock(2L, 3);

        assertThat(lapiz.getStock()).isZero();
        assertThat(lapiz.isActivo()).isFalse();
    }

    @Test
    void descontarStock_masQueElStock_lanzaExcepcionYNoGuarda() {
        when(productoRepository.findById(2L)).thenReturn(Optional.of(lapiz)); // stock 3

        assertThatThrownBy(() -> productoService.descontarStock(2L, 4))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente");
        verify(productoRepository, never()).save(any());
    }

    // ----------------------------------------------------- crear / actualizar / eliminar

    @Test
    void crear_datosValidos_guardaProductoActivoConSlug() {
        ProductoRequestDTO dto = TestDataFactory.unRequest();
        when(productoRepository.findBySkuIgnoreCase("NEW-100")).thenReturn(Optional.empty());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO resultado = productoService.crear(dto);

        assertThat(resultado.getSlug()).isEqualTo("destacador-neon");
        assertThat(resultado.isActivo()).isTrue();
        assertThat(resultado.isNuevo()).isTrue();
    }

    @Test
    void crear_skuDuplicado_lanzaValidacion() {
        when(productoRepository.findBySkuIgnoreCase("NEW-100")).thenReturn(Optional.of(cuaderno));

        assertThatThrownBy(() -> productoService.crear(TestDataFactory.unRequest()))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("Ya existe un producto con ese SKU");
        verify(productoRepository, never()).save(any());
    }

    @Test
    void crear_precioCeroYCategoriaInvalida_lanzaValidacion() {
        ProductoRequestDTO dto = TestDataFactory.unRequest();
        dto.setPrecio(BigDecimal.ZERO);
        dto.setCategoria("Inexistente");
        when(productoRepository.findBySkuIgnoreCase("NEW-100")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.crear(dto))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("precio")
                .hasMessageContaining("categoria");
    }

    @Test
    void eliminar_productoExistente_loBorra() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuaderno));

        productoService.eliminar(1L);

        verify(productoRepository).delete(cuaderno);
    }

    // ----------------------------------------------------- HU-68 CSV

    @Test
    void importarCsv_insertaActualizaEInformaErroresPorLinea() {
        lapiz.setSku("LAP-002");
        when(productoRepository.findBySkuIgnoreCase("NEW-1")).thenReturn(Optional.empty());
        when(productoRepository.findBySkuIgnoreCase("LAP-002")).thenReturn(Optional.of(lapiz));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(lapiz));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        String csv = String.join("\n",
                "SKU,Nombre,Precio,Stock",
                "NEW-1,Cuaderno nuevo,3500,10",
                "LAP-002,Lapiz mina HB,13000,6",
                "BAD-1,Sin precio,abc,1",
                ",Sin sku,100,1");

        ResultadoImportacionDTO resultado = productoService.importarCsv(csv);

        assertThat(resultado.getInsertados()).isEqualTo(1);
        assertThat(resultado.getActualizados()).isEqualTo(1);
        assertThat(resultado.getErrores()).hasSize(2);
        assertThat(resultado.getErrores().get(0).getLinea()).isEqualTo(4);
        assertThat(resultado.getErrores().get(1).getLinea()).isEqualTo(5);
        assertThat(lapiz.getPrecio()).isEqualByComparingTo("13000");
        assertThat(lapiz.getStock()).isEqualTo(6);
    }

    @Test
    void importarCsv_sinColumnasObligatorias_informaElError() {
        ResultadoImportacionDTO resultado = productoService.importarCsv("A,B\n1,2");

        assertThat(resultado.getErrores()).hasSize(1);
        assertThat(resultado.getErrores().get(0).getMotivo()).contains("Faltan columnas obligatorias");
    }

    @Test
    void importarCsv_aceptaPuntoYComaYPrecioConSignoPeso() {
        when(productoRepository.findBySkuIgnoreCase("X-9")).thenReturn(Optional.empty());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultadoImportacionDTO resultado = productoService.importarCsv("SKU;Nombre;Precio\nX-9;Con punto y coma;$1.500");

        assertThat(resultado.getInsertados()).isEqualTo(1);
        assertThat(resultado.getErrores()).isEmpty();
    }
}
