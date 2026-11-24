-- =====================================================================
--  PROYECTO TIENDA VIRTUAL - GRUPO 03
--  SENA - Ficha 3239188 - ADSO
--  FASE 4: Base de datos relacional (MySQL 8.x, 3FN)
--
--  Convenciones (retroalimentación del instructor):
--    * Tablas en plural y en inglés        -> users, products, orders...
--    * Columnas en inglés y snake_case     -> unit_price, created_at...
--    * Valores de estado en inglés         -> PENDING, DELIVERED...
--
--  Origen: Fase 2 (RF-001..RF-043, RN-001..RN-008) y Fase 3 (diagrama de clases).
--  Orden del script: 1) Base de datos  2) Tablas  3) Datos de prueba
--                    4) Consultas de verificación
-- =====================================================================
 
DROP DATABASE IF EXISTS virtual_store;
CREATE DATABASE virtual_store
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE virtual_store;
 
-- =====================================================================
-- 1. ACCESO Y USUARIOS  (Épica 1: RF-001 a RF-004)
-- =====================================================================
 
-- Roles del sistema (los 6 actores de la Fase 1)
CREATE TABLE roles (
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(30)  NOT NULL,
    description VARCHAR(150) NULL,
    CONSTRAINT pk_roles      PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB;
 
-- Cuentas de acceso (personal interno y clientes con cuenta)
CREATE TABLE users (
    id            INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,              -- RNF-003: nunca texto plano
    phone         VARCHAR(20)  NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE, -- RF-001 E2 / RF-002: cuenta inactiva
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users       PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)          -- RF-004 E1: correo duplicado
) ENGINE=InnoDB;
 
-- Relación N:M usuario-rol (RF-003: "uno o más roles")
CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    CONSTRAINT pk_user_roles      PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB;
 
