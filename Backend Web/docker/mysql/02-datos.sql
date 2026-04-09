USE proyect_smartstock;

-- 1. Insertar Roles
INSERT INTO Rol (nombre) VALUES ('administrador');
INSERT INTO Rol (nombre) VALUES ('supervisor');
INSERT INTO Rol (nombre) VALUES ('auxiliar');
INSERT INTO Rol (nombre) VALUES ('Proveedor');
INSERT INTO Rol (nombre) VALUES ('Cliente');

-- 2. Insertar Usuarios
-- rol_id 1 es 'administrador', 2 es 'supervisor', 3 es 'auxiliar'
INSERT INTO Usuario (rol_id, nombre, Cedula, email, password_hash, estado, telefono)
VALUES (1, 'Admin Global', 1000337252, 'admin@smartstock.co', md5('admin123'), 1, '3144472227');

INSERT INTO Usuario (rol_id, nombre, Cedula, email, password_hash, estado, telefono)
VALUES (2, 'Andres Pena', 1034567890, 'andres.pena@smartstock.co', md5('SmartStockSup2026'), 1, '3015579032');

INSERT INTO Usuario (rol_id, nombre, Cedula, email, password_hash, estado, telefono)
VALUES (3, 'Camila Rojas', 1045678901, 'camila.rojas@smartstock.co', md5('SmartStockAux2026'), 1, '3026401189');

INSERT INTO Usuario (rol_id, nombre, Cedula, email, password_hash, estado, telefono)
VALUES (4, 'Distribuciones FarmaAndina SAS', 900123456, 'comercial@farmaandina.co', md5('Proveedor2026'), 1, '3205017788');

INSERT INTO Usuario (rol_id, nombre, Cedula, email, password_hash, estado, telefono)
VALUES (5, 'Clinica San Rafael', 901456789, 'abastecimiento@clinicasanrafael.co', md5('Cliente2026'), 1, '3155097720');

-- 3. Insertar CategorÃ­as
INSERT INTO Categoria (nombre, estado) VALUES ('tabletas', 1);
INSERT INTO Categoria (nombre, estado) VALUES ('capsulas', 1);
INSERT INTO Categoria (nombre, estado) VALUES ('inyectables', 1);
INSERT INTO Categoria (nombre, estado) VALUES ('soluciones orales', 1);

-- 4. Insertar SubcategorÃ­as
-- categoria_id 1 es 'capsulas', 2 es 'inyectables', 3 es 'antibioticos', 4 es 'jarabe'
INSERT INTO Subcategoria (categoria_id, nombre, estado) VALUES (2, 'antibioticos', 1);
INSERT INTO Subcategoria (categoria_id, nombre, estado) VALUES (2, 'gastroprotectores', 1);
INSERT INTO Subcategoria (categoria_id, nombre, estado) VALUES (3, 'antiinflamatorios', 1);
INSERT INTO Subcategoria (categoria_id, nombre, estado) VALUES (1, 'analgesicos', 1);
INSERT INTO Subcategoria (categoria_id, nombre, estado) VALUES (4, 'antitusivos', 1);

-- 5. Insertar Productos
-- Asumiendo subcategoria_id:
-- 1: 'Multivitaminico' (de Capsulas)
-- 2: 'Antiinfeccioso' (de Capsulas)
-- 3: 'Dolor y fiebre' (de Inyectables)
-- 4: 'Antiinflamatorio' (de Inyectables)
-- 5: 'Jarabe para la tos' (de Jarabe)
INSERT INTO Producto (subcategoria_id, codigo, nombre, descripcion, precio_unitario, estado)
VALUES (4, 'TAB-ANA-001', 'Paracetamol 500 mg', 'Tableta. Presentacion: caja x 100 unidades', 150.00, 1);

INSERT INTO Producto (subcategoria_id, codigo, nombre, descripcion, precio_unitario, estado)
VALUES (2, 'CAP-GP-001', 'Omeprazol 20 mg', 'Capsula. Presentacion: caja x 100 unidades', 280.00, 1);

INSERT INTO Producto (subcategoria_id, codigo, nombre, descripcion, precio_unitario, estado)
VALUES (3, 'INY-AI-001', 'Diclofenaco 75 mg/3 mL', 'Ampolla inyectable. Presentacion: unidad', 1900.00, 1);

INSERT INTO Producto (subcategoria_id, codigo, nombre, descripcion, precio_unitario, estado)
VALUES (5, 'SOL-AT-001', 'Dextrometorfano 15 mg/5 mL', 'Jarabe antitusivo. Frasco 120 mL', 5200.00, 1);

