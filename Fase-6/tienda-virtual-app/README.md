# Fase 6 – Programa funcional · Tienda Virtual (Grupo 03)

Aplicación web del Sistema de Gestión de Ventas, construida sobre el modelo POO de la Fase 3 y la base de datos `virtual_store` de la Fase 4. Las pantallas siguen el mockup de la Fase 5.

**Tecnologías:** Java 17+, Spring Boot 3.5 (Web, Data JPA, Security, Validation), Thymeleaf y MySQL 8.

## Flujos incluidos

| Flujo | Rol | Requisitos |
|---|---|---|
| Inicio de sesión por rol, cierre de sesión, cuenta inactiva | Todos | RF-001, RF-002, RF-003, RNF-002 |
| Registro de cliente en línea | Visitante | RF-004 |
| Catálogo, búsqueda, filtro por categoría y detalle | Visitante / Cliente | RF-008, RF-009, RF-010 |
| Carrito: agregar, cambiar cantidad, quitar | Cliente | RF-023, RF-024, RF-025 |
| Compra en línea con dirección y pago (pasarela simulada, con opción de rechazo y reintento) | Cliente | RF-026, RF-027, RF-032, RF-033 |
| Mis pedidos y seguimiento | Cliente | RF-034, RF-040 |
| Venta presencial o telefónica (buscar/registrar cliente, agregar productos, confirmar) | Vendedor | RF-015, RF-016, RF-018 a RF-022 |
| Consulta de pedidos con filtros, detalle, cambio de estado y cancelación | Vendedor (gestiona) / Administrador (consulta) | RF-028 a RF-031 |

Reglas de negocio aplicadas en `OrderService` y `CartService`: RN-001 (carrito no vacío), RN-002 (existencia), RN-003 (cancelación solo en Pendiente o En preparación), RN-004 (orden de estados), RN-005 (descuenta inventario al confirmar y lo devuelve al cancelar, con movimientos ENTRY/EXIT), RN-008 (total = cantidad × precio) y RNF-008 (bloqueo de fila del producto para que dos ventas no tomen la misma unidad).

## Cómo ejecutarla (Windows, PowerShell)

1. Tener la base de datos cargada con el script de la Fase 4 (`Fase-4/database/tienda_virtual.sql`). Si ya se hicieron compras de prueba y se quiere empezar de cero, se vuelve a ejecutar el script.
2. Abrir PowerShell en esta carpeta y ejecutar:

```powershell
$env:Path = "$env:USERPROFILE\tools\apache-maven-3.9.9\bin;$env:Path"
$env:DB_PASSWORD = "su-contraseña-de-mysql"
mvn spring-boot:run
```

3. Cuando aparezca `Started TiendaVirtualApplication`, abrir **http://localhost:8080**.

Para correr solo las pruebas unitarias (no necesitan MySQL): `mvn test`.

La contraseña de MySQL se pasa por la variable `DB_PASSWORD` para no guardarla en el repositorio. Si el usuario de MySQL no es `root`, se define también `$env:DB_USER`.

## Cuentas de prueba

Los datos de la Fase 4 traen contraseñas de ejemplo. Al arrancar, la aplicación les asigna contraseñas reales (solo a esos registros de ejemplo):

| Correo | Contraseña | Rol |
|---|---|---|
| admin@tienda.com | Tienda123 | Administrador (y vendedor) |
| vendedor@tienda.com | Tienda123 | Vendedor |
| laura.gomez@correo.com | Cliente123 | Cliente |
| ana.torres@correo.com | Cliente123 | Cliente |
| inactivo@tienda.com | Tienda123 | Cuenta inactiva (debe ser rechazada) |

## Guion corto de prueba

1. Entrar como **laura.gomez@correo.com**, agregar dos productos al carrito, confirmar la compra con una dirección y ver el pedido en *Mis pedidos*.
2. Repetir marcando *Simular pago rechazado* y luego reintentar el pago desde el detalle del pedido.
3. Entrar como **vendedor@tienda.com**, registrar una venta presencial (buscar el documento `1012345678` o registrar un cliente nuevo), confirmarla y avanzar su estado.
4. Cancelar un pedido pendiente y comprobar en MySQL que la existencia volvió: `SELECT code, stock FROM products;` y `SELECT * FROM inventory_movements ORDER BY id DESC;`.
5. Entrar como **admin@tienda.com** y revisar el listado de pedidos con los filtros.

## Estructura

```
src/main/java/co/edu/sena/tiendavirtual/
├── entity/       Entidades JPA mapeadas a las tablas de la Fase 4
├── repository/   Spring Data JPA
├── service/      Reglas de negocio (pedidos, carrito, clientes)
├── config/       Seguridad por roles y contraseñas de prueba
└── web/          Controladores
src/main/resources/templates/   Vistas Thymeleaf
src/test/java/                  Pruebas unitarias (JUnit 5 + Mockito)
```