-- =====================================================================
-- 2. CLIENTES  (RF-004, RF-015 a RF-017)
--    Un cliente puede tener cuenta (user_id) o haber sido registrado por
--    un vendedor sin cuenta propia (user_id NULL).
-- =====================================================================
CREATE TABLE customers (
    id         INT          NOT NULL AUTO_INCREMENT,
    user_id    INT          NULL,
    document   VARCHAR(20)  NULL,                      -- RF-015 E1: documento único
    name       VARCHAR(100) NOT NULL,
    phone      VARCHAR(20)  NULL,
    email      VARCHAR(120) NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_customers          PRIMARY KEY (id),
    CONSTRAINT uq_customers_user     UNIQUE (user_id),   -- relación 0..1 con users
    CONSTRAINT uq_customers_document UNIQUE (document),  -- NULL permitido varias veces
    CONSTRAINT fk_customers_user     FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;
 
-- =====================================================================
-- 3. CATÁLOGO  (Épica 2: RF-005 a RF-009)
-- =====================================================================
CREATE TABLE categories (
    id   INT          NOT NULL AUTO_INCREMENT,
    name VARCHAR(60)  NOT NULL,
    CONSTRAINT pk_categories      PRIMARY KEY (id),
    CONSTRAINT uq_categories_name UNIQUE (name)
) ENGINE=InnoDB;
 
CREATE TABLE products (
    id          INT           NOT NULL AUTO_INCREMENT,
    category_id INT           NOT NULL,
    code        VARCHAR(30)   NOT NULL,                       -- RF-005 E1: código único
    name        VARCHAR(120)  NOT NULL,
    description VARCHAR(500)  NULL,
    price       DECIMAL(12,2) NOT NULL,
    stock       INT           NOT NULL DEFAULT 0,             -- RF-005: existencia inicial en 0
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,          -- RF-007: desactivar, no borrar
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_products          PRIMARY KEY (id),
    CONSTRAINT uq_products_code     UNIQUE (code),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_products_price    CHECK (price >= 0),       -- CA-RF-006: precio no negativo
    CONSTRAINT ck_products_stock    CHECK (stock >= 0)        -- RF-010: existencia no negativa
) ENGINE=InnoDB;
 
CREATE INDEX idx_products_name ON products (name);            -- RF-009 / RF-018: búsqueda por nombre
 
-- =====================================================================
-- 4. INVENTARIO  (Épica 3: RF-010 a RF-014)
--    Historial de entradas y salidas. 'order_id' deja trazabilidad cuando
--    el movimiento viene de una venta o de una cancelacion.
-- =====================================================================
CREATE TABLE inventory_movements (
    id         INT                    NOT NULL AUTO_INCREMENT,
    product_id INT                    NOT NULL,
    user_id    INT                    NOT NULL,               -- quien registra el movimiento
    order_id   INT                    NULL,                   -- FK se agrega después de crear orders
    type       ENUM('ENTRY','EXIT')   NOT NULL,
    quantity   INT                    NOT NULL,
    notes      VARCHAR(200)           NULL,
    created_at DATETIME               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_inventory_movements PRIMARY KEY (id),
    CONSTRAINT fk_movements_product   FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_movements_user      FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT ck_movements_quantity  CHECK (quantity > 0)    -- RF-011 E1: cantidad > 0
) ENGINE=InnoDB;
 
CREATE INDEX idx_movements_product_date ON inventory_movements (product_id, created_at);
 
-- =====================================================================
-- 5. CARRITO  (Épica 5: RF-023 a RF-025)
-- =====================================================================
CREATE TABLE carts (
    id          INT      NOT NULL AUTO_INCREMENT,
    customer_id INT      NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_carts          PRIMARY KEY (id),
    CONSTRAINT uq_carts_customer UNIQUE (customer_id),         -- un carrito por cliente (0..1)
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
) ENGINE=InnoDB;
 
CREATE TABLE cart_items (
    id         INT NOT NULL AUTO_INCREMENT,
    cart_id    INT NOT NULL,
    product_id INT NOT NULL,
    quantity   INT NOT NULL,
    CONSTRAINT pk_cart_items         PRIMARY KEY (id),
    CONSTRAINT uq_cart_items_product UNIQUE (cart_id, product_id),  -- no repetir producto en el carrito
    CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_cart_items_qty     CHECK (quantity > 0)
) ENGINE=InnoDB;
 
-- =====================================================================
-- 6. PEDIDOS  (Épica 4 y 6: RF-019 a RF-022, RF-026 a RF-031)
-- =====================================================================
CREATE TABLE delivery_addresses (
    id        INT          NOT NULL AUTO_INCREMENT,
    address   VARCHAR(200) NOT NULL,                           -- RF-027: campos obligatorios
    city      VARCHAR(80)  NOT NULL,
    reference VARCHAR(200) NULL,
    CONSTRAINT pk_delivery_addresses PRIMARY KEY (id)
) ENGINE=InnoDB;
 
CREATE TABLE orders (
    id                  INT           NOT NULL AUTO_INCREMENT,
    customer_id         INT           NOT NULL,
    seller_id           INT           NULL,                    -- solo canal presencial (vendedor que atiende)
    delivery_address_id INT           NULL,
    channel             ENUM('IN_STORE','ONLINE') NOT NULL,
    status              ENUM('PENDING','IN_PREPARATION','ON_THE_WAY','DELIVERED','CANCELLED')
                                      NOT NULL DEFAULT 'PENDING',   -- RF-019 / RF-026: nace "pendiente"
    total               DECIMAL(14,2) NOT NULL DEFAULT 0,      -- RN-008: sum(quantity * unit_price)
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_orders          PRIMARY KEY (id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id)         REFERENCES customers (id),
    CONSTRAINT fk_orders_seller   FOREIGN KEY (seller_id)           REFERENCES users (id),
    CONSTRAINT fk_orders_address  FOREIGN KEY (delivery_address_id) REFERENCES delivery_addresses (id),
    CONSTRAINT ck_orders_total    CHECK (total >= 0)
) ENGINE=InnoDB;
 
CREATE INDEX idx_orders_status  ON orders (status);
CREATE INDEX idx_orders_created ON orders (created_at);        -- reportes por rango de fechas (RF-043)
 
-- Ahora si, se enlaza el movimiento de inventario con el pedido
ALTER TABLE inventory_movements
    ADD CONSTRAINT fk_movements_order FOREIGN KEY (order_id) REFERENCES orders (id);
 
-- Detalle del pedido. unit_price es una "foto" del precio al momento de la venta:
-- si el producto cambia de precio después, el pedido histórico no se altera.
CREATE TABLE order_items (
    id         INT           NOT NULL AUTO_INCREMENT,
    order_id   INT           NOT NULL,
    product_id INT           NOT NULL,
    quantity   INT           NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_order_items         PRIMARY KEY (id),
    CONSTRAINT uq_order_items_product UNIQUE (order_id, product_id),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_order_items_qty     CHECK (quantity > 0),
    CONSTRAINT ck_order_items_price   CHECK (unit_price >= 0)
) ENGINE=InnoDB;
 
-- Historial de estados del pedido (RF-039: "estado actual y historial de avance")
CREATE TABLE order_status_history (
    id         INT      NOT NULL AUTO_INCREMENT,
    order_id   INT      NOT NULL,
    status     ENUM('PENDING','IN_PREPARATION','ON_THE_WAY','DELIVERED','CANCELLED') NOT NULL,
    changed_by INT      NULL,                                  -- NULL si lo hizo el propio sistema/cliente
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_order_status_history PRIMARY KEY (id),
    CONSTRAINT fk_history_order        FOREIGN KEY (order_id)   REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user         FOREIGN KEY (changed_by) REFERENCES users (id)
) ENGINE=InnoDB;
 
CREATE INDEX idx_history_order ON order_status_history (order_id, changed_at);
 
-- =====================================================================
-- 7. PAGOS  (Épica 7: RF-032 a RF-034)
-- =====================================================================
CREATE TABLE payment_methods (
    id        INT         NOT NULL AUTO_INCREMENT,
    name      VARCHAR(40) NOT NULL,
    is_active BOOLEAN     NOT NULL DEFAULT TRUE,               -- RF-032 E1: "no hay métodos disponibles"
    CONSTRAINT pk_payment_methods      PRIMARY KEY (id),
    CONSTRAINT uq_payment_methods_name UNIQUE (name)
) ENGINE=InnoDB;
 
-- Un pedido puede tener varios intentos de pago (uno rechazado y luego otro aprobado)
CREATE TABLE payments (
    id                INT      NOT NULL AUTO_INCREMENT,
    order_id          INT      NOT NULL,
    payment_method_id INT      NOT NULL,
    status            ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payments        PRIMARY KEY (id),
    CONSTRAINT fk_payments_order  FOREIGN KEY (order_id)          REFERENCES orders (id),
    CONSTRAINT fk_payments_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id)
    -- RNF-004: NO se guarda número de tarjeta, solo método y estado.
) ENGINE=InnoDB;
 
