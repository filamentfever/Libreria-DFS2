package com.libreriajuanin.carrito.service;

import com.libreriajuanin.carrito.client.CatalogoClient;
import com.libreriajuanin.carrito.client.UsuariosClient;
import com.libreriajuanin.carrito.dto.CarritoResponseDTO;
import com.libreriajuanin.carrito.dto.ItemCarritoRequestDTO;
import com.libreriajuanin.carrito.dto.ItemCarritoResponseDTO;
import com.libreriajuanin.carrito.dto.ProductoDTO;
import com.libreriajuanin.carrito.exception.RecursoNoEncontradoException;
import com.libreriajuanin.carrito.exception.ReglaNegocioException;
import com.libreriajuanin.carrito.model.Carrito;
import com.libreriajuanin.carrito.model.ItemCarrito;
import com.libreriajuanin.carrito.repository.CarritoRepository;
import com.libreriajuanin.carrito.repository.ItemCarritoRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del carrito (HU-18, HU-35, HU-36, HU-37, HU-38).
 * Colaboradores: 2 repositorios + 2 clientes Feign (catálogo y usuarios).
 */
@Service
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final CatalogoClient catalogoClient;
    private final UsuariosClient usuariosClient;

    public CarritoService(CarritoRepository carritoRepository,
                          ItemCarritoRepository itemCarritoRepository,
                          CatalogoClient catalogoClient,
                          UsuariosClient usuariosClient) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.catalogoClient = catalogoClient;
        this.usuariosClient = usuariosClient;
    }

    /** Crea un carrito vacío para un usuario que debe existir en codigoms_usuarios. */
    public CarritoResponseDTO crear(String usuario) {
        try {
            usuariosClient.buscarPorNombre(usuario);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El usuario '" + usuario + "' no existe en el sistema");
        }
        Carrito guardado = carritoRepository.save(new Carrito(usuario));
        return aDTO(guardado);
    }

    @Transactional(readOnly = true)
    public Optional<Carrito> obtenerPorId(Long id) {
        return carritoRepository.findById(id);
    }

    /** HU-36: recupera el carrito de un usuario (el más reciente) para no perderlo al volver. */
    @Transactional(readOnly = true)
    public Optional<Carrito> obtenerPorUsuario(String usuario) {
        return carritoRepository.findByUsuario(usuario).stream()
                .max((a, b) -> a.getFechaCreacion().compareTo(b.getFechaCreacion()));
    }

    /**
     * Lógica más importante: si el producto ya está en el carrito se INCREMENTA la cantidad
     * en lugar de agregar un ítem duplicado (HU-35). Además valida el stock (HU-38).
     */
    public CarritoResponseDTO agregarItem(Long carritoId, ItemCarritoRequestDTO dto) {
        Carrito carrito = buscarCarrito(carritoId);
        validarCantidad(dto.getCantidad());
        ProductoDTO producto = consultarProducto(dto.getProductoId());

        Optional<ItemCarrito> existente = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(dto.getProductoId()))
                .findFirst();
        int cantidadActual = existente.map(ItemCarrito::getCantidad).orElse(0);
        validarStock(producto, cantidadActual + dto.getCantidad());

        if (existente.isPresent()) {
            existente.get().setCantidad(cantidadActual + dto.getCantidad());
        } else {
            carrito.getItems().add(new ItemCarrito(
                    producto.getId(), producto.getNombre(), producto.getPrecio(), dto.getCantidad(), carrito));
        }
        return aDTO(carritoRepository.save(carrito));
    }

    /** HU-35: cambia la cantidad de un ítem (respetando el stock). */
    public CarritoResponseDTO cambiarCantidad(Long carritoId, Long itemId, Integer cantidad) {
        Carrito carrito = buscarCarrito(carritoId);
        validarCantidad(cantidad);
        ItemCarrito item = buscarItemDelCarrito(carrito, itemId);
        validarStock(consultarProducto(item.getProductoId()), cantidad);
        item.setCantidad(cantidad);
        return aDTO(carritoRepository.save(carrito));
    }

    /** HU-35: quita un ítem; solo si pertenece al carrito indicado. */
    public CarritoResponseDTO quitarItem(Long carritoId, Long itemId) {
        Carrito carrito = buscarCarrito(carritoId);
        ItemCarrito item = buscarItemDelCarrito(carrito, itemId);
        carrito.getItems().remove(item);
        return aDTO(carritoRepository.save(carrito));
    }

    /** HU-35 / HU-37: deja el carrito sin ítems. */
    public CarritoResponseDTO vaciar(Long carritoId) {
        Carrito carrito = buscarCarrito(carritoId);
        carrito.getItems().clear();
        return aDTO(carritoRepository.save(carrito));
    }

    /** Convierte la entidad en la respuesta: calcula subtotales, cantidad total y total a pagar. */
    public CarritoResponseDTO aDTO(Carrito carrito) {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(carrito.getId());
        dto.setUsuario(carrito.getUsuario());
        dto.setFechaCreacion(carrito.getFechaCreacion());
        BigDecimal total = BigDecimal.ZERO;
        int cantidadTotal = 0;
        for (ItemCarrito item : carrito.getItems()) {
            ItemCarritoResponseDTO i = new ItemCarritoResponseDTO();
            i.setId(item.getId());
            i.setProductoId(item.getProductoId());
            i.setNombreProducto(item.getNombreProducto());
            i.setPrecioUnitario(item.getPrecioUnitario());
            i.setCantidad(item.getCantidad());
            BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
            i.setSubtotal(subtotal);
            dto.getItems().add(i);
            total = total.add(subtotal);
            cantidadTotal += item.getCantidad();
        }
        dto.setCantidadTotal(cantidadTotal);
        dto.setTotal(total);
        return dto;
    }

    // ---------- auxiliares ----------

    private Carrito buscarCarrito(Long id) {
        return carritoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El carrito " + id + " no existe"));
    }

    private ProductoDTO consultarProducto(Long productoId) {
        try {
            return catalogoClient.obtenerProductoPorId(productoId);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El producto con id " + productoId + " no existe en el catalogo");
        }
    }

    private ItemCarrito buscarItemDelCarrito(Carrito carrito, Long itemId) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El ítem " + itemId + " no existe"));
        if (item.getCarrito() == null || !item.getCarrito().getId().equals(carrito.getId())) {
            throw new ReglaNegocioException("El ítem " + itemId + " no pertenece al carrito " + carrito.getId());
        }
        return item;
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new ReglaNegocioException("La cantidad debe ser al menos 1");
        }
    }

    /** HU-38: no se puede superar el stock disponible del catálogo. */
    private void validarStock(ProductoDTO producto, int cantidadSolicitada) {
        if (!producto.isActivo() || producto.getStock() == null || producto.getStock() <= 0) {
            throw new ReglaNegocioException("El producto '" + producto.getNombre() + "' no tiene stock disponible");
        }
        if (cantidadSolicitada > producto.getStock()) {
            throw new ReglaNegocioException("Stock insuficiente: solo quedan " + producto.getStock() + " unidades disponibles");
        }
    }
}
