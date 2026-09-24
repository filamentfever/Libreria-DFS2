/* =========================================================
   Librería Juanin — Lógica del carrito de compras
   Compartida por todas las páginas del sitio.
   Persistencia: localStorage (clave: carritoJuanin)
   ========================================================= */

(function (window) {
  "use strict";

  const CLAVE_CARRITO = "carritoJuanin";

  function obtenerCarrito() {
    try {
      const datos = localStorage.getItem(CLAVE_CARRITO);
      return datos ? JSON.parse(datos) : [];
    } catch (e) {
      return [];
    }
  }

  function guardarCarrito(carrito) {
    localStorage.setItem(CLAVE_CARRITO, JSON.stringify(carrito));
    actualizarContadoresHeader();
    // Avisa a otras partes de la misma página (si las hay) que el carrito cambió
    document.dispatchEvent(new CustomEvent("carrito:actualizado", { detail: carrito }));
  }

  function formatoPrecio(valor) {
    return "$" + Number(valor).toLocaleString("es-CL");
  }

  function agregarAlCarrito(producto) {
    // producto: { id, nombre, precio, imagen, cantidad }
    const carrito = obtenerCarrito();
    const cantidadAAgregar = producto.cantidad && producto.cantidad > 0 ? producto.cantidad : 1;
    const existente = carrito.find(function (item) { return item.id === producto.id; });

    if (existente) {
      existente.cantidad += cantidadAAgregar;
    } else {
      carrito.push({
        id: producto.id,
        nombre: producto.nombre,
        precio: producto.precio,
        imagen: producto.imagen,
        cantidad: cantidadAAgregar
      });
    }

    guardarCarrito(carrito);
    return carrito;
  }

  function cambiarCantidad(idProducto, delta) {
    const carrito = obtenerCarrito();
    const item = carrito.find(function (i) { return i.id === idProducto; });
    if (!item) return carrito;

    item.cantidad += delta;

    if (item.cantidad <= 0) {
      return eliminarDelCarrito(idProducto);
    }

    guardarCarrito(carrito);
    return carrito;
  }

  function establecerCantidad(idProducto, valor) {
    const cantidad = parseInt(valor, 10);
    const carrito = obtenerCarrito();
    const item = carrito.find(function (i) { return i.id === idProducto; });
    if (!item) return carrito;

    if (isNaN(cantidad) || cantidad <= 0) {
      return eliminarDelCarrito(idProducto);
    }

    item.cantidad = cantidad;
    guardarCarrito(carrito);
    return carrito;
  }

  function eliminarDelCarrito(idProducto) {
    let carrito = obtenerCarrito();
    carrito = carrito.filter(function (item) { return item.id !== idProducto; });
    guardarCarrito(carrito);
    return carrito;
  }

  function vaciarCarrito() {
    guardarCarrito([]);
    return [];
  }

  function calcularTotales(carrito) {
    carrito = carrito || obtenerCarrito();
    let cantidadTotal = 0;
    let totalPagar = 0;
    carrito.forEach(function (item) {
      cantidadTotal += item.cantidad;
      totalPagar += item.cantidad * item.precio;
    });
    return { cantidadTotal: cantidadTotal, totalPagar: totalPagar };
  }

  // Actualiza cualquier indicador "Carrito (N)" presente en el header de la página actual
  function actualizarContadoresHeader() {
    const totales = calcularTotales();
    document.querySelectorAll("[data-carrito-contador]").forEach(function (el) {
      el.textContent = totales.cantidadTotal;
    });
  }

  // Conecta automáticamente cualquier botón con la clase .btn-agregar-carrito
  // Lee los datos del producto desde sus atributos data-*
  function conectarBotonesAgregar(contenedor) {
    const raiz = contenedor || document;
    raiz.querySelectorAll(".btn-agregar-carrito").forEach(function (boton) {
      if (boton.dataset.carritoConectado === "1") return; // evita doble binding
      boton.dataset.carritoConectado = "1";

      boton.addEventListener("click", function (evento) {
        evento.preventDefault();

        const id = boton.getAttribute("data-id");
        const nombre = boton.getAttribute("data-nombre");
        const precio = parseInt(boton.getAttribute("data-precio"), 10) || 0;
        const imagen = boton.getAttribute("data-imagen");

        let cantidad = 1;
        const inputCantidadId = boton.getAttribute("data-cantidad-input");
        if (inputCantidadId) {
          const inputCantidad = document.getElementById(inputCantidadId);
          if (inputCantidad) {
            cantidad = parseInt(inputCantidad.value, 10);
            if (isNaN(cantidad) || cantidad < 1) cantidad = 1;
          }
        }

        agregarAlCarrito({ id: id, nombre: nombre, precio: precio, imagen: imagen, cantidad: cantidad });

        // Feedback visual breve en el botón
        const textoOriginal = boton.textContent;
        boton.textContent = "¡Agregado!";
        boton.disabled = true;
        setTimeout(function () {
          boton.textContent = textoOriginal;
          boton.disabled = false;
        }, 900);
      });
    });
  }

  function inicializar() {
    actualizarContadoresHeader();
    conectarBotonesAgregar(document);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", inicializar);
  } else {
    inicializar();
  }

  // API pública del carrito, disponible en toda la web
  window.CarritoJuanin = {
    obtener: obtenerCarrito,
    agregar: agregarAlCarrito,
    cambiarCantidad: cambiarCantidad,
    establecerCantidad: establecerCantidad,
    eliminar: eliminarDelCarrito,
    vaciar: vaciarCarrito,
    totales: calcularTotales,
    formatoPrecio: formatoPrecio,
    conectarBotones: conectarBotonesAgregar,
    actualizarContadores: actualizarContadoresHeader
  };
})(window);