-- 6. Insertar Proveedores
INSERT INTO Proveedor (usuario_id, contacto)
VALUES (4, 'compras@farmaandina.co');

INSERT INTO Proveedor (usuario_id, contacto)
VALUES (4, 'facturacion@farmaandina.co');

-- 6.1 Insertar Clientes
INSERT INTO Cliente (usuario_id, contacto, direccion, sucursal)
VALUES (5, 'abastecimiento@clinicasanrafael.co', 'Avenida 68 # 22-15, Bogota', 'Clinica San Rafael - Central');

INSERT INTO Cliente (usuario_id, contacto, direccion, sucursal)
VALUES (5, 'farmacia@clinicasanrafael.co', 'Carrera 50 # 80-10, Bogota', 'Clinica San Rafael - Sede Norte');

-- 7. Insertar Inventario
-- Asumiendo producto_id: 1='MEGA-MULTI', 2=Doxiclina, 3=Diclofenaco, 4=Acetominofen
INSERT INTO Inventario (producto_id, ubicacion, cantidad, punto_reorden, estado)
VALUES (1, 'Bodega Central - Zona Seca - Estante A1', 1200, 300, 1);

INSERT INTO Inventario (producto_id, ubicacion, cantidad, punto_reorden, estado)
VALUES (2, 'Bodega Central - Zona Seca - Estante B2', 500, 120, 1);

INSERT INTO Inventario (producto_id, ubicacion, cantidad, punto_reorden, estado)
VALUES (3, 'Bodega Central - Zona Seca - Estante C2', 220, 80, 1);

INSERT INTO Inventario (producto_id, ubicacion, cantidad, punto_reorden, estado)
VALUES (4, 'Bodega Central - Zona Seca - Estante D1', 140, 40, 1);

-- 8. Insertar Ã“rdenes de Reabastecimiento
-- proveedor_id 1 es 'Audifarma', 2 es 'Bayer'
INSERT INTO OrdenReabastecimiento (proveedor_id, fecha_creacion, estado)
VALUES (1, NOW(), 'Pendiente'); -- Orden a Audifarma (ID 1)
INSERT INTO OrdenReabastecimiento (proveedor_id, fecha_creacion, estado)
VALUES (2, NOW(), 'Completada'); -- Orden a Bayer (ID 2)

-- 8.1. Insertar Detalles de Productos en Ã“rdenes de Reabastecimiento
-- Orden 1 (Audifarma) pide Producto 1 (MEGA-MULTI) y Producto 2 (Doxiclina)
INSERT INTO OrdenReabastecimiento_Producto (orden_id, producto_id, cantidad_pedida, precio_compra_unitario, estado)
VALUES (1, 1, 3000, 120.00, 1);

INSERT INTO OrdenReabastecimiento_Producto (orden_id, producto_id, cantidad_pedida, precio_compra_unitario, estado)
VALUES (1, 2, 1500, 180.00, 1);

-- Orden 2 (Bayer) pide Producto 3 (Diclofenaco)
INSERT INTO OrdenReabastecimiento_Producto (orden_id, producto_id, cantidad_pedida, precio_compra_unitario, estado)
VALUES (2, 3, 200, 6800.00, 1);

-- 9. Insertar Movimientos
-- inventario_id: 1=MEGA-MULTI, 2=Doxiclina, 3=Diclofenaco, 4=Acetominofen
-- usuario_id: 1=Admin Global, 2=Maria Inventarios, 3=Pedro Ventas
-- proveedor_id: 1=Audifarma, 2=Bayer
-- cliente_id: 1=Cruz Verde, 2=Farmacia Central
-- Movimiento de Entrada (asociado a Orden 2 de Bayer, por Maria Inventarios)
INSERT INTO Movimiento (inventario_id, orden_id, usuario_id, proveedor_id, cliente_id, tipo, cantidad, fecha_movimiento, lote, estado)
VALUES (3, 2, 2, 2, NULL, 'entrada', 200, NOW(), 'SS-DICLO-2026-01', 1);

-- Movimiento de Salida (por Pedro Ventas, para Cruz Verde)
INSERT INTO Movimiento (inventario_id, orden_id, usuario_id, proveedor_id, cliente_id, tipo, cantidad, fecha_movimiento, lote, estado)
VALUES (4, NULL, 3, NULL, 1, 'salida', 20, NOW(), 'SS-DEXT-2026-01', 1);

