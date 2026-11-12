package com.libreriajuanin.pedidos.service;

import com.libreriajuanin.pedidos.client.CarritoClient;
import com.libreriajuanin.pedidos.client.CatalogoClient;
import com.libreriajuanin.pedidos.client.PagosClient;
import com.libreriajuanin.pedidos.dto.CarritoDTO;
import com.libreriajuanin.pedidos.dto.CheckoutRequestDTO;
import com.libreriajuanin.pedidos.dto.CotizacionEnvioDTO;
import com.libreriajuanin.pedidos.dto.CuponValidacionDTO;
import com.libreriajuanin.pedidos.dto.ItemCarritoDTO;
import com.libreriajuanin.pedidos.dto.ItemPedidoDTO;
import com.libreriajuanin.pedidos.dto.PagoTarjetaRequestDTO;
import com.libreriajuanin.pedidos.dto.PedidoResponseDTO;
import com.libreriajuanin.pedidos.dto.ProductoDTO;
import com.libreriajuanin.pedidos.dto.ResultadoPagoDTO;
import com.libreriajuanin.pedidos.dto.StockRequestDTO;
import com.libreriajuanin.pedidos.dto.TarjetaDTO;
import com.libreriajuanin.pedidos.exception.PagoRechazadoException;
import com.libreriajuanin.pedidos.exception.RecursoNoEncontradoException;
import com.libreriajuanin.pedidos.exception.ReglaNegocioException;
import com.libreriajuanin.pedidos.model.EstadoPedido;
import com.libreriajuanin.pedidos.model.ItemPedido;
import com.libreriajuanin.pedidos.model.Pedido;
import com.libreriajuanin.pedidos.repository.PedidoRepository;
import com.libreriajuanin.pedidos.util.CalculadoraEnvio;
import feign.FeignException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pedidos y checkout (HU-39 a HU-45, HU-52 a HU-63, HU-70 a HU-74).
 * Orquesta tres microservicios: carrito, catálogo y pagos (OpenFeign).
 */
@Service
@Transactional
public class PedidoService {

    public static final String ENTREGA_DOMICILIO = "DOMICILIO";
    public static final String ENTREGA_RETIRO = "RETIRO";
    public static final String PAGO_TARJETA = "TARJETA";
    public static final String PAGO_TRANSFERENCIA = "TRANSFERENCIA";

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal IVA = BigDecimal.valueOf(19);
    private static final BigDecimal UNO_MAS_IVA = BigDecimal.valueOf(119);

    private final PedidoRepository pedidoRepository;
    private final CarritoClient carritoClient;
    private final CatalogoClient catalogoClient;
    private final PagosClient pagosClient;

    public PedidoService(PedidoRepository pedidoRepository, CarritoClient carritoClient,
                         CatalogoClient catalogoClient, PagosClient pagosClient) {
        this.pedidoRepository = pedidoRepository;
        this.carritoClient = carritoClient;
        this.catalogoClient = catalogoClient;
        this.pagosClient = pagosClient;
    }

    // ================================================================ checkout

    /**
     * HU-59: valida carrito, stock, entrega y cupón; cobra (tarjeta) y SOLO si el pago es aprobado
     * crea el pedido, descuenta el stock en el catálogo y vacía el carrito.
     * HU-56: con transferencia el pedido queda PENDIENTE hasta recibir el comprobante.
     */
    public PedidoResponseDTO crearPedido(CheckoutRequestDTO dto) {
        CarritoDTO carrito = consultarCarrito(dto.getCarritoId());
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new ReglaNegocioException("El carrito está vacío");
        }
        String entrega = normalizarMayus(dto.getEntrega());
        String medioPago = normalizarMayus(dto.getMedioPago());
        if (!ENTREGA_DOMICILIO.equals(entrega) && !ENTREGA_RETIRO.equals(entrega)) {
            throw new ReglaNegocioException("La entrega debe ser DOMICILIO o RETIRO");
        }
        if (!PAGO_TARJETA.equals(medioPago) && !PAGO_TRANSFERENCIA.equals(medioPago)) {
            throw new ReglaNegocioException("El medio de pago debe ser TARJETA o TRANSFERENCIA");
        }

        // 1. Stock vigente (HU-38)
        for (ItemCarritoDTO item : carrito.getItems()) {
            ProductoDTO producto = consultarProducto(item.getProductoId());
            if (!producto.isActivo() || producto.getStock() == null || producto.getStock() < item.getCantidad()) {
                throw new ReglaNegocioException("Stock insuficiente para '" + item.getNombreProducto() + "'");
            }
        }

