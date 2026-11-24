# Tienda Virtual: modelo POO en Java (Fase 3)

Implementación en Java de las clases del diagrama de clases (sección 5.2 del documento). Los nombres de clases y atributos están en inglés y coinciden con las tablas del script SQL de la Fase 4 (`products`, `orders`, `invoices`…). Así, en la Fase 6 estas clases se pueden convertir en entidades JPA de Spring Boot sin renombrar nada.

## Requisitos

- JDK 17 o superior
- Maven 3.8 o superior

## Cómo ejecutarlo

```bash
mvn test              # 33 pruebas unitarias de las reglas de negocio
mvn compile exec:java # recorre la compra en línea, la venta presencial y la cancelación
```

## Estructura

```
src/main/java/co/edu/sena/tiendavirtual/
├── TiendaVirtualDemo.java        demostración por consola
├── exception/BusinessRuleException.java
└── model/
    ├── User, Role, RoleName       usuarios y los 6 roles (RF-001 a RF-003)
    ├── Customer                   cliente con o sin cuenta (RF-004, RF-015)
    ├── Category, Product          catálogo (RF-005 a RF-009)
    ├── InventoryMovement          entradas y salidas (RF-010 a RF-014)
    ├── Cart, CartItem             carrito y compra en línea (RF-023 a RF-026)
    ├── InStoreSale                venta presencial en curso (RF-019 a RF-022)
    ├── Order, OrderItem           pedido de los dos canales (RF-028 a RF-031)
    ├── OrderStatus, OrderStatusHistory
    ├── DeliveryAddress            dirección de entrega (RF-027)
    ├── PaymentMethod, Payment     pagos (RF-032 a RF-034)
    ├── Delivery                   domicilios (RF-035 a RF-038)
    └── Invoice                    facturación (RF-041, RF-042)
```

## Dónde se cumple cada regla de negocio

| Regla | Clase y método |
|---|---|
| RN-001 Carrito no vacío para comprar | `Cart.checkout()` |
| RN-002 No vender sin existencia | `Product.checkAvailable()` (lo usan `Cart` e `InStoreSale`) |
| RN-003 Cancelar solo si está pendiente o en preparación | `OrderStatus.canBeCancelled()`, `Order.cancel()` |
| RN-004 Los estados avanzan en orden | `OrderStatus.next()`, `Order.advanceStatus()` |
| RN-005 Descontar al confirmar, devolver al cancelar | `Order.create()`, `Order.cancel()`, `InventoryMovement` |
| RN-006 Factura con pago aprobado o venta finalizada | `Invoice.generate()` |
| RN-007 Editar la venta solo antes de finalizarla | `InStoreSale` |
| RN-008 Total = suma de cantidad × precio | `Cart.getTotal()`, `Order.calculateTotal()`, `OrderItem.getSubtotal()` |
| RNF-002 Acceso por rol (el administrador consulta) | `User.requireRole()`, `User.canView()` |

Los mensajes de error son los textos de los criterios de aceptación del ERS (sección 4.7), por ejemplo *"Existencia insuficiente"* o *"Esta cuenta se encuentra inactiva"*.

## Diferencias con la base de datos

- `InStoreSale` no es una tabla: es la venta que el vendedor arma en pantalla. Al confirmarla se guarda como un `Order` del canal `IN_STORE`.
- Los `id` quedan en `null` hasta que la clase se guarde en la base de datos (Fase 6).
