package com.libreriajuanin.catalogo.service;

import com.libreriajuanin.catalogo.dto.CategoriaDTO;
import com.libreriajuanin.catalogo.dto.ErrorImportacionDTO;
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
import com.libreriajuanin.catalogo.util.CategoriaCatalogo;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Catálogo público (HU-11 a HU-34) e inventario del panel admin (HU-64 a HU-69). */
@Service
@Transactional
public class ProductoService {

    /** HU-12: un producto es "Nuevo" si ingresó en los últimos 15 días. */
    public static final int DIAS_PRODUCTO_NUEVO = 15;
    /** HU-67: crítico cuando el stock es menor a 5. */
    public static final int STOCK_CRITICO = 5;
    /** HU-15 */
    public static final int TAMANO_PAGINA = 8;
    /** HU-30 y HU-32 */
    public static final int MIN_CARACTERES_SUGERENCIA = 3;
    public static final int MAX_SUGERENCIAS = 5;
    public static final String MENSAJE_SIN_RESULTADOS =
            "No se encontraron resultados para tu búsqueda. Prueba ajustando los filtros o las palabras clave.";

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // ============================================ catálogo público

    /** HU-11, 15, 16, 23, 24, 29, 31, 33, 34: filtra, ordena y pagina los productos ACTIVOS. */
    @Transactional(readOnly = true)
    public PaginaDTO<ProductoDTO> listar(FiltroCatalogo f) {
        List<Producto> filtrados = new ArrayList<>(productoRepository.findByActivoTrue().stream()
                .filter(p -> coincide(p, f))
                .toList());
        ordenar(filtrados, f.getOrden());

        int tamano = f.getTamano() == null || f.getTamano() < 1 ? TAMANO_PAGINA : f.getTamano();
        int totalPaginas = Math.max(1, (int) Math.ceil(filtrados.size() / (double) tamano));
        int pagina = Math.min(Math.max(1, f.getPagina() == null ? 1 : f.getPagina()), totalPaginas);
        int desde = (pagina - 1) * tamano;
        List<Producto> pagActual = filtrados.subList(Math.min(desde, filtrados.size()), Math.min(desde + tamano, filtrados.size()));

        PaginaDTO<ProductoDTO> respuesta = new PaginaDTO<>();
        respuesta.setItems(pagActual.stream().map(this::aDTO).toList());
        respuesta.setPagina(pagina);
        respuesta.setTamano(tamano);
        respuesta.setTotal(filtrados.size());
        respuesta.setTotalPaginas(totalPaginas);
        respuesta.setHayMas(pagina < totalPaginas);
        respuesta.setSinResultados(filtrados.isEmpty());
        respuesta.setMensaje(filtrados.isEmpty() ? MENSAJE_SIN_RESULTADOS : null);
        return respuesta;
    }