        // 2. Entrega y costo de despacho (HU-40, HU-42, HU-46)
        BigDecimal envio = BigDecimal.ZERO;
        String direccion;
        if (ENTREGA_DOMICILIO.equals(entrega)) {
            if (esVacio(dto.getDireccion())) {
                throw new ReglaNegocioException("Indica la dirección de despacho");
            }
            CotizacionEnvioDTO cotizacion = CalculadoraEnvio.cotizar(dto.getComuna())
                    .orElseThrow(() -> new ReglaNegocioException("Por ahora no tenemos despacho a esa comuna"));
            envio = cotizacion.getCosto();
            direccion = dto.getDireccion().trim();
        } else {
            if (esVacio(dto.getSucursal())) {
                throw new ReglaNegocioException("Selecciona una sucursal para el retiro");
            }
            direccion = dto.getSucursal().trim();
        }

        // 3. Cupón (HU-57)
        int porcentaje = 0;
        String cuponAplicado = null;
        if (!esVacio(dto.getCuponCodigo())) {
            CuponValidacionDTO cupon = pagosClient.validarCupon(dto.getCuponCodigo());
            if (!cupon.isValido()) {
                throw new ReglaNegocioException(cupon.getMensaje());
            }
            porcentaje = cupon.getPorcentaje();
            cuponAplicado = cupon.getCodigo();
        }

        // 4. Montos: el IVA va incluido en el total (HU-63)
        BigDecimal subtotal = carrito.getItems().stream()
                .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuento = subtotal.multiply(BigDecimal.valueOf(porcentaje)).divide(CIEN, 0, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(descuento).add(envio);
        BigDecimal iva = total.multiply(IVA).divide(UNO_MAS_IVA, 0, RoundingMode.HALF_UP);
        BigDecimal neto = total.subtract(iva);

        // 5. Pago (HU-52, HU-54, HU-58): si se rechaza, no se crea nada
        String codigoAutorizacion = null;
        boolean pagoConfirmado = false;
        if (PAGO_TARJETA.equals(medioPago)) {
            codigoAutorizacion = cobrarConTarjeta(dto, total);
            pagoConfirmado = true;
        }

        // 6. Pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(carrito.getUsuario());
        pedido.setFecha(LocalDate.now());
        pedido.setEstado(pagoConfirmado ? EstadoPedido.NUEVO : EstadoPedido.PENDIENTE);
        pedido.setEntrega(entrega);
        pedido.setDireccionDespacho(direccion);
        pedido.setComuna(ENTREGA_DOMICILIO.equals(entrega) ? dto.getComuna() : null);
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setEnvio(envio);
        pedido.setNeto(neto);
        pedido.setIva(iva);
        pedido.setTotal(total);
        pedido.setCupon(cuponAplicado);
        pedido.setMedioPago(medioPago);
        pedido.setPagoConfirmado(pagoConfirmado);
        pedido.setCodigoAutorizacion(codigoAutorizacion);
        for (ItemCarritoDTO i : carrito.getItems()) {
            pedido.getItems().add(new ItemPedido(i.getProductoId(), i.getNombreProducto(), i.getPrecioUnitario(), i.getCantidad(), pedido));
        }
        Pedido guardado = pedidoRepository.save(pedido);
        guardado.setNumero("ORD-" + (1000 + guardado.getId()));
        guardado = pedidoRepository.save(guardado);

        // 7. Efectos sobre los otros microservicios (HU-59, HU-69)
        for (ItemCarritoDTO i : carrito.getItems()) {
            catalogoClient.descontarStock(i.getProductoId(), new StockRequestDTO(i.getCantidad()));
        }
        carritoClient.vaciarCarrito(carrito.getId());
        return aDTO(guardado);
    }

    // ================================================================ consultas

    /** HU-60 y HU-61: el comprobante siempre refleja el estado vigente. */
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorNumero(String numero) {
        return pedidoRepository.findByNumeroIgnoreCase(numero == null ? "" : numero.trim())
                .map(this::aDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("El pedido " + numero + " no existe"));
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    /** HU-10: historial del cliente, del más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorUsuario(String usuario) {
        return pedidoRepository.findByUsuarioOrderByFechaDescIdDesc(usuario).stream().map(this::aDTO).toList();
    }

    /** HU-70, HU-72 y HU-74: filtra por estado y por rango de fechas (extremos incluidos). */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listar(EstadoPedido estado, LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            return List.of();
        }
        return pedidoRepository.findAll().stream()
                .filter(p -> estado == null || p.getEstado() == estado)
                .filter(p -> desde == null || !p.getFecha().isBefore(desde))
                .filter(p -> hasta == null || !p.getFecha().isAfter(hasta))
                .map(this::aDTO)
                .toList();
    }

    /** HU-39, HU-40 y HU-44: costo y plazo de despacho de una comuna. */
    public CotizacionEnvioDTO cotizarEnvio(String comuna) {
        return CalculadoraEnvio.cotizar(comuna)
                .orElseThrow(() -> new RecursoNoEncontradoException("Por ahora no tenemos despacho a esa comuna"));
    }