-- =====================================================================
-- 8. DOMICILIOS  (Épica 8: RF-035 a RF-038)
-- =====================================================================
CREATE TABLE deliveries (
    id               INT      NOT NULL AUTO_INCREMENT,
    order_id         INT      NOT NULL,
    courier_id       INT      NOT NULL,                        -- usuario con rol COURIER
    status           ENUM('ASSIGNED','ON_THE_WAY','DELIVERED') NOT NULL DEFAULT 'ASSIGNED',
    incident         VARCHAR(300) NULL,                        -- RF-038 E1: cliente no recibe el pedido
    assigned_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at     DATETIME NULL,
    CONSTRAINT pk_deliveries         PRIMARY KEY (id),
    CONSTRAINT uq_deliveries_order   UNIQUE (order_id),        -- un pedido -> un domicilio (0..1)
    CONSTRAINT fk_deliveries_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_deliveries_courier FOREIGN KEY (courier_id) REFERENCES users (id)
) ENGINE=InnoDB;
 
-- =====================================================================
-- 9. FACTURACIÓN  (Épica 10: RF-041, RF-042)
-- =====================================================================
CREATE TABLE invoices (
    id             INT           NOT NULL AUTO_INCREMENT,
    order_id       INT           NOT NULL,
    issued_by      INT           NOT NULL,                     -- encargado de facturación
    invoice_number VARCHAR(20)   NOT NULL,                     -- RF-041: número único
    subtotal       DECIMAL(14,2) NOT NULL,
    tax            DECIMAL(14,2) NOT NULL,
    total          DECIMAL(14,2) NOT NULL,
    issued_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_invoices        PRIMARY KEY (id),
    CONSTRAINT uq_invoices_number UNIQUE (invoice_number),
    CONSTRAINT uq_invoices_order  UNIQUE (order_id),           -- RF-041 E1: no duplicar factura
    CONSTRAINT fk_invoices_order  FOREIGN KEY (order_id)  REFERENCES orders (id),
    CONSTRAINT fk_invoices_user   FOREIGN KEY (issued_by) REFERENCES users (id),
    CONSTRAINT ck_invoices_total  CHECK (total = subtotal + tax)
) ENGINE=InnoDB;
 
