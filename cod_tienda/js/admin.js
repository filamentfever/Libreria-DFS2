/* =========================================================
   Librería Juanin — Panel de administración
   EP-12: Gestión de Inventario
   EP-13: Gestión de Pedidos
   Persistencia: localStorage
   ========================================================= */

(function (window) {
  "use strict";

  const CLAVE_PRODUCTOS = "juaninAdminProductos";
  const CLAVE_PEDIDOS = "juaninAdminPedidos";
  const UMBRAL_STOCK_BAJO = 5;

  const ESTADOS_PEDIDO = [
    "Nuevo",
    "Pendiente",
    "Armando caja",
    "Listo para retiro",
    "Enviado",
    "Entregado",
    "Cancelado"
  ];

  const CATEGORIAS = ["Papelería", "Lapicería", "Librería"];

  /* ---------- Datos de ejemplo (mismo catálogo del sitio) ---------- */

  const PRODUCTOS_INICIALES = [
    { id: "p1", sku: "SKU-P1", nombre: "Cuaderno Miku", categoria: "Papelería", precio: 19990, stock: 12, imagen: "img/cuadernomiku.jpg" },
    { id: "p2", sku: "SKU-P2", nombre: "Lapiz mina", categoria: "Lapicería", precio: 12500, stock: 3, imagen: "img/lapizmina.jpg" },
    { id: "p3", sku: "SKU-P3", nombre: "Resma de hojas", categoria: "Papelería", precio: 8990, stock: 25, imagen: "img/hojascarta.jpg" },
    { id: "p4", sku: "SKU-P4", nombre: "Libro", categoria: "Librería", precio: 24000, stock: 8, imagen: "img/libroaura.jpg" },
    { id: "p5", sku: "SKU-P5", nombre: "Estuche", categoria: "Papelería", precio: 15750, stock: 0, imagen: "img/estuche.jpg" },
    { id: "p6", sku: "SKU-P6", nombre: "Goma de borrar", categoria: "Lapicería", precio: 32200, stock: 4, imagen: "img/gomadeborrar.jpg" }
  ];

  function fechaHace(dias) {
    const d = new Date();
    d.setDate(d.getDate() - dias);
    return d.toISOString().slice(0, 10);
  }

  const PEDIDOS_INICIALES = [
    { id: "ORD-1001", cliente: "Camila Rojas", fecha: fechaHace(0), estado: "Nuevo", entrega: "Domicilio",
      items: [{ nombre: "Cuaderno Miku", cantidad: 2, precio: 19990 }, { nombre: "Lapiz mina", cantidad: 1, precio: 12500 }] },
    { id: "ORD-1002", cliente: "Benjamín Soto", fecha: fechaHace(1), estado: "Pendiente", entrega: "Retiro en tienda",
      items: [{ nombre: "Resma de hojas", cantidad: 3, precio: 8990 }] },
    { id: "ORD-1003", cliente: "Valentina Muñoz", fecha: fechaHace(2), estado: "Armando caja", entrega: "Domicilio",
      items: [{ nombre: "Libro", cantidad: 1, precio: 24000 }, { nombre: "Estuche", cantidad: 1, precio: 15750 }] },
    { id: "ORD-1004", cliente: "Matías Fuentes", fecha: fechaHace(4), estado: "Listo para retiro", entrega: "Retiro en tienda",
      items: [{ nombre: "Goma de borrar", cantidad: 2, precio: 32200 }] },
    { id: "ORD-1005", cliente: "Javiera Contreras", fecha: fechaHace(6), estado: "Enviado", entrega: "Domicilio",
      items: [{ nombre: "Cuaderno Miku", cantidad: 1, precio: 19990 }, { nombre: "Resma de hojas", cantidad: 2, precio: 8990 }] },
    { id: "ORD-1006", cliente: "Ignacio Pérez", fecha: fechaHace(9), estado: "Entregado", entrega: "Domicilio",
      items: [{ nombre: "Libro", cantidad: 2, precio: 24000 }] },
    { id: "ORD-1007", cliente: "Florencia Díaz", fecha: fechaHace(12), estado: "Cancelado", entrega: "Retiro en tienda",
      items: [{ nombre: "Lapiz mina", cantidad: 4, precio: 12500 }] }
  ];

  /* ---------- Acceso a datos ---------- */

  function calcularTotal(items) {
    return items.reduce(function (acc, it) { return acc + it.precio * it.cantidad; }, 0);
  }

  function obtenerProductos() {
    try {
      const datos = localStorage.getItem(CLAVE_PRODUCTOS);
      return datos ? JSON.parse(datos) : semillaProductos();
    } catch (e) {
      return semillaProductos();
    }
  }

  function semillaProductos() {
    const productos = PRODUCTOS_INICIALES.map(function (p) {
      return Object.assign({}, p, { activo: p.stock > 0 });
    });
    guardarProductos(productos);
    return productos;
  }

  function guardarProductos(productos) {
    localStorage.setItem(CLAVE_PRODUCTOS, JSON.stringify(productos));
  }

  function obtenerPedidos() {
    try {
      const datos = localStorage.getItem(CLAVE_PEDIDOS);
      return datos ? JSON.parse(datos) : semillaPedidos();
    } catch (e) {
      return semillaPedidos();
    }
  }

  function semillaPedidos() {
    const pedidos = PEDIDOS_INICIALES.map(function (p) {
      return Object.assign({}, p, { total: calcularTotal(p.items) });
    });
    guardarPedidos(pedidos);
    return pedidos;
  }

  function guardarPedidos(pedidos) {
    localStorage.setItem(CLAVE_PEDIDOS, JSON.stringify(pedidos));
  }

  function formatoPrecio(valor) {
    return "$" + Number(valor).toLocaleString("es-CL");
  }

  function formatoFecha(iso) {
    const [a, m, d] = iso.split("-");
    return d + "/" + m + "/" + a;
  }

  function idUnico(prefijo) {
    return prefijo + "-" + Date.now().toString(36).toUpperCase();
  }

  window.AdminJuanin = {
    ESTADOS_PEDIDO: ESTADOS_PEDIDO,
    CATEGORIAS: CATEGORIAS,
    UMBRAL_STOCK_BAJO: UMBRAL_STOCK_BAJO,
    obtenerProductos: obtenerProductos,
    guardarProductos: guardarProductos,
    obtenerPedidos: obtenerPedidos,
    guardarPedidos: guardarPedidos,
    calcularTotal: calcularTotal,
    formatoPrecio: formatoPrecio,
    formatoFecha: formatoFecha,
    idUnico: idUnico
  };

})(window);
