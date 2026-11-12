# Librería Juanin

Tienda online de librería y papelería (cuadernos, lápices, resmas, libros, estuches, etc.). El cliente puede ver el catálogo, filtrar y buscar, armar su carrito, pagar con tarjeta o transferencia y elegir si le despachan a domicilio o retira en una sucursal. También hay un panel de administración para manejar el inventario y los pedidos.

Está dividido en dos partes que se levantan por separado:

- **Backend**: microservicios en Java con Spring Boot.
- **Frontend**: una SPA en React con TypeScript.

El front nunca habla directo con los microservicios, todo pasa por un API Gateway.

## Cómo está armado

En vez de un solo backend grande, cada parte del negocio es un servicio aparte, con su puerto y su propia base de datos:

```
React (5173)  ->  API Gateway (8080)  ->  microservicios de negocio
                         |
                      Eureka (8761)  <- cada servicio se registra aquí
```

- **Eureka** funciona como directorio: cada servicio se registra al arrancar y el Gateway los encuentra por nombre, sin depender de puertos.
- **Gateway** es la única entrada. Recibe la petición de React, mira la ruta (`/api/productos/...`, `/api/pedidos/...`) y la manda al servicio que corresponde. También maneja el CORS.
- Entre servicios se llaman con **OpenFeign** (por ejemplo, pedidos le pregunta stock a catálogo y cobra a través de pagos).
- Cada servicio sigue la misma estructura: Controller -> Service -> Repository, con DTOs para lo que entra y sale por la API.

### Servicios

| Servicio | Puerto | Qué hace |
|---|---|---|
| `codigoms_eureka` | 8761 | Registro de servicios |
| `codigoms_config_server` | 8888 | Configuración centralizada (opcional) |
| `codigoms_gateway` | 8080 | Punto de entrada único |
| `codigoms_usuarios` | 8081 | Registro, login, recuperar clave, perfil y direcciones |
| `codigoms_catalogo` | 8097 | Productos, categorías, búsqueda, stock e inventario |
| `codigoms_carrito` | 8083 | Carrito de cada usuario |
| `codigoms_pagos` | 8084 | Pago con tarjeta (simulado), cupones y datos de transferencia |
| `codigoms_pedidos` | 8085 | Checkout y pedidos |
| `codigoms_envios` | 8086 | Cotización de despacho, seguimiento y puntos de retiro |
| `codigoms_resenas` | 8087 | Reseñas y promedio de valoración |
| `codigoms_notificaciones` | 8088 | Cola de correos (registro, pedido, alertas) |
| `codigoms_admin` | 8089 | Dashboard y reportes para el panel admin |

### Cómo es una compra

Cuando el cliente paga, el front hace un `POST /api/pedidos` y el servicio de pedidos coordina todo: lee el carrito, revisa el stock en catálogo, calcula el despacho, valida el cupón y cobra en pagos. Si el pago se rechaza no se crea nada. Si pasa, guarda el pedido (número `ORD-1001` en adelante), descuenta el stock y vacía el carrito.

## Qué hay en el repo

```
libreria-microservicios/   backend (parent pom + 12 módulos)
tienda-react/              frontend en React + TypeScript + Vite
cod_tienda/                primera versión de la tienda (HTML/Bootstrap estático)
documentos/                documentación del proyecto
```

## Tecnologías

- Java 21, Spring Boot 4.0.5, Spring Cloud 2025.1.2 (Eureka, Gateway, OpenFeign, Config)
- Spring Data JPA con MySQL/MariaDB
- JUnit 5, Mockito y AssertJ para las pruebas unitarias
- React 19, TypeScript, Vite y react-router

## Cómo correrlo

Necesitas JDK 21, Maven, Node.js y MySQL o MariaDB encendido (con XAMPP funciona). Las bases `libreria_*` se crean solas la primera vez, y el usuario por defecto es `root` sin clave (se puede cambiar con las variables `DB_USER` y `DB_PASSWORD`).

**Backend.** El orden importa: Eureka primero, el Gateway al final y los de negocio en el medio, cada uno en su terminal.

```bash
cd libreria-microservicios/codigoms_eureka && mvn spring-boot:run
# luego: usuarios, catalogo, carrito, pagos, pedidos, envios, resenas, notificaciones, admin
# (config_server es opcional)
cd libreria-microservicios/codigoms_gateway && mvn spring-boot:run
```

Los servicios demoran unos 30 segundos en aparecer en Eureka (`http://localhost:8761`). Hasta entonces el Gateway responde 503.

**Frontend.**

```bash
cd tienda-react
npm install
npm run dev
```

Queda en `http://localhost:5173`. La URL del backend sale de `VITE_API_URL` (hay un `.env.example` de referencia) y por defecto apunta al Gateway en `http://localhost:8080`. En PowerShell, si `npm` da error de scripts, usa `npm.cmd` o la terminal de cmd.

**Tests.** Desde `libreria-microservicios`, `mvn test` corre las pruebas de todos los módulos.

## Cosas útiles al probar

- Al abrir el front se crea solo un usuario (`demo` o un invitado) con su carrito, así que se puede comprar sin registrarse.
- El pago con tarjeta es simulado: una tarjeta que termina en `0000` da saldo insuficiente y una que termina en `9999` da error temporal. Cualquier otra valida bien.
- Los correos salen en modo `simulado` (solo se registran). Para enviarlos de verdad hay que cambiar `app.correo.modo` a `smtp` en `codigoms_notificaciones` y configurar el correo.
- Los productos de ejemplo se cargan solo si la base del catálogo está vacía.

## Pendientes y limitaciones

- No hay autenticación con tokens: el login devuelve los datos del usuario y la sesión se guarda en el navegador.
- El panel `/adm` y los endpoints de admin no validan roles.
- Cada servicio tiene su propia base, así que no hay una transacción que cubra todo el checkout. Si falla un paso del final, el pedido ya queda guardado.
- Los favoritos se guardan en el navegador, no hay servicio para eso en el backend.
- El pago y el envío de correos están simulados.

## Integrantes

- Martina Córdova
- Fabián Millar
- Ignacio Retamal