-- =====================================================================
-- DATOS DE PRUEBA
-- Nota: los password_hash son valores de ejemplo con formato bcrypt;
--       en la aplicación real los genera Spring Security (BCryptPasswordEncoder).
-- =====================================================================
 
INSERT INTO roles (id, name, description) VALUES
 (1, 'ADMIN',               'Gestiona usuarios, roles, catálogo y reportes'),
 (2, 'SELLER',              'Atiende clientes, ventas presenciales, pedidos y domicilios'),
 (3, 'INVENTORY_MANAGER',   'Controla existencias y movimientos'),
 (4, 'BILLING_MANAGER',     'Genera y consulta facturas'),
 (5, 'CUSTOMER',            'Compra en línea y hace seguimiento de pedidos'),
 (6, 'COURIER',             'Realiza entregas y actualiza su estado');
 
INSERT INTO users (id, name, email, password_hash, phone, is_active) VALUES
 (1, 'Carlos Admin',     'admin@tienda.com',     '$2a$10$EjemploHashBcryptAdmin000000000000000000000000000000', '3001112233', TRUE),
 (2, 'Sofía Vendedora',  'vendedor@tienda.com',  '$2a$10$EjemploHashBcryptSeller00000000000000000000000000000', '3002223344', TRUE),
 (3, 'Mario Inventario', 'inventario@tienda.com','$2a$10$EjemploHashBcryptInvent00000000000000000000000000000', '3003334455', TRUE),
 (4, 'Elena Facturación','facturacion@tienda.com','$2a$10$EjemploHashBcryptBilling0000000000000000000000000000', '3004445566', TRUE),
 (5, 'Diego Domiciliario','domiciliario@tienda.com','$2a$10$EjemploHashBcryptCourier0000000000000000000000000000','3005556677', TRUE),
 (6, 'Ana Torres',       'ana.torres@correo.com','$2a$10$EjemploHashBcryptCustomer1000000000000000000000000000', '3101234567', TRUE),
 (7, 'Laura Gómez',      'laura.gomez@correo.com','$2a$10$EjemploHashBcryptCustomer2000000000000000000000000000','3109876543', TRUE),
 (8, 'Usuario Inactivo', 'inactivo@tienda.com',  '$2a$10$EjemploHashBcryptInactive0000000000000000000000000000', NULL,        FALSE);
 
INSERT INTO user_roles (user_id, role_id) VALUES
 (1,1), (2,2), (3,3), (4,4), (5,6), (6,5), (7,5), (8,2),
 (1,2);   -- el administrador también puede actuar como vendedor (ejemplo de rol múltiple)
 
INSERT INTO customers (id, user_id, document, name, phone, email) VALUES
 (1, 6,    '1012345678', 'Ana Torres',   '3101234567', 'ana.torres@correo.com'),   -- con cuenta
 (2, 7,    NULL,         'Laura Gómez',  '3109876543', 'laura.gomez@correo.com'),  -- con cuenta (sin documento)
 (3, NULL, '79876543',   'Pedro Ramírez','3157778899', 'pedro.ramirez@correo.com');-- registrado por vendedor
 
INSERT INTO categories (id, name) VALUES
 (1, 'Computadores'), (2, 'Celulares'), (3, 'Accesorios'), (4, 'Monitores');
 
INSERT INTO products (id, category_id, code, name, description, price, stock, is_active) VALUES
 (1, 1, 'LAP-001', 'Laptop 14" 16GB RAM',        'Portátil para oficina y estudio',     2500000.00,  9, TRUE),
 (2, 2, 'CEL-001', 'Smartphone 128GB',           'Teléfono inteligente gama media',     1800000.00, 14, TRUE),
 (3, 3, 'ACC-001', 'Audífonos Bluetooth',        'Audífonos inalámbricos con micrófono', 150000.00, 29, TRUE),
 (4, 3, 'ACC-002', 'Mouse inalámbrico',          'Mouse óptico 2.4 GHz',                  45000.00, 47, TRUE),
 (5, 3, 'ACC-003', 'Teclado mecánico',           'Teclado mecánico retroiluminado',      120000.00, 24, TRUE),
 (6, 3, 'ACC-004', 'Funda para celular',         'Funda de silicona',                     35000.00, 38, TRUE),
 (7, 4, 'MON-001', 'Monitor 24" Full HD',        'Monitor IPS 75 Hz',                    650000.00,  8, TRUE),
 (8, 3, 'ACC-005', 'Cable USB descontinuado',    'Producto que ya no se maneja',          12000.00,  0, FALSE);
 
