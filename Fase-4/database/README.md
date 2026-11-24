# Base de datos de la Tienda Virtual (Fase 4)

`tienda_virtual.sql` crea la base de datos `virtual_store` en MySQL 8 con:

- las 17 tablas en 3FN, con nombres en inglés y en plural;
- las claves primarias, foráneas y únicas, y las restricciones `CHECK` que aplican las reglas de negocio;
- los datos de prueba (8 usuarios, 8 productos, 5 pedidos en distintos estados, pagos, domicilios, facturas y movimientos de inventario);
- las consultas de verificación V1 a V5, comentadas al final del archivo.

> El script empieza con `DROP DATABASE IF EXISTS virtual_store`: cada vez que se ejecuta, borra y vuelve a crear la base de datos.

## Cómo ejecutarlo

```bash
mysql -u root -p < Fase-4/database/tienda_virtual.sql
```

En MySQL Workbench: **File → Open SQL Script**, seleccionar el archivo y ejecutarlo completo (Ctrl + Shift + Enter). Para comprobar el resultado, `USE virtual_store; SHOW TABLES;` debe mostrar 17 tablas.

## Pruebas realizadas

Se ejecutó en MariaDB 10.11:

- el script corre sin errores;
- V1 (existencia de `products` contra la suma de movimientos) y V2 (total del pedido contra la suma de sus ítems) no muestran diferencias;
- la base de datos rechaza correos duplicados, existencias negativas, cantidades en 0, una segunda factura para el mismo pedido, facturas cuyo total no es subtotal + impuesto y estados de entrega que no existen.
