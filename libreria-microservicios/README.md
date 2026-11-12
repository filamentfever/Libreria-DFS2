# Librería Juanin · Microservicios (Spring Boot)

Backend de la tienda: **3 servicios de infraestructura + 9 microservicios de negocio**, con **Spring Boot 4.0.5 · Java 21 · Spring Cloud (OpenFeign, Eureka, Gateway, Config)**,
arquitectura **CSR (Controller → Service → Repository)** y pruebas unitarias con **JUnit 5 + Mockito + AssertJ** (guía de pruebas *codigoms_carrito*).


## Mapa de servicios

| Módulo | Puerto | Rol | Cubre del front React |
|---|---|---|---|
| `codigoms_eureka` | 8761 | Registro de servicios (Eureka Server) | — |
| `codigoms_config_server` | 8888 | Configuración centralizada (Config Server) | — |
| `codigoms_gateway` | 8080 | Punto de entrada único: rutas `lb://`, CORS global | todas las llamadas |
| `codigoms_usuarios` | 8081 | Registro, login, recuperar contraseña, perfil, direcciones | `cuentaService.ts` |
| `codigoms_catalogo` | 8082 | Productos, categorías, filtros, stock, inventario, CSV | `productoService.ts`, `categoriaService.ts`, `adminService.ts` (inventario) |
| `codigoms_carrito` | 8083 | Carrito (→ usuarios, catálogo) | `carritoService.ts` |
| `codigoms_pagos` | 8084 | Tarjeta simulada, cupones, transferencia | `pagoService.ts` |
| `codigoms_pedidos` | 8085 | Checkout, pedidos, estados (→ carrito, catálogo, pagos) | `pedidoService.ts`, `adminService.ts` (pedidos) |
| `codigoms_envios` | 8086 | Cotización y seguimiento de despachos; puntos de retiro | `envioService.ts`, `retiroService.ts` |
| `codigoms_resenas` | 8087 | Reseñas y promedio de valoración (→ catálogo, usuarios) | `resenaService.ts` |
| `codigoms_notificaciones` | 8088 | Cola y envío de correos (registro, pedido, alertas) | `notificacionService.ts` |
| `codigoms_admin` | 8089 | Dashboard y reportes consolidados (→ pedidos, catálogo, usuarios) | `adminService.ts` (métricas) |

Detalle servicio por servicio del front: [`docs/MAPA_FRONTEND.md`](docs/MAPA_FRONTEND.md).

```
React (5173/3000) ──▶ codigoms_gateway :8080 ──lb://CODIGOMS-*──▶ cada microservicio
                              │                                         │
                              └──────── consulta ─────────▶ codigoms_eureka :8761 ◀── se registran
   Feign (llamadas entre servicios):
   carrito ─▶ usuarios, catalogo        pedidos ─▶ carrito, catalogo, pagos
   resenas ─▶ catalogo, usuarios        admin   ─▶ pedidos, catalogo, usuarios
```

## Estructura de cada microservicio de negocio (CSR)

```
codigoms_<nombre>/
├─ pom.xml
└─ src/main/
   ├─ java/com/libreriajuanin/<nombre>/
   │  ├─ <Nombre>Application.java
   │  ├─ controller/   REST (sin lógica de negocio)
   │  ├─ service/      lógica de negocio  ← lo que prueban los tests
   │  ├─ repository/   Spring Data JPA
   │  ├─ model/        entidades (y enums)
   │  ├─ dto/          entrada/salida y contratos con otros servicios
   │  ├─ client/       OpenFeign (solo carrito, pedidos, resenas y admin)
   │  ├─ config/       CorsConfig, datos iniciales, seguridad
   │  └─ exception/    excepciones + GlobalExceptionHandler (404 / 400 / 402 / 503)
   └─ resources/application.properties
```

## Cómo levantarlo

Requisitos: **JDK 21**, **Maven 3.9+** y **MySQL/MariaDB con phpMyAdmin** (XAMPP, Laragon o WAMP). Una terminal por servicio, **en este orden**:

```bash
cd codigoms_eureka          && mvn spring-boot:run   # 1) http://localhost:8761  (panel de Eureka)
cd codigoms_config_server   && mvn spring-boot:run   # 2) opcional (ver "Config Server")
cd codigoms_usuarios        && mvn spring-boot:run   # 3) negocio, en cualquier orden entre sí
cd codigoms_catalogo        && mvn spring-boot:run
cd codigoms_pagos           && mvn spring-boot:run
cd codigoms_carrito         && mvn spring-boot:run
cd codigoms_pedidos         && mvn spring-boot:run
cd codigoms_envios          && mvn spring-boot:run
cd codigoms_resenas         && mvn spring-boot:run
cd codigoms_notificaciones  && mvn spring-boot:run
cd codigoms_admin           && mvn spring-boot:run
cd codigoms_gateway         && mvn spring-boot:run   # 4) al final: http://localhost:8080
```
Los servicios tardan ~30 s en aparecer en Eureka; hasta entonces el Gateway responde 503. Cada servicio usa **su propia base MySQL** (`libreria_<servicio>`), visible en phpMyAdmin; **MySQL debe estar encendido antes de arrancar** los microservicios. Guía: [`docs/BASE_DE_DATOS.md`](docs/BASE_DE_DATOS.md).
Peticiones de ejemplo: [`docs/PRUEBAS_HTTP.http`](docs/PRUEBAS_HTTP.http).

## Pruebas unitarias

```bash
mvn test                       # todos los módulos (reporte JaCoCo en codigoms_*/target/site/jacoco/index.html)
cd codigoms_carrito && mvn test
```

| Módulo | Casos | | Módulo | Casos |
|---|---|---|---|---|
| usuarios | 24 | | envios | 25 |
| catalogo | 31 | | resenas | 9 |
| carrito | 15 | | notificaciones | 13 |
| pagos | 14 | | admin | 12 |
| pedidos | 29 | | **Total** | **172** |

Las pruebas unitarias usan Mockito: **no necesitan MySQL ni Eureka** (se pueden correr con todo apagado).

Cómo se aplicó la guía del PDF: [`docs/GUIA_DE_PRUEBAS.md`](docs/GUIA_DE_PRUEBAS.md). Eureka, Config Server y Gateway no llevan pruebas (no tienen lógica propia).