-- Pedidos (se insertan antes de los movimientos porque estos los referencian)
INSERT INTO delivery_addresses (id, address, city, reference) VALUES
 (1, 'Cra 5 # 12-34, Barrio Centro', 'Neiva', 'Casa blanca, portón negro'),
 (2, 'Calle 21 # 8-15, Apto 302',    'Neiva', 'Edificio Mirador'),
 (3, 'Cra 16 # 40-20, Barrio Cándido', 'Neiva', NULL);
 
INSERT INTO orders (id, customer_id, seller_id, delivery_address_id, channel, status, total, created_at) VALUES
 (1, 1, NULL, 1,    'ONLINE',   'DELIVERED',      2590000.00, '2026-10-05 10:15:00'),
 (2, 3, 2,    NULL, 'IN_STORE', 'DELIVERED',       165000.00, '2026-10-07 16:40:00'),
 (3, 2, NULL, 2,    'ONLINE',   'ON_THE_WAY',     1870000.00, '2026-10-20 09:30:00'),
 (4, 1, NULL, 3,    'ONLINE',   'PENDING',         150000.00, '2026-10-25 20:05:00'),
 (5, 3, 2,    NULL, 'IN_STORE', 'CANCELLED',       650000.00, '2026-10-22 11:00:00');
 
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
 (1, 1, 1, 2500000.00), (1, 4, 2,   45000.00),
 (2, 5, 1,  120000.00), (2, 4, 1,   45000.00),
 (3, 2, 1, 1800000.00), (3, 6, 2,   35000.00),
 (4, 3, 1,  150000.00),
 (5, 7, 1,  650000.00);
 
-- Movimientos de inventario (entradas iniciales + salidas por ventas + restitución por cancelación)
-- Resultado: la suma de movimientos coincide con products.stock
INSERT INTO inventory_movements (product_id, user_id, order_id, type, quantity, notes, created_at) VALUES
 (1, 3, NULL, 'ENTRY', 10, 'Compra inicial a proveedor', '2026-10-01 08:00:00'),
 (2, 3, NULL, 'ENTRY', 15, 'Compra inicial a proveedor', '2026-10-01 08:05:00'),
 (3, 3, NULL, 'ENTRY', 30, 'Compra inicial a proveedor', '2026-10-01 08:10:00'),
 (4, 3, NULL, 'ENTRY', 50, 'Compra inicial a proveedor', '2026-10-01 08:15:00'),
 (5, 3, NULL, 'ENTRY', 25, 'Compra inicial a proveedor', '2026-10-01 08:20:00'),
 (6, 3, NULL, 'ENTRY', 40, 'Compra inicial a proveedor', '2026-10-01 08:25:00'),
 (7, 3, NULL, 'ENTRY',  8, 'Compra inicial a proveedor', '2026-10-01 08:30:00'),
 -- Pedido 1
 (1, 3, 1, 'EXIT', 1, 'Venta pedido 1', '2026-10-05 10:16:00'),
 (4, 3, 1, 'EXIT', 2, 'Venta pedido 1', '2026-10-05 10:16:00'),
 -- Pedido 2
 (5, 2, 2, 'EXIT', 1, 'Venta pedido 2', '2026-10-07 16:41:00'),
 (4, 2, 2, 'EXIT', 1, 'Venta pedido 2', '2026-10-07 16:41:00'),
 -- Pedido 3
 (2, 3, 3, 'EXIT', 1, 'Venta pedido 3', '2026-10-20 09:31:00'),
 (6, 3, 3, 'EXIT', 2, 'Venta pedido 3', '2026-10-20 09:31:00'),
 -- Pedido 4
 (3, 3, 4, 'EXIT', 1, 'Venta pedido 4', '2026-10-25 20:06:00'),
 -- Pedido 5: salida y luego restitución por cancelación (RF-030)
 (7, 2, 5, 'EXIT',  1, 'Venta pedido 5',                 '2026-10-22 11:01:00'),
 (7, 2, 5, 'ENTRY', 1, 'Cancelación pedido 5: restitución','2026-10-22 15:30:00');
 