-- 10. Insertar Reportes
INSERT INTO Reporte (titulo, descripcion, evidencia, fecha_creado, tipo_reporte, estado)
VALUES (
  'Reporte de Stock Bajo',
  'Productos con cantidad por debajo del punto de reorden.',
  '{"blobKeys":["reportes/inventario/1/stock-bajo-demo.webp"],"observation":null}',
  NOW(),
  'Inventario',
  1
);

INSERT INTO Reporte (titulo, descripcion, evidencia, fecha_creado, tipo_reporte, estado)
VALUES (
  'Analisis de Movimientos Mensuales',
  'Resumen de entradas y salidas por mes.',
  '{"blobKeys":["reportes/movimientos/2/movimientos-demo-1.webp","reportes/movimientos/2/movimientos-demo-2.webp"],"observation":"Comparativo de soportes capturados en bodega."}',
  NOW(),
  'Movimiento',
  1
);

INSERT INTO Reporte (titulo, descripcion, evidencia, fecha_creado, tipo_reporte, estado)
VALUES (
  'Tareas Pendientes Almacen',
  'Listado de tareas asignadas al personal del almacÃ©n.',
  '{"blobKeys":[],"observation":"Seguimiento pendiente de validaciÃ³n visual por supervisor."}',
  NOW(),
  'Tarea',
  1
);

INSERT INTO Reporte (titulo, descripcion, evidencia, fecha_creado, tipo_reporte, estado)
VALUES ('Reporte General', 'Reporte sin evidencia adjunta.', NULL, NOW(), 'General', 1);

-- 11. Insertar Relaciones Reporte_Usuario
-- reporte_id 1='Stock Bajo', 2='Movimientos Mensuales', 3='Tareas Pendientes'
-- usuario_id 1='Admin Global', 2='Maria Inventarios', 3='Pedro Ventas'
INSERT INTO Reporte_Usuario (reporte_id, usuario_id) VALUES (1, 1);
INSERT INTO Reporte_Usuario (reporte_id, usuario_id) VALUES (1, 2);
INSERT INTO Reporte_Usuario (reporte_id, usuario_id) VALUES (2, 1);
INSERT INTO Reporte_Usuario (reporte_id, usuario_id) VALUES (2, 2);
INSERT INTO Reporte_Usuario (reporte_id, usuario_id) VALUES (3, 1);
INSERT INTO Reporte_Usuario (reporte_id, usuario_id) VALUES (3, 3);

-- 12. Insertar Relaciones Reporte_Movimiento
-- Reporte 2 ('AnÃ¡lisis de Movimientos Mensuales') estÃ¡ anclado a Movimientos 1 y 2
INSERT INTO Reporte_Movimiento (reporte_id, movimiento_id) VALUES (2, 1);
INSERT INTO Reporte_Movimiento (reporte_id, movimiento_id) VALUES (1,1);

-- 13. Insertar Tareas
-- Asumiendo usuario_id 2 es 'Maria Inventarios'
INSERT INTO Tarea (titulo, descripcion, asignado_a, fecha_creacion, fecha_fin, estado)
VALUES ('Verificar inventario de inyectables', 'Realizar conteo fÃ­sico de productos inyectables en Bodega Central - Zona Seca.', 2, NOW(), NULL, 1);

INSERT INTO Tarea (titulo, descripcion, asignado_a, fecha_creacion, fecha_fin, estado)
VALUES ('Preparar despacho Clinica San Rafael', 'Empaquetar 20 unidades de Dextrometorfano para la Clinica San Rafael.', 2, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 1);

-- 14. Insertar Relaciones Reporte_Tarea
-- Reporte 3 ('Tareas Pendientes AlmacÃ©n') anclado a Tareas 1 y 2
INSERT INTO Reporte_Tarea (reporte_id, tarea_id) VALUES (3, 1);
INSERT INTO Reporte_Tarea (reporte_id, tarea_id) VALUES (3, 2);

-- 15. Insertar Relaciones Tarea_Producto
-- tarea_id 1='Verificar inventario de inyectables' (relacionada con Producto 3 'Diclofenaco')
-- tarea_id 2='Preparar pedido Cliente Farmacia Central' (relacionada con Producto 4 'Acetominofen')
INSERT INTO Tarea_Producto (tarea_id, producto_id, cantidad, estado) VALUES (1, 3, 0, 1); -- Tarea de verificaciÃ³n (cantidad 0 o N/A para conteo)
INSERT INTO Tarea_Producto (tarea_id, producto_id, cantidad, estado) VALUES (2, 4, 20, 1); -- Tarea de preparaciÃ³n de 20 unidades
