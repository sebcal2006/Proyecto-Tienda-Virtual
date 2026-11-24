# Fase 6 – Programa funcional · Tienda Virtual (Grupo 03)

Aplicación web del Sistema de Gestión de Ventas, construida sobre el modelo POO de la Fase 3 y la base de datos `virtual_store` de la Fase 4. Las pantallas siguen el mockup de la Fase 5.

**Tecnologías:** Java 17+, Spring Boot 3.5 (Web, Data JPA, Security, Validation), Thymeleaf y MySQL 8.

## Funcionalidades (MVP de la Fase 1)

El programa cubre las 30 historias **Must** de la priorización de la Fase 1 (sección 2.15 del documento), con un flujo completo para cada uno de los 6 roles, y además varias historias **Should**.

| Rol | Módulo | Historias |
|---|---|---|
| Todos | Inicio de sesión por rol, cuenta inactiva, cierre de sesión | HU01 |
| Administrador | Panel con indicadores; usuarios y roles; productos (crear, modificar, desactivar con advertencia) | HU02, HU03, HU05, HU06, HU07 |
| Cliente | Registro, catálogo con búsqueda y filtro, carrito, compra con dirección y pago simulado (rechazo y reintento), seguimiento e historial | HU04, HU08, HU09, HU23 a HU27, HU32, HU33, HU34, HU39, HU40 |
| Encargado de inventario | Existencias, alerta de reposición, entradas, salidas e historial por producto | HU11, HU12, HU13, HU14 |
| Vendedor | Venta presencial (cliente por documento, productos, cantidades, total), pedidos con filtros, cambio de estado, cancelación y asignación de domiciliario | HU15, HU16, HU18 a HU22, HU28, HU30, HU31, HU35 |
| Domiciliario | Sus entregas, marcar en camino, confirmar entrega o registrar incidencia | HU36, HU37, HU38 |
| Encargado de facturación | Pedidos por facturar, factura con número único e IVA 19 %, consulta e impresión en PDF | HU41, HU42 |

Reglas de negocio aplicadas en los servicios: RN-001 (carrito no vacío), RN-002 (existencia), RN-003 (cancelación solo en Pendiente o En preparación), RN-004 (orden de estados), RN-005 (descuenta inventario al confirmar y lo devuelve al cancelar, con movimientos ENTRY/EXIT), RN-008 (total = cantidad × precio) y RNF-008 (bloqueo de fila del producto para que dos ventas no tomen la misma unidad). RNF-002: cada rol solo entra a sus módulos; el administrador consulta inventario y facturas sin modificarlos.

**Flujo de un pedido en línea:** el cliente compra → el vendedor lo pasa a *En preparación* (requiere pago aprobado) y asigna un domiciliario → el domiciliario lo marca *En camino* y confirma la entrega → el encargado de facturación genera la factura.

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
| inventario@tienda.com | Tienda123 | Encargado de inventario |
| facturacion@tienda.com | Tienda123 | Encargado de facturación |
| domiciliario@tienda.com | Tienda123 | Domiciliario |
| laura.gomez@correo.com | Cliente123 | Cliente |
| ana.torres@correo.com | Cliente123 | Cliente |
| inactivo@tienda.com | Tienda123 | Cuenta inactiva (debe ser rechazada) |

## Guion corto de prueba

1. **Cliente** (`laura.gomez@correo.com`): agregar productos al carrito, confirmar la compra con una dirección y ver el pedido en *Mis pedidos*. Repetir marcando *Simular pago rechazado* y reintentar el pago.
2. **Vendedor** (`vendedor@tienda.com`): registrar una venta presencial con el documento `1012345678`. Abrir el pedido en línea, pasarlo a *En preparación* y asignar a Diego Domiciliario.
3. **Domiciliario** (`domiciliario@tienda.com`): marcar el pedido *En camino* y confirmar la entrega (o registrar una incidencia).
4. **Facturación** (`facturacion@tienda.com`): generar la factura del pedido entregado e imprimirla como PDF.
5. **Inventario** (`inventario@tienda.com`): registrar una entrada y una salida y revisar el historial del producto.
6. **Administrador** (`admin@tienda.com`): revisar el panel, crear un usuario con un rol, crear un producto (entra con existencia 0) y desactivarlo.

Para volver a los datos iniciales se ejecuta de nuevo `Fase-4/database/tienda_virtual.sql`.

## Estructura

```
src/main/java/co/edu/sena/tiendavirtual/
├── entity/       Entidades JPA mapeadas a las tablas de la Fase 4
├── repository/   Spring Data JPA
├── service/      Reglas de negocio (pedidos, carrito, clientes)
├── config/       Seguridad por roles y contraseñas de prueba
└── web/          Controladores por módulo (cliente, vendedor, admin, inventario, domicilios, facturación)
src/main/resources/templates/   Vistas Thymeleaf
src/test/java/                  Pruebas unitarias (JUnit 5 + Mockito)
```
