package com.libreriajuanin.carrito.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.carrito.TestDataFactory;
import com.libreriajuanin.carrito.client.CatalogoClient;
import com.libreriajuanin.carrito.client.UsuariosClient;
import com.libreriajuanin.carrito.dto.CarritoResponseDTO;
import com.libreriajuanin.carrito.dto.ItemCarritoRequestDTO;
import com.libreriajuanin.carrito.dto.ProductoDTO;
import com.libreriajuanin.carrito.exception.ReglaNegocioException;
import com.libreriajuanin.carrito.model.Carrito;
import com.libreriajuanin.carrito.model.ItemCarrito;
import com.libreriajuanin.carrito.repository.CarritoRepository;
import com.libreriajuanin.carrito.repository.ItemCarritoRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas unitarias de CarritoService: 4 dependencias mockeadas (@Mock x 4). */
@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private ItemCarritoRepository itemCarritoRepository;
    @Mock
    private CatalogoClient catalogoClient;
    @Mock
    private UsuariosClient usuariosClient;

    @InjectMocks
    private CarritoService carritoService;

    // ---------------------------------------------------------------- crear

    @Test
    void crear_usuarioValido_guardaCarrito() {
        Carrito carritoGuardado = TestDataFactory.unCarrito();
        when(usuariosClient.buscarPorNombre(anyString())).thenReturn(null);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoGuardado);

        CarritoResponseDTO resultado = carritoService.crear(carritoGuardado.getUsuario());

        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsuario()).isEqualTo(carritoGuardado.getUsuario());
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void crear_usuarioNoExiste_lanzaExcepcion() {
        when(usuariosClient.buscarPorNombre(anyString()))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> carritoService.crear("usuarioInexistente"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no existe en el sistema");

        verify(carritoRepository, never()).save(any());
    }

    // ---------------------------------------------------------- agregarItem

    @Test
    void agregarItem_productoNuevo_agregaItem() {
        Carrito carrito = new Carrito("juan"); // items vacío
        ProductoDTO producto = TestDataFactory.unProductoDTO();
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setProductoId(producto.getId());
        request.setCantidad(2);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerProductoPorId(producto.getId())).thenReturn(producto);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.agregarItem(1L, request);

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().get(0).getProductoId()).isEqualTo(producto.getId());
        assertThat(carrito.getItems().get(0).getCantidad()).isEqualTo(2);
    }

    @Test
    void agregarItem_productoYaEnCarrito_incrementaCantidad() {
        // Carrito con el producto YA agregado (cantidad = 2)
        Carrito carrito = new Carrito("juan");
        ProductoDTO producto = TestDataFactory.unProductoDTO();
        ItemCarrito itemExistente = new ItemCarrito(
                producto.getId(), producto.getNombre(), producto.getPrecio(), 2, carrito);
        carrito.getItems().add(itemExistente);

        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setProductoId(producto.getId());
        request.setCantidad(3); // se quieren agregar 3 más

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerProductoPorId(producto.getId())).thenReturn(producto);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.agregarItem(1L, request);

        // El carrito sigue teniendo UN solo ítem (sin duplicados)
        assertThat(carrito.getItems()).hasSize(1);
        // La cantidad es 2 + 3 = 5
        assertThat(itemExistente.getCantidad()).isEqualTo(5);
    }

    @Test
    void agregarItem_productoNoExiste_lanzaExcepcion() {
        Carrito carrito = new Carrito("juan");
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setProductoId(99L);
        request.setCantidad(1);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerProductoPorId(anyLong()))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> carritoService.agregarItem(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no existe en el catalogo");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void agregarItem_superaElStock_lanzaExcepcionYNoGuarda() {
        Carrito carrito = new Carrito("juan");
        ProductoDTO poco = TestDataFactory.otroProductoDTO(); // stock 3
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setProductoId(poco.getId());
        request.setCantidad(5);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerProductoPorId(poco.getId())).thenReturn(poco);

        assertThatThrownBy(() -> carritoService.agregarItem(1L, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente: solo quedan 3");

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository, never()).save(any());
    }

    @Test
    void agregarItem_sumaMasQueElStock_noIncrementa() {
        Carrito carrito = new Carrito("juan");
        ProductoDTO poco = TestDataFactory.otroProductoDTO(); // stock 3
        ItemCarrito existente = new ItemCarrito(poco.getId(), poco.getNombre(), poco.getPrecio(), 2, carrito);
        carrito.getItems().add(existente);
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setProductoId(poco.getId());
        request.setCantidad(2); // 2 + 2 = 4 > 3

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerProductoPorId(poco.getId())).thenReturn(poco);

        assertThatThrownBy(() -> carritoService.agregarItem(1L, request))
                .isInstanceOf(ReglaNegocioException.class);

        assertThat(existente.getCantidad()).isEqualTo(2); // no cambió
    }

    @Test
    void agregarItem_productoSinStock_lanzaExcepcion() {
        Carrito carrito = new Carrito("juan");
        ProductoDTO agotado = TestDataFactory.unProductoDTO();
        agotado.setStock(0);
        agotado.setActivo(false);
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setProductoId(agotado.getId());
        request.setCantidad(1);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerProductoPorId(agotado.getId())).thenReturn(agotado);

        assertThatThrownBy(() -> carritoService.agregarItem(1L, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no tiene stock disponible");
    }

    @Test
    void agregarItem_cantidadCero_lanzaExcepcion() {
        Carrito carrito = new Carrito("juan");
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setProductoId(10L);
        request.setCantidad(0);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> carritoService.agregarItem(1L, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("al menos 1");
        verify(catalogoClient, never()).obtenerProductoPorId(anyLong());
    }

    // ---------------------------------------------------------- obtenerPorId

    @Test
    void obtenerPorId_existente_retornaCarrito() {
        Carrito carrito = new Carrito("pedro");
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        var resultado = carritoService.obtenerPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsuario()).isEqualTo("pedro");
    }

    // ------------------------------------------------------------ quitarItem

    @Test
    void quitarItem_itemPertenece_eliminaItem() {
        Carrito carrito = new Carrito("juan");
        carrito.setId(1L); // se necesita el id para comparar en la validación
        ProductoDTO producto = TestDataFactory.unProductoDTO();
        ItemCarrito item = new ItemCarrito(producto.getId(), producto.getNombre(), producto.getPrecio(), 2, carrito);
        item.setId(5L);
        carrito.getItems().add(item);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findById(5L)).thenReturn(Optional.of(item));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.quitarItem(1L, 5L);

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }

    @Test
    void quitarItem_itemDeOtroCarrito_lanzaExcepcionYNoGuarda() {
        Carrito carrito = new Carrito("juan");
        carrito.setId(1L);
        Carrito otro = new Carrito("ana");
        otro.setId(2L);
        ProductoDTO producto = TestDataFactory.unProductoDTO();
        ItemCarrito ajeno = new ItemCarrito(producto.getId(), producto.getNombre(), producto.getPrecio(), 1, otro);
        ajeno.setId(5L);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findById(5L)).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> carritoService.quitarItem(1L, 5L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no pertenece al carrito");

        verify(carritoRepository, never()).save(any());
    }

    // ------------------------------------------------------- cambiarCantidad

    @Test
    void cambiarCantidad_dentroDelStock_actualizaLaCantidad() {
        Carrito carrito = new Carrito("juan");
        carrito.setId(1L);
        ProductoDTO producto = TestDataFactory.unProductoDTO();
        ItemCarrito item = new ItemCarrito(producto.getId(), producto.getNombre(), producto.getPrecio(), 2, carrito);
        item.setId(5L);
        carrito.getItems().add(item);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findById(5L)).thenReturn(Optional.of(item));
        when(catalogoClient.obtenerProductoPorId(producto.getId())).thenReturn(producto);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.cambiarCantidad(1L, 5L, 4);

        assertThat(item.getCantidad()).isEqualTo(4);
    }

    // ---------------------------------------------------------------- vaciar

    @Test
    void vaciar_eliminaTodosLosItems() {
        Carrito carrito = new Carrito("ana");
        ProductoDTO producto = TestDataFactory.unProductoDTO();
        carrito.getItems().add(new ItemCarrito(producto.getId(), producto.getNombre(), producto.getPrecio(), 1, carrito));
        carrito.getItems().add(new ItemCarrito(producto.getId() + 1, "Otro producto", new BigDecimal("9900.00"), 2, carrito));

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.vaciar(1L);

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }

    // ------------------------------------------------------------------ aDTO

    @Test
    void aDTO_calculaSubtotalesCantidadTotalYTotal() {
        Carrito carrito = new Carrito("juan");
        carrito.getItems().add(new ItemCarrito(10L, "Cuaderno Miku", new BigDecimal("19990.00"), 2, carrito));
        carrito.getItems().add(new ItemCarrito(11L, "Lapiz mina", new BigDecimal("12500.00"), 1, carrito));

        CarritoResponseDTO dto = carritoService.aDTO(carrito);

        assertThat(dto.getItems()).hasSize(2);
        assertThat(dto.getCantidadTotal()).isEqualTo(3);
        assertThat(dto.getTotal()).isEqualByComparingTo("52480.00");
    }
}