    /** HU-17 y consumo desde carrito/pedidos: devuelve el producto aunque esté inactivo (el llamador valida el stock). */
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    /** HU-17: la ficha pública solo existe para productos activos. */
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorSlug(String slug) {
        return productoRepository.findBySlugAndActivoTrue(slug)
                .map(this::aDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto '" + slug + "' no existe"));
    }

    /** HU-19: misma categoría (primero los de la misma subcategoría), sin el propio producto. */
    @Transactional(readOnly = true)
    public List<ProductoDTO> relacionados(Long id, int limite) {
        Producto base = buscar(id);
        return productoRepository.findByActivoTrue().stream()
                .filter(p -> !p.getId().equals(base.getId()) && base.getCategoria().equals(p.getCategoria()))
                .sorted(Comparator.comparing((Producto p) -> !base.getSubcategoria().equals(p.getSubcategoria())))
                .limit(limite)
                .map(this::aDTO)
                .toList();
    }

    /** HU-30 y HU-32: con 3 o más caracteres devuelve hasta 5 nombres que coinciden. */
    @Transactional(readOnly = true)
    public List<String> sugerencias(String texto) {
        String q = normalizar(texto);
        if (q.length() < MIN_CARACTERES_SUGERENCIA) return List.of();
        List<Producto> coinciden = productoRepository.findByActivoTrue().stream()
                .filter(p -> normalizar(p.getNombre()).contains(q))
                .toList();
        List<String> resultado = new ArrayList<>();
        coinciden.stream().filter(p -> normalizar(p.getNombre()).startsWith(q)).forEach(p -> resultado.add(p.getNombre()));
        coinciden.stream().filter(p -> !normalizar(p.getNombre()).startsWith(q)).forEach(p -> resultado.add(p.getNombre()));
        return resultado.stream().limit(MAX_SUGERENCIAS).toList();
    }

    /** HU-26: árbol de categorías y subcategorías para el menú. */
    public List<CategoriaDTO> categorias() {
        return CategoriaCatalogo.arbol().entrySet().stream()
                .map(e -> new CategoriaDTO(e.getKey(), e.getValue()))
                .toList();
    }

    // ============================================ inventario (admin)

    /** HU-66: busca por nombre o SKU entre TODOS los productos (también inactivos). */
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarInventario(String texto) {
        String q = normalizar(texto);
        return productoRepository.findAll().stream()
                .filter(p -> q.isEmpty() || normalizar(p.getNombre()).contains(q) || normalizar(p.getSku()).contains(q))
                .map(this::aDTO)
                .toList();
    }

    /** HU-67: "Artículos críticos" = stock menor a 5. */
    @Transactional(readOnly = true)
    public List<ProductoDTO> criticos() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getStock() < STOCK_CRITICO)
                .map(this::aDTO)
                .toList();
    }

    /** HU-64: crea un producto; queda en el catálogo si tiene stock. */
    public ProductoDTO crear(ProductoRequestDTO dto) {
        validar(dto, null);
        Producto p = new Producto();
        aplicar(p, dto);
        p.setSlug(slugUnico(dto.getNombre()));
        p.setFechaIngreso(LocalDate.now());
        p.setActivo(true);
        aplicarReglaStock(p);
        return aDTO(productoRepository.save(p));
    }

    /** HU-64: edita un producto existente. */
    public ProductoDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto p = buscar(id);
        validar(dto, id);
        aplicar(p, dto);
        aplicarReglaStock(p);
        return aDTO(productoRepository.save(p));
    }

    /** HU-64 */
    public void eliminar(Long id) {
        Producto p = buscar(id);
        productoRepository.delete(p);
    }

    /** HU-65: fija el stock; el catálogo público lo refleja de inmediato. */
    public ProductoDTO actualizarStock(Long id, Integer stock) {
        if (stock == null || stock < 0) {
            throw new ReglaNegocioException("El stock debe ser un entero mayor o igual a 0");
        }
        Producto p = buscar(id);
        p.setStock(stock);
        aplicarReglaStock(p);
        return aDTO(productoRepository.save(p));
    }

    /** Usado por codigoms_pedidos al confirmar una compra. */
    public ProductoDTO descontarStock(Long id, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new ReglaNegocioException("La cantidad a descontar debe ser al menos 1");
        }
        Producto p = buscar(id);
        if (p.getStock() < cantidad) {
            throw new ReglaNegocioException("Stock insuficiente: solo quedan " + p.getStock() + " unidades de '" + p.getNombre() + "'");
        }
        p.setStock(p.getStock() - cantidad);
        aplicarReglaStock(p);
        return aDTO(productoRepository.save(p));
    }

    /**
     * HU-69: si el stock llega a 0 el producto queda inactivo y desaparece del catálogo;
     * si luego se repone y lo ocultó el sistema, se reactiva solo.
     */
    public void aplicarReglaStock(Producto p) {
        if (p.getStock() <= 0) {
            p.setStock(0);
            p.setActivo(false);
            p.setOcultoPorStock(true);
        } else if (p.isOcultoPorStock()) {
            p.setActivo(true);
            p.setOcultoPorStock(false);
        }
    }

    /**
     * HU-68: lee las columnas SKU, Nombre, Precio (opcionales: Stock, Categoria, Marca) e inserta
     * los SKU nuevos o actualiza los existentes. Acepta separador "," o ";".
     */
    public ResultadoImportacionDTO importarCsv(String texto) {
        ResultadoImportacionDTO res = new ResultadoImportacionDTO();
        List<String> lineas = texto == null ? List.of()
                : texto.replace("\uFEFF", "").lines().filter(l -> !l.isBlank()).toList();
        if (lineas.isEmpty()) {
            res.getErrores().add(new ErrorImportacionDTO(0, "El archivo está vacío."));
            return res;
        }
        String sep = contar(lineas.get(0), ';') > contar(lineas.get(0), ',') ? ";" : ",";
        List<String> cab = separar(lineas.get(0), sep).stream().map(this::normalizar).toList();
        int iSku = cab.indexOf("sku");
        int iNombre = cab.indexOf("nombre");
        int iPrecio = cab.indexOf("precio");
        int iStock = cab.indexOf("stock");
        int iCategoria = cab.indexOf("categoria");
        int iMarca = cab.indexOf("marca");
        if (iSku < 0 || iNombre < 0 || iPrecio < 0) {
            res.getErrores().add(new ErrorImportacionDTO(1, "Faltan columnas obligatorias: SKU, Nombre, Precio."));
            return res;
        }

        for (int n = 1; n < lineas.size(); n++) {
            List<String> campos = separar(lineas.get(n), sep);
            int numeroLinea = n + 1;
            String sku = valor(campos, iSku);
            String nombre = valor(campos, iNombre);
            if (sku.isEmpty() || nombre.isEmpty()) {
                res.getErrores().add(new ErrorImportacionDTO(numeroLinea, "SKU y Nombre son obligatorios."));
                continue;
            }
            BigDecimal precio;
            try {
                precio = new BigDecimal(valor(campos, iPrecio).replaceAll("[$.\\s]", "").replace(",", "."));
            } catch (NumberFormatException e) {
                res.getErrores().add(new ErrorImportacionDTO(numeroLinea, "Precio inválido."));
                continue;
            }
            Integer stock = null;
            String stockTxt = valor(campos, iStock);
            if (!stockTxt.isEmpty()) {
                try {
                    stock = Integer.parseInt(stockTxt);
                } catch (NumberFormatException e) {
                    res.getErrores().add(new ErrorImportacionDTO(numeroLinea, "Stock inválido."));
                    continue;
                }
            }
            String categoria = CategoriaCatalogo.arbol().keySet().stream()
                    .filter(c -> normalizar(c).equals(normalizar(valor(campos, iCategoria))))
                    .findFirst().orElse(null);
            if (!valor(campos, iCategoria).isEmpty() && categoria == null) {
                res.getErrores().add(new ErrorImportacionDTO(numeroLinea, "Categoría desconocida: " + valor(campos, iCategoria) + "."));
                continue;
            }

            Optional<Producto> existente = productoRepository.findBySkuIgnoreCase(sku);
            try {
                if (existente.isPresent()) {
                    Producto p = existente.get();
                    ProductoRequestDTO dto = aRequest(p);
                    dto.setNombre(nombre);
                    dto.setPrecio(precio);
                    if (stock != null) dto.setStock(stock);
                    if (categoria != null && !categoria.equals(p.getCategoria())) {
                        dto.setCategoria(categoria);
                        dto.setSubcategoria(CategoriaCatalogo.arbol().get(categoria).get(0));
                    }
                    if (!valor(campos, iMarca).isEmpty()) dto.setMarca(valor(campos, iMarca));
                    actualizar(p.getId(), dto);
                    res.setActualizados(res.getActualizados() + 1);
                } else {
                    ProductoRequestDTO dto = new ProductoRequestDTO();
                    dto.setSku(sku);
                    dto.setNombre(nombre);
                    dto.setPrecio(precio);
                    dto.setStock(stock == null ? 0 : stock);
                    dto.setCategoria(categoria == null ? "Accesorios" : categoria);
                    dto.setSubcategoria(CategoriaCatalogo.arbol().get(dto.getCategoria()).get(0));
                    dto.setMarca(valor(campos, iMarca));
                    crear(dto);
                    res.setInsertados(res.getInsertados() + 1);
                }
            } catch (ValidacionException e) {
                res.getErrores().add(new ErrorImportacionDTO(numeroLinea, e.getErrores().values().iterator().next()));
            }
        }
        return res;
    }

    // ============================================ conversiones y reglas internas

    /** HU-12: "nuevo" si ingresó hace 15 días o menos. */
    public ProductoDTO aDTO(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(p.getId());
        dto.setSlug(p.getSlug());
        dto.setSku(p.getSku());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecio(p.getPrecio());
        dto.setCategoria(p.getCategoria());
        dto.setSubcategoria(p.getSubcategoria());
        dto.setMarca(p.getMarca());
        dto.setColores(new ArrayList<>(p.getColoresLista()));
        dto.setImagen(p.getImagen());
        dto.setStock(p.getStock());
        dto.setActivo(p.isActivo());
        dto.setFechaIngreso(p.getFechaIngreso());
        dto.setNuevo(esNuevo(p.getFechaIngreso()));
        return dto;
    }

    public boolean esNuevo(LocalDate fechaIngreso) {
        if (fechaIngreso == null) return false;
        long dias = ChronoUnit.DAYS.between(fechaIngreso, LocalDate.now());
        return dias >= 0 && dias <= DIAS_PRODUCTO_NUEVO;
    }

    private boolean coincide(Producto p, FiltroCatalogo f) {
        String texto = normalizar(f.getBuscar());
        return (vacio(f.getCategoria()) || f.getCategoria().equals(p.getCategoria()))
                && (vacio(f.getSubcategoria()) || f.getSubcategoria().equals(p.getSubcategoria()))
                && (texto.isEmpty()
                    || normalizar(p.getNombre()).contains(texto)
                    || normalizar(p.getMarca()).contains(texto)
                    || normalizar(p.getDescripcion()).contains(texto))
                && (f.getPrecioMin() == null || p.getPrecio().compareTo(f.getPrecioMin()) >= 0)
                && (f.getPrecioMax() == null || p.getPrecio().compareTo(f.getPrecioMax()) <= 0)
                && (f.getMarcas() == null || f.getMarcas().isEmpty() || f.getMarcas().contains(p.getMarca()))
                && (f.getColores() == null || f.getColores().isEmpty()
                    || p.getColoresLista().stream().anyMatch(f.getColores()::contains));
    }

    /** HU-16: precio o nombre, ascendente o descendente; sin orden, queda el original. */
    private void ordenar(List<Producto> lista, String orden) {
        if (orden == null) return;
        switch (orden) {
            case "precio-asc" -> lista.sort(Comparator.comparing(Producto::getPrecio));
            case "precio-desc" -> lista.sort(Comparator.comparing(Producto::getPrecio).reversed());
            case "nombre-asc" -> lista.sort(Comparator.comparing(p -> normalizar(p.getNombre())));
            case "nombre-desc" -> lista.sort(Comparator.comparing((Producto p) -> normalizar(p.getNombre())).reversed());
            default -> { }
        }
    }

    private void validar(ProductoRequestDTO d, Long idActual) {
        Map<String, String> e = new LinkedHashMap<>();
        if (d.getNombre() == null || d.getNombre().trim().length() < 2) {
            e.put("nombre", "Ingresa el nombre del producto.");
        }
        if (d.getSku() == null || d.getSku().isBlank()) {
            e.put("sku", "Ingresa el SKU.");
        } else {
            Optional<Producto> otro = productoRepository.findBySkuIgnoreCase(d.getSku().trim());
            if (otro.isPresent() && !otro.get().getId().equals(idActual)) {
                e.put("sku", "Ya existe un producto con ese SKU.");
            }
        }
        if (d.getPrecio() == null || d.getPrecio().signum() <= 0) {
            e.put("precio", "El precio debe ser mayor a 0.");
        }
        if (d.getStock() == null || d.getStock() < 0) {
            e.put("stock", "El stock debe ser un entero mayor o igual a 0.");
        }
        if (!CategoriaCatalogo.esCategoria(d.getCategoria())) {
            e.put("categoria", "Selecciona una categoría válida.");
        } else if (!vacio(d.getSubcategoria()) && !CategoriaCatalogo.esSubcategoria(d.getCategoria(), d.getSubcategoria())) {
            e.put("subcategoria", "La subcategoría no pertenece a la categoría.");
        }
        if (!e.isEmpty()) throw new ValidacionException(e);
    }

    private void aplicar(Producto p, ProductoRequestDTO d) {
        p.setSku(d.getSku().trim());
        p.setNombre(d.getNombre().trim());
        p.setDescripcion(d.getDescripcion() == null ? "" : d.getDescripcion().trim());
        p.setPrecio(d.getPrecio());
        p.setCategoria(d.getCategoria());
        p.setSubcategoria(vacio(d.getSubcategoria()) ? CategoriaCatalogo.arbol().get(d.getCategoria()).get(0) : d.getSubcategoria());
        p.setMarca(d.getMarca() == null ? "" : d.getMarca().trim());
        p.setColoresLista(d.getColores());
        p.setImagen(vacio(d.getImagen()) ? (p.getImagen() == null ? "/img/imagendestaca.jpg" : p.getImagen()) : d.getImagen().trim());
        p.setStock(d.getStock());
    }

    private ProductoRequestDTO aRequest(Producto p) {
        ProductoRequestDTO d = new ProductoRequestDTO();
        d.setSku(p.getSku());
        d.setNombre(p.getNombre());
        d.setDescripcion(p.getDescripcion());
        d.setPrecio(p.getPrecio());
        d.setCategoria(p.getCategoria());
        d.setSubcategoria(p.getSubcategoria());
        d.setMarca(p.getMarca());
        d.setColores(p.getColoresLista());
        d.setImagen(p.getImagen());
        d.setStock(p.getStock());
        return d;
    }

    private String slugUnico(String nombre) {
        String base = normalizar(nombre).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (base.isEmpty()) base = "producto";
        String slug = base;
        int n = 2;
        while (productoRepository.existsBySlug(slug)) {
            slug = base + "-" + n++;
        }
        return slug;
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto con id " + id + " no existe"));
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    /** Minúsculas y sin tildes, para comparar textos. */
    String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    private int contar(String s, char c) {
        return (int) s.chars().filter(ch -> ch == c).count();
    }

    private String valor(List<String> campos, int indice) {
        return indice >= 0 && indice < campos.size() ? campos.get(indice).trim() : "";
    }

    /** Separa una línea CSV respetando las comillas. */
    private List<String> separar(String linea, String sep) {
        List<String> out = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean comillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char ch = linea.charAt(i);
            if (ch == '"') {
                if (comillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else {
                    comillas = !comillas;
                }
            } else if (ch == sep.charAt(0) && !comillas) {
                out.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(ch);
            }
        }
        out.add(actual.toString());
        return out;
    }
}
