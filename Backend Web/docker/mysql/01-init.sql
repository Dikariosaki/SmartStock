-- Eliminar la base de datos si ya existe para una ejecución limpia
DROP DATABASE IF EXISTS proyect_smartstock;

-- Crear la base de datos con soporte completo para caracteres especiales (UTF-8)
CREATE DATABASE proyect_smartstock CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE proyect_smartstock;

-- Tabla para definir los roles de los usuarios (ej: Administrador, Almacenista, etc.)
CREATE TABLE `Rol` (
  `rol_id` INT PRIMARY KEY AUTO_INCREMENT,
  `nombre` VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla para almacenar la información de los usuarios del sistema (quienes inician sesión)
CREATE TABLE `Usuario` (
  `usuario_id` INT PRIMARY KEY AUTO_INCREMENT,
  `rol_id` INT NOT NULL, -- FK a la tabla Rol
  `nombre` VARCHAR(100) NOT NULL,
  `Cedula`INT NOT NULL,
  `email` VARCHAR(150) UNIQUE NOT NULL,
  /*`Cedúla` INT UNIQUE NOT NULL,*/
  `password_hash` VARCHAR(255) NOT NULL,
  `estado` BOOLEAN DEFAULT TRUE, -- Indica si la cuenta de usuario está activa
  INDEX `idx_usuario_email` (`email`), -- Índice para búsquedas rápidas por email
  `telefono` VARCHAR(50)
);

-- Tabla para las categorías de productos (ej: Electrónica, Ropa, Alimentos)
CREATE TABLE `Categoria` (
  `categoria_id` INT PRIMARY KEY AUTO_INCREMENT,
  `nombre` VARCHAR(100) UNIQUE NOT NULL,
  `estado` BOOLEAN DEFAULT TRUE
);

-- Tabla para las subcategorías de productos, relacionadas con una categoría
CREATE TABLE `Subcategoria` (
  `subcategoria_id` INT PRIMARY KEY AUTO_INCREMENT,
  `categoria_id` INT NOT NULL, -- FK a la tabla Categoria
  `nombre` VARCHAR(100) NOT NULL,
  UNIQUE INDEX `idx_subcategoria_nombre` (`categoria_id`, `nombre`), -- Asegura que una subcategoría sea única dentro de su categoría
  `estado` BOOLEAN DEFAULT TRUE
);

-- Tabla para almacenar los detalles de cada producto
CREATE TABLE `Producto` (
  `producto_id` INT PRIMARY KEY AUTO_INCREMENT,
  `subcategoria_id` INT NOT NULL, -- FK a la tabla Subcategoria
  `codigo` VARCHAR(50) UNIQUE NOT NULL, -- Código único del producto (SKU, EAN, etc.)
  `nombre` VARCHAR(150) NOT NULL,
  `descripcion` TEXT,
  `precio_unitario` DECIMAL(12,2) NOT NULL,
  INDEX `idx_producto_nombre` (`nombre`), -- Índice para búsquedas por nombre de producto
  INDEX `idx_producto_codigo` (`codigo`), -- Índice para búsquedas por código de producto
  `estado` BOOLEAN DEFAULT TRUE
);

-- Tabla para Proveedores (no tienen contraseña para acceder al sistema)
CREATE TABLE `Proveedor` (
  `proveedor_id` INT PRIMARY KEY AUTO_INCREMENT,
  `usuario_id` INT,
  /*`nombre` VARCHAR(150) NOT NULL,*/
  `contacto` VARCHAR(150)
  /*`telefono` VARCHAR(50),*/
  /*`email` VARCHAR(150)*/
);

-- Tabla para Clientes (no tienen contraseña para acceder al sistema)
CREATE TABLE `Cliente` (
  `cliente_id` INT PRIMARY KEY AUTO_INCREMENT,
  /*`nombre` VARCHAR(150) NOT NULL,*/
  `usuario_id` INT,
  `contacto` VARCHAR(150),
  `direccion` varchar(150),
  `sucursal` varchar(150)
  
  /*`telefono` VARCHAR(50),
  `email` VARCHAR(150),*/
  
);

-- Tabla para el inventario de productos en distintas ubicaciones
CREATE TABLE `Inventario` (
  `inventario_id` INT PRIMARY KEY AUTO_INCREMENT,
  `producto_id` INT NOT NULL, -- FK a la tabla Producto
  `ubicacion` VARCHAR(100),
  `cantidad` INT NOT NULL DEFAULT 0,
  `punto_reorden` INT NOT NULL DEFAULT 0, -- Cantidad mínima para activar un reorden
  UNIQUE INDEX `idx_inventario_producto_ubicacion` (`producto_id`, `ubicacion`), -- Un producto solo puede estar en una ubicación por inventario
  `estado` BOOLEAN DEFAULT TRUE
);

-- Tabla para registrar las órdenes de reabastecimiento (compras a proveedores)
CREATE TABLE `OrdenReabastecimiento` (
  `orden_id` INT PRIMARY KEY AUTO_INCREMENT,
  `proveedor_id` INT NOT NULL, -- FK a la tabla Proveedor
  `fecha_creacion` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` VARCHAR(50) NOT NULL -- Ej: 'Pendiente', 'En Proceso', 'Completada', 'Cancelada'
);

-- Tabla de detalle para los productos dentro de cada OrdenReabastecimiento
CREATE TABLE `OrdenReabastecimiento_Producto` (
    `orden_id` INT NOT NULL, -- FK a OrdenReabastecimiento
    `producto_id` INT NOT NULL, -- FK a Producto
    `cantidad_pedida` INT NOT NULL,
    `precio_compra_unitario` DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (`orden_id`, `producto_id`),
    `estado` BOOLEAN DEFAULT TRUE
);

-- Tabla para registrar cada movimiento de inventario (entrada/salida)
CREATE TABLE `Movimiento` (
  `movimiento_id` INT PRIMARY KEY AUTO_INCREMENT,
  `inventario_id` INT NOT NULL, -- FK al registro de inventario afectado
  `orden_id` INT NULL, -- FK a OrdenReabastecimiento (si el movimiento es por una orden de entrada)
  `usuario_id` INT NOT NULL, -- FK al usuario que realizó el movimiento
  `proveedor_id` INT NULL, -- FK al Proveedor (si el movimiento es una entrada directa o asociada a uno)
  `cliente_id` INT NULL, -- FK al Cliente (si el movimiento es una salida por venta, por ejemplo)
  `tipo` VARCHAR(10) NOT NULL, -- 'entrada' o 'salida'
  `cantidad` INT NOT NULL,
  `fecha_movimiento` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lote` VARCHAR(100), -- Opcional: para control de lotes
  INDEX `idx_movimiento_fecha` (`fecha_movimiento`), -- Índice para búsquedas por fecha de movimiento
  INDEX `idx_movimiento_tipo` (`tipo`),
 -- Índice para búsquedas por tipo de movimiento
  `estado` BOOLEAN DEFAULT TRUE
);

-- Tabla para almacenar los reportes generados en el sistema
CREATE TABLE `Reporte` (
  `reporte_id` INT PRIMARY KEY AUTO_INCREMENT,
  `titulo` VARCHAR(200) NOT NULL,
  `descripcion` TEXT,
  `evidencia` JSON NULL,
  `fecha_creado` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tipo_reporte` VARCHAR(50) NOT NULL,-- Ej: 'Movimiento', 'Tarea', 'Inventario', 'General'
  `estado` BOOLEAN DEFAULT TRUE
);

-- Tabla de relación N:M entre Reporte y Usuario (quién puede ver/generar qué reportes)
CREATE TABLE `Reporte_Usuario` (
  `reporte_id` INT,
  `usuario_id` INT,
  PRIMARY KEY (`reporte_id`, `usuario_id`)
);

-- Tabla de relación N:M entre Reporte y Movimiento
CREATE TABLE `Reporte_Movimiento` (
  `reporte_id` INT NOT NULL,
  `movimiento_id` INT NOT NULL,
  PRIMARY KEY (`reporte_id`, `movimiento_id`)
);

-- Tabla para las tareas asignadas a usuarios (ej: inventario físico, limpieza)
CREATE TABLE `Tarea` (
  `tarea_id` INT PRIMARY KEY AUTO_INCREMENT,
  `titulo` VARCHAR(200) NOT NULL,
  `descripcion` TEXT,
  `asignado_a` INT, -- FK al usuario asignado
  `fecha_creacion` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_fin` TIMESTAMP NULL, -- Fecha de finalización prevista o real
  `estado` BOOLEAN DEFAULT TRUE
);

-- Tabla de relación N:M entre Reporte y Tarea
CREATE TABLE `Reporte_Tarea` (
  `reporte_id` INT NOT NULL,
  `tarea_id` INT NOT NULL,
  PRIMARY KEY (`reporte_id`, `tarea_id`)
);

-- Tabla de relación N:M entre Tarea y Producto (qué productos están relacionados con una tarea)
CREATE TABLE `Tarea_Producto` (
  `tarea_id` INT NOT NULL, -- FK a la tabla Tarea
  `producto_id` INT NOT NULL, -- FK a la tabla Producto
  `cantidad` INT NOT NULL, -- Cantidad de producto relevante para la tarea
  PRIMARY KEY (`tarea_id`, `producto_id`),
  `estado` BOOLEAN DEFAULT TRUE
);

---

### Definición de Claves Foráneas (Foreign Keys)

-- Relaciones para la tabla Usuario
ALTER TABLE `Usuario` ADD FOREIGN KEY (`rol_id`) REFERENCES `Rol` (`rol_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Subcategoria
ALTER TABLE `Subcategoria` ADD FOREIGN KEY (`categoria_id`) REFERENCES `Categoria` (`categoria_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Producto
ALTER TABLE `Producto` ADD FOREIGN KEY (`subcategoria_id`) REFERENCES `Subcategoria` (`subcategoria_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Inventario
ALTER TABLE `Inventario` ADD FOREIGN KEY (`producto_id`) REFERENCES `Producto` (`producto_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla OrdenReabastecimiento
ALTER TABLE `OrdenReabastecimiento` ADD FOREIGN KEY (`proveedor_id`) REFERENCES `Proveedor` (`proveedor_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla OrdenReabastecimiento_Producto
ALTER TABLE `OrdenReabastecimiento_Producto` ADD FOREIGN KEY (`orden_id`) REFERENCES `OrdenReabastecimiento` (`orden_id`) ON DELETE RESTRICT;
ALTER TABLE `OrdenReabastecimiento_Producto` ADD FOREIGN KEY (`producto_id`) REFERENCES `Producto` (`producto_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Movimiento
ALTER TABLE `Movimiento` ADD FOREIGN KEY (`inventario_id`) REFERENCES `Inventario` (`inventario_id`) ON DELETE RESTRICT;
ALTER TABLE `Movimiento` ADD FOREIGN KEY (`orden_id`) REFERENCES `OrdenReabastecimiento` (`orden_id`) ON DELETE RESTRICT;
ALTER TABLE `Movimiento` ADD FOREIGN KEY (`usuario_id`) REFERENCES `Usuario` (`usuario_id`) ON DELETE RESTRICT;
ALTER TABLE `Movimiento` ADD FOREIGN KEY (`proveedor_id`) REFERENCES `Proveedor` (`proveedor_id`) ON DELETE RESTRICT;
ALTER TABLE `Movimiento` ADD FOREIGN KEY (`cliente_id`) REFERENCES `Cliente` (`cliente_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Reporte_Usuario
ALTER TABLE `Reporte_Usuario` ADD FOREIGN KEY (`reporte_id`) REFERENCES `Reporte` (`reporte_id`) ON DELETE RESTRICT;
ALTER TABLE `Reporte_Usuario` ADD FOREIGN KEY (`usuario_id`) REFERENCES `Usuario` (`usuario_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Reporte_Movimiento
ALTER TABLE `Reporte_Movimiento` ADD FOREIGN KEY (`reporte_id`) REFERENCES `Reporte` (`reporte_id`) ON DELETE RESTRICT;
ALTER TABLE `Reporte_Movimiento` ADD FOREIGN KEY (`movimiento_id`) REFERENCES `Movimiento` (`movimiento_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Tarea
ALTER TABLE `Tarea` ADD FOREIGN KEY (`asignado_a`) REFERENCES `Usuario` (`usuario_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Reporte_Tarea
ALTER TABLE `Reporte_Tarea` ADD FOREIGN KEY (`reporte_id`) REFERENCES `Reporte` (`reporte_id`) ON DELETE RESTRICT;
ALTER TABLE `Reporte_Tarea` ADD FOREIGN KEY (`tarea_id`) REFERENCES `Tarea` (`tarea_id`) ON DELETE RESTRICT;

-- Relaciones para la tabla Tarea_Producto
ALTER TABLE `Tarea_Producto` ADD FOREIGN KEY (`tarea_id`) REFERENCES `Tarea` (`tarea_id`) ON DELETE RESTRICT;
ALTER TABLE `Tarea_Producto` ADD FOREIGN KEY (`producto_id`) REFERENCES `Producto` (`producto_id`) ON DELETE RESTRICT;

ALTER TABLE `cliente` ADD FOREIGN KEY (`usuario_id`) REFERENCES `Usuario` (`usuario_id`) ON DELETE RESTRICT;
ALTER TABLE `Proveedor` ADD FOREIGN KEY (`usuario_id`) REFERENCES `Usuario` (`usuario_id`) ON DELETE RESTRICT;