    // ================================================================ gestión (admin)

    /** HU-70, HU-71 y HU-73: cambia el estado; el cliente lo ve reflejado en su comprobante. */
    public PedidoResponseDTO cambiarEstado(Long id, String estado) {
        EstadoPedido nuevo;
        try {
            nuevo = EstadoPedido.valueOf(normalizarMayus(estado));
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("Estado inválido: " + estado);
        }
        Pedido pedido = buscar(id);
        pedido.setEstado(nuevo);
        return aDTO(pedidoRepository.save(pedido));
    }

    /** HU-56: al subir el comprobante de transferencia, el pedido sale de pausa y pasa a NUEVO. */
    public PedidoResponseDTO adjuntarComprobanteTransferencia(Long id, String archivo) {
        if (esVacio(archivo)) {
            throw new ReglaNegocioException("Adjunta el comprobante de transferencia");
        }
        Pedido pedido = buscar(id);
        if (!PAGO_TRANSFERENCIA.equals(pedido.getMedioPago())) {
            throw new ReglaNegocioException("El pedido no se paga por transferencia");
        }
        pedido.setComprobanteTransferencia(archivo.trim());
        pedido.setPagoConfirmado(true);
        if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
            pedido.setEstado(EstadoPedido.NUEVO);
        }
        return aDTO(pedidoRepository.save(pedido));
    }

    // ================================================================ conversiones

    /** HU-60: los estados internos se muestran al cliente como Pendiente / Listo / Enviado. */
    public String estadoParaCliente(EstadoPedido estado) {
        return switch (estado) {
            case LISTO_PARA_RETIRO -> "Listo";
            case ENVIADO, ENTREGADO -> "Enviado";
            case CANCELADO -> "Cancelado";
            default -> "Pendiente";
        };
    }

    public PedidoResponseDTO aDTO(Pedido p) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(p.getId());
        dto.setNumero(p.getNumero());
        dto.setUsuario(p.getUsuario());
        dto.setFecha(p.getFecha());
        dto.setEstado(p.getEstado().name());
        dto.setEstadoCliente(estadoParaCliente(p.getEstado()));
        dto.setEntrega(p.getEntrega());
        dto.setDireccionDespacho(p.getDireccionDespacho());
        dto.setComuna(p.getComuna());
        dto.setSubtotal(p.getSubtotal());
        dto.setDescuento(p.getDescuento());
        dto.setEnvio(p.getEnvio());
        dto.setNeto(p.getNeto());
        dto.setIva(p.getIva());
        dto.setTotal(p.getTotal());
        dto.setCupon(p.getCupon());
        dto.setMedioPago(p.getMedioPago());
        dto.setPagoConfirmado(p.isPagoConfirmado());
        dto.setCodigoAutorizacion(p.getCodigoAutorizacion());
        for (ItemPedido i : p.getItems()) {
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProductoId(i.getProductoId());
            item.setNombreProducto(i.getNombreProducto());
            item.setPrecioUnitario(i.getPrecioUnitario());
            item.setCantidad(i.getCantidad());
            item.setSubtotal(i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())));
            dto.getItems().add(item);
        }
        return dto;
    }

    // ================================================================ auxiliares

    private String cobrarConTarjeta(CheckoutRequestDTO dto, BigDecimal total) {
        TarjetaDTO t = dto.getTarjeta();
        if (t == null) {
            throw new ReglaNegocioException("Ingresa los datos de la tarjeta");
        }
        PagoTarjetaRequestDTO pago = new PagoTarjetaRequestDTO();
        pago.setTitular(t.getTitular());
        pago.setNumero(t.getNumero());
        pago.setVencimiento(t.getVencimiento());
        pago.setCvv(t.getCvv());
        pago.setMonto(total);
        pago.setReferencia("CARRITO-" + dto.getCarritoId());
        ResultadoPagoDTO resultado = pagosClient.pagarConTarjeta(pago);
        if (!resultado.isAprobado()) {
            throw new PagoRechazadoException(resultado.getMensaje(), resultado.isPuedeReintentar());
        }
        return resultado.getCodigoAutorizacion();
    }

    private CarritoDTO consultarCarrito(Long id) {
        try {
            return carritoClient.obtenerCarrito(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El carrito " + id + " no existe");
        }
    }

    private ProductoDTO consultarProducto(Long id) {
        try {
            return catalogoClient.obtenerProducto(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El producto con id " + id + " no existe en el catalogo");
        }
    }

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El pedido " + id + " no existe"));
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }

    private String normalizarMayus(String s) {
        return s == null ? "" : s.trim().toUpperCase().replace(' ', '_');
    }
}