INSERT INTO order_status_history (order_id, status, changed_by, changed_at) VALUES
 (1, 'PENDING',        NULL, '2026-10-05 10:15:00'),
 (1, 'IN_PREPARATION', 2,    '2026-10-05 11:00:00'),
 (1, 'ON_THE_WAY',     5,    '2026-10-05 15:00:00'),
 (1, 'DELIVERED',      5,    '2026-10-05 17:20:00'),
 (2, 'PENDING',        2,    '2026-10-07 16:40:00'),
 (2, 'IN_PREPARATION', 2,    '2026-10-07 16:45:00'),
 (2, 'ON_THE_WAY',     2,    '2026-10-07 16:50:00'),
 (2, 'DELIVERED',      2,    '2026-10-07 16:55:00'),
 (3, 'PENDING',        NULL, '2026-10-20 09:30:00'),
 (3, 'IN_PREPARATION', 2,    '2026-10-20 10:30:00'),
 (3, 'ON_THE_WAY',     5,    '2026-10-26 08:00:00'),
 (4, 'PENDING',        NULL, '2026-10-25 20:05:00'),
 (5, 'PENDING',        2,    '2026-10-22 11:00:00'),
 (5, 'CANCELLED',      2,    '2026-10-22 15:30:00');
 
INSERT INTO payment_methods (id, name, is_active) VALUES
 (1, 'Tarjeta de crédito', TRUE),
 (2, 'Tarjeta débito',     TRUE),
 (3, 'PSE',                TRUE);
 
INSERT INTO payments (order_id, payment_method_id, status, created_at) VALUES
 (1, 1, 'APPROVED', '2026-10-05 10:17:00'),
 (3, 3, 'APPROVED', '2026-10-20 09:33:00'),
 (4, 1, 'REJECTED', '2026-10-25 20:07:00'),   -- primer intento rechazado...
 (4, 3, 'APPROVED', '2026-10-25 20:09:00');   -- ...segundo intento aprobado con otro método
 
INSERT INTO deliveries (order_id, courier_id, status, incident, assigned_at, delivered_at) VALUES
 (1, 5, 'DELIVERED',  NULL, '2026-10-05 11:30:00', '2026-10-05 17:20:00'),
 (3, 5, 'ON_THE_WAY', NULL, '2026-10-20 11:00:00', NULL);
 
INSERT INTO invoices (order_id, issued_by, invoice_number, subtotal, tax, total, issued_at) VALUES
 (1, 4, 'FV-0001', 2590000.00, 492100.00, 3082100.00, '2026-10-05 18:00:00'),  -- IVA 19 %
 (2, 4, 'FV-0002',  165000.00,  31350.00,  196350.00, '2026-10-07 17:10:00'),
 (3, 4, 'FV-0003', 1870000.00, 355300.00, 2225300.00, '2026-10-20 09:45:00');
 
INSERT INTO carts (id, customer_id) VALUES (1, 1), (2, 2);   -- carrito de Ana vacío (ya compró)
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
 (2, 3, 1), (2, 5, 1);
 
-- =====================================================================
-- CONSULTAS DE VERIFICACION (para probar el modelo)
-- =====================================================================
 
-- V1. Existencia de products.stock vs suma de movimientos (deben coincidir)
-- SELECT p.code, p.stock,
--        COALESCE(SUM(CASE m.type WHEN 'ENTRY' THEN m.quantity ELSE -m.quantity END),0) AS calculated_stock
-- FROM products p LEFT JOIN inventory_movements m ON m.product_id = p.id
-- GROUP BY p.id, p.code, p.stock;
 
-- V2. El total de cada pedido debe ser igual a la suma de sus items (RN-008)
-- SELECT o.id, o.total, SUM(oi.quantity * oi.unit_price) AS items_total
-- FROM orders o JOIN order_items oi ON oi.order_id = o.id
-- GROUP BY o.id, o.total;
 
-- V3. Historial de compras de un cliente (RF-040)
-- SELECT o.id, o.created_at, o.status, o.total
-- FROM orders o WHERE o.customer_id = 1 ORDER BY o.created_at DESC;
 
-- V4. Pedidos asignados a un domiciliario (RF-036)
-- SELECT d.order_id, d.status, a.address, a.city
-- FROM deliveries d
-- JOIN orders o ON o.id = d.order_id
-- JOIN delivery_addresses a ON a.id = o.delivery_address_id
-- WHERE d.courier_id = 5;
 
-- V5. Reporte de ventas por día, sin pedidos cancelados (RF-043)
-- SELECT DATE(created_at) AS day, COUNT(*) AS orders, SUM(total) AS sales
-- FROM orders WHERE status <> 'CANCELLED'
-- GROUP BY DATE(created_at);




