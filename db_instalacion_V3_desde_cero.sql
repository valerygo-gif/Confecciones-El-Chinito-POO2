-- CONFECCIONES EL CHINITO - V3 / INSTALACION DESDE CERO
-- Ejecutar solo sobre una base NUEVA sin tablas: NO usar para actualizar datos reales.
-- Crea 36 tablas y 10 vistas, sin datos de ejemplo.
-- Sin DROP TABLE, DROP DATABASE ni DELETE; no contiene DEFINER=root@localhost.
-- Para una chinito ya existente utilizar la migracion V1 o el parche V2.

-- CONFECCIONES EL CHINITO - MODELO BASE V1 (MySQL 8.0.16+)
-- Primera version academica para los cuatro modulos. Revisar reglas del negocio antes de operar en produccion.
-- IMPORTANTE: Las existencias y las reservas deben actualizarse mediante transacciones desde el backend.
CREATE DATABASE IF NOT EXISTS chinito CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE chinito;

-- 1. CATALOGO, VARIANTES Y EXISTENCIAS
CREATE TABLE ubicacion (
  id_ubicacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255),
  activa BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE region (
  id_region BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE danza (
  id_danza BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_region BIGINT UNSIGNED NULL,
  nombre VARCHAR(120) NOT NULL,
  CONSTRAINT fk_danza_region FOREIGN KEY (id_region) REFERENCES region(id_region)
) ENGINE=InnoDB;

CREATE TABLE categoria_producto (
  id_categoria BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE producto (
  id_producto BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_categoria BIGINT UNSIGNED NOT NULL,
  id_danza BIGINT UNSIGNED NULL,
  nombre VARCHAR(150) NOT NULL,
  tipo ENUM('PRENDA','ACCESORIO') NOT NULL DEFAULT 'PRENDA',
  descripcion TEXT,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria_producto(id_categoria),
  CONSTRAINT fk_producto_danza FOREIGN KEY (id_danza) REFERENCES danza(id_danza)
) ENGINE=InnoDB;

CREATE TABLE talla (
  id_talla BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  etiqueta VARCHAR(25) NOT NULL UNIQUE,
  orden_visual INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE producto_variante (
  id_variante BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_producto BIGINT UNSIGNED NOT NULL,
  id_talla BIGINT UNSIGNED NOT NULL,
  sku VARCHAR(70) NULL UNIQUE,
  precio_venta_referencial DECIMAL(12,2) NOT NULL DEFAULT 0,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_variante_producto_talla UNIQUE (id_producto,id_talla),
  CONSTRAINT ck_variante_precio CHECK (precio_venta_referencial >= 0),
  CONSTRAINT fk_variante_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
  CONSTRAINT fk_variante_talla FOREIGN KEY (id_talla) REFERENCES talla(id_talla)
) ENGINE=InnoDB;

CREATE TABLE conjunto (
  id_conjunto BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_danza BIGINT UNSIGNED NULL,
  nombre VARCHAR(150) NOT NULL,
  precio_referencial DECIMAL(12,2) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT ck_conjunto_precio CHECK (precio_referencial IS NULL OR precio_referencial >= 0),
  CONSTRAINT fk_conjunto_danza FOREIGN KEY (id_danza) REFERENCES danza(id_danza)
) ENGINE=InnoDB;

-- Define los TIPOS de prendas de un conjunto. La talla exacta se elige en cada pedido.
CREATE TABLE conjunto_componente (
  id_conjunto BIGINT UNSIGNED NOT NULL,
  id_producto BIGINT UNSIGNED NOT NULL,
  unidades_requeridas INT UNSIGNED NOT NULL DEFAULT 1,
  PRIMARY KEY (id_conjunto,id_producto),
  CONSTRAINT ck_cc_cantidad CHECK (unidades_requeridas > 0),
  CONSTRAINT fk_cc_conjunto FOREIGN KEY (id_conjunto) REFERENCES conjunto(id_conjunto),
  CONSTRAINT fk_cc_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE=InnoDB;

CREATE TABLE existencia_producto (
  id_ubicacion BIGINT UNSIGNED NOT NULL,
  id_variante BIGINT UNSIGNED NOT NULL,
  cantidad_fisica INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (id_ubicacion,id_variante),
  CONSTRAINT fk_exist_prod_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES ubicacion(id_ubicacion),
  CONSTRAINT fk_exist_prod_variante FOREIGN KEY (id_variante) REFERENCES producto_variante(id_variante)
) ENGINE=InnoDB;

CREATE TABLE movimiento_inventario (
  id_movimiento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_ubicacion BIGINT UNSIGNED NOT NULL,
  id_variante BIGINT UNSIGNED NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo ENUM('INGRESO','SALIDA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','TRASLADO_ENTRADA','TRASLADO_SALIDA') NOT NULL,
  cantidad INT UNSIGNED NOT NULL,
  motivo VARCHAR(255),
  CONSTRAINT ck_mov_cantidad CHECK (cantidad > 0),
  CONSTRAINT fk_mov_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES ubicacion(id_ubicacion),
  CONSTRAINT fk_mov_variante FOREIGN KEY (id_variante) REFERENCES producto_variante(id_variante)
) ENGINE=InnoDB;

-- 2. CLIENTES, PEDIDOS Y RESERVAS
CREATE TABLE cliente (
  id_cliente BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  tipo_documento ENUM('DNI','RUC','CE','OTRO') NULL,
  numero_documento VARCHAR(20) NULL,
  nombre VARCHAR(160) NOT NULL,
  telefono VARCHAR(30),
  email VARCHAR(140),
  direccion VARCHAR(255),
  CONSTRAINT uq_cliente_documento UNIQUE (tipo_documento,numero_documento)
) ENGINE=InnoDB;

CREATE TABLE pedido (
  id_pedido BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_cliente BIGINT UNSIGNED NOT NULL,
  fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_entrega_comprometida DATE NULL,
  tipo ENUM('REGULAR','PERSONALIZADO','MIXTO') NOT NULL DEFAULT 'REGULAR',
  estado ENUM('REGISTRADO','CONFIRMADO','EN_PRODUCCION','LISTO','ENTREGA_PARCIAL','ENTREGADO','CANCELADO') NOT NULL DEFAULT 'REGISTRADO',
  notas TEXT,
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  INDEX idx_pedido_prioridad (estado,fecha_entrega_comprometida)
) ENGINE=InnoDB;

-- El precio_acordado se guarda como fotografia historica; NO debe depender del precio actual del catalogo.
CREATE TABLE pedido_detalle (
  id_pedido_detalle BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_pedido BIGINT UNSIGNED NOT NULL,
  tipo_item ENUM('INDIVIDUAL','CONJUNTO','PERSONALIZADO') NOT NULL,
  id_conjunto BIGINT UNSIGNED NULL,
  descripcion VARCHAR(255) NOT NULL,
  cantidad INT UNSIGNED NOT NULL,
  precio_unitario_acordado DECIMAL(12,2) NOT NULL,
  referencia_foto_url VARCHAR(500) NULL,
  CONSTRAINT ck_pd_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_pd_precio CHECK (precio_unitario_acordado >= 0),
  CONSTRAINT ck_pd_conjunto CHECK ((tipo_item = 'CONJUNTO' AND id_conjunto IS NOT NULL) OR (tipo_item <> 'CONJUNTO' AND id_conjunto IS NULL)),
  CONSTRAINT fk_pd_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
  CONSTRAINT fk_pd_conjunto FOREIGN KEY (id_conjunto) REFERENCES conjunto(id_conjunto)
) ENGINE=InnoDB;

-- Una linea puede contener varias piezas de distintas tallas.
-- Requerimiento total de cada pieza = pedido_detalle.cantidad * unidades_por_item.
-- Para un personalizado, registrar primero la variante solicitada en el catalogo.
CREATE TABLE pedido_componente (
  id_pedido_componente BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_pedido_detalle BIGINT UNSIGNED NOT NULL,
  id_variante BIGINT UNSIGNED NOT NULL,
  unidades_por_item INT UNSIGNED NOT NULL DEFAULT 1,
  CONSTRAINT ck_pc_cantidad CHECK (unidades_por_item > 0),
  CONSTRAINT fk_pc_detalle FOREIGN KEY (id_pedido_detalle) REFERENCES pedido_detalle(id_pedido_detalle),
  CONSTRAINT fk_pc_variante FOREIGN KEY (id_variante) REFERENCES producto_variante(id_variante)
) ENGINE=InnoDB;

-- Mantener reservas activas mientras no se entregue/cancele el pedido.
CREATE TABLE reserva_stock (
  id_reserva BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_pedido_componente BIGINT UNSIGNED NOT NULL,
  id_ubicacion BIGINT UNSIGNED NOT NULL,
  cantidad INT UNSIGNED NOT NULL,
  estado ENUM('ACTIVA','CONSUMIDA','LIBERADA') NOT NULL DEFAULT 'ACTIVA',
  fecha_reserva DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_reserva_componente_ubicacion UNIQUE (id_pedido_componente,id_ubicacion),
  CONSTRAINT ck_reserva_cantidad CHECK (cantidad > 0),
  CONSTRAINT fk_reserva_pc FOREIGN KEY (id_pedido_componente) REFERENCES pedido_componente(id_pedido_componente),
  CONSTRAINT fk_reserva_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES ubicacion(id_ubicacion)
) ENGINE=InnoDB;

-- 3. PRODUCCION Y TRABAJO A DESTAJO
CREATE TABLE trabajador (
  id_trabajador BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(160) NOT NULL,
  especialidad ENUM('CORTE','COSTURA','ACABADO','VARIOS') NOT NULL DEFAULT 'VARIOS',
  activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE orden_produccion (
  id_orden BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  tipo ENUM('PEDIDO','REPOSICION') NOT NULL DEFAULT 'PEDIDO',
  id_pedido BIGINT UNSIGNED NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_objetivo DATE NOT NULL,
  prioridad_manual TINYINT UNSIGNED NULL COMMENT '1 = mas urgente, 5 = menos urgente',
  estado ENUM('PENDIENTE','EN_PROCESO','TERMINADA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
  indicaciones TEXT,
  CONSTRAINT ck_op_pedido_o_stock CHECK ((tipo='PEDIDO' AND id_pedido IS NOT NULL) OR (tipo='REPOSICION' AND id_pedido IS NULL)),
  CONSTRAINT ck_op_prioridad CHECK (prioridad_manual IS NULL OR prioridad_manual BETWEEN 1 AND 5),
  CONSTRAINT fk_op_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
  INDEX idx_op_vencimiento (estado,fecha_objetivo)
) ENGINE=InnoDB;

CREATE TABLE orden_produccion_detalle (
  id_orden_detalle BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_orden BIGINT UNSIGNED NOT NULL,
  id_variante BIGINT UNSIGNED NOT NULL,
  id_pedido_componente BIGINT UNSIGNED NULL,
  cantidad_a_producir INT UNSIGNED NOT NULL,
  cantidad_terminada INT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT uq_opd_componente UNIQUE (id_orden,id_pedido_componente),
  CONSTRAINT ck_opd_cantidades CHECK (cantidad_a_producir > 0 AND cantidad_terminada <= cantidad_a_producir),
  CONSTRAINT fk_opd_orden FOREIGN KEY (id_orden) REFERENCES orden_produccion(id_orden),
  CONSTRAINT fk_opd_variante FOREIGN KEY (id_variante) REFERENCES producto_variante(id_variante),
  CONSTRAINT fk_opd_pc FOREIGN KEY (id_pedido_componente) REFERENCES pedido_componente(id_pedido_componente)
) ENGINE=InnoDB;

-- Validar una actividad antes de incluirla en el calculo de comisiones.
CREATE TABLE trabajo_realizado (
  id_trabajo BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_orden_detalle BIGINT UNSIGNED NOT NULL,
  id_trabajador BIGINT UNSIGNED NOT NULL,
  etapa ENUM('CORTE','COSTURA','ACABADO') NOT NULL,
  fecha_trabajo DATE NOT NULL,
  cantidad INT UNSIGNED NOT NULL,
  tarifa_por_unidad DECIMAL(12,2) NOT NULL,
  estado ENUM('REPORTADO','VALIDADO','RECHAZADO') NOT NULL DEFAULT 'REPORTADO',
  fecha_validacion DATETIME NULL,
  observaciones VARCHAR(255),
  CONSTRAINT ck_trabajo_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_trabajo_tarifa CHECK (tarifa_por_unidad >= 0),
  CONSTRAINT fk_trabajo_opd FOREIGN KEY (id_orden_detalle) REFERENCES orden_produccion_detalle(id_orden_detalle),
  CONSTRAINT fk_trabajo_trabajador FOREIGN KEY (id_trabajador) REFERENCES trabajador(id_trabajador)
) ENGINE=InnoDB;

CREATE TABLE liquidacion_trabajador (
  id_liquidacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_trabajador BIGINT UNSIGNED NOT NULL,
  periodo_desde DATE NOT NULL,
  periodo_hasta DATE NOT NULL,
  estado ENUM('BORRADOR','PAGADA','ANULADA') NOT NULL DEFAULT 'BORRADOR',
  fecha_pago DATETIME NULL,
  CONSTRAINT ck_liq_periodo CHECK (periodo_hasta >= periodo_desde),
  CONSTRAINT fk_liq_trabajador FOREIGN KEY (id_trabajador) REFERENCES trabajador(id_trabajador)
) ENGINE=InnoDB;

-- UNIQUE(id_trabajo): una actividad solo puede pertenecer a una liquidacion.
CREATE TABLE liquidacion_detalle (
  id_liquidacion BIGINT UNSIGNED NOT NULL,
  id_trabajo BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id_liquidacion,id_trabajo),
  CONSTRAINT uq_liq_trabajo UNIQUE (id_trabajo),
  CONSTRAINT fk_ld_liquidacion FOREIGN KEY (id_liquidacion) REFERENCES liquidacion_trabajador(id_liquidacion),
  CONSTRAINT fk_ld_trabajo FOREIGN KEY (id_trabajo) REFERENCES trabajo_realizado(id_trabajo)
) ENGINE=InnoDB;

-- 4. VENTAS, PAGOS Y ENTREGAS
CREATE TABLE venta (
  id_venta BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_pedido BIGINT UNSIGNED NOT NULL UNIQUE,
  fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado ENUM('ACTIVA','ANULADA') NOT NULL DEFAULT 'ACTIVA',
  CONSTRAINT fk_venta_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
) ENGINE=InnoDB;

-- Un mismo pedido admite muchos pagos; se registra el destino para conciliar efectivo/Yape/bancos.
CREATE TABLE pago (
  id_pago BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_pedido BIGINT UNSIGNED NOT NULL,
  fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  monto DECIMAL(12,2) NOT NULL,
  metodo ENUM('EFECTIVO','YAPE','PLIN','TRANSFERENCIA','DEPOSITO','TARJETA','OTRO') NOT NULL,
  cuenta_receptora VARCHAR(100),
  referencia_operacion VARCHAR(120),
  estado ENUM('CONFIRMADO','ANULADO') NOT NULL DEFAULT 'CONFIRMADO',
  observaciones VARCHAR(255),
  CONSTRAINT ck_pago_monto CHECK (monto > 0),
  CONSTRAINT fk_pago_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
) ENGINE=InnoDB;

CREATE TABLE entrega (
  id_entrega BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_pedido BIGINT UNSIGNED NOT NULL,
  fecha_entrega DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  observaciones VARCHAR(255),
  CONSTRAINT fk_entrega_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
) ENGINE=InnoDB;

CREATE TABLE entrega_detalle (
  id_entrega BIGINT UNSIGNED NOT NULL,
  id_pedido_componente BIGINT UNSIGNED NOT NULL,
  cantidad_entregada INT UNSIGNED NOT NULL,
  PRIMARY KEY (id_entrega,id_pedido_componente),
  CONSTRAINT ck_ed_cantidad CHECK (cantidad_entregada > 0),
  CONSTRAINT fk_ed_entrega FOREIGN KEY (id_entrega) REFERENCES entrega(id_entrega),
  CONSTRAINT fk_ed_pc FOREIGN KEY (id_pedido_componente) REFERENCES pedido_componente(id_pedido_componente)
) ENGINE=InnoDB;

-- 5. PROVEEDORES, COMPRAS, MATERIALES Y COSTOS
CREATE TABLE proveedor (
  id_proveedor BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(160) NOT NULL,
  ruc VARCHAR(11) NULL UNIQUE,
  telefono VARCHAR(30),
  contacto VARCHAR(120),
  observaciones TEXT
) ENGINE=InnoDB;

CREATE TABLE material (
  id_material BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(140) NOT NULL,
  unidad_medida VARCHAR(30) NOT NULL COMMENT 'Ejemplo: metro, unidad, cono',
  costo_referencial DECIMAL(12,2) NOT NULL DEFAULT 0,
  CONSTRAINT ck_material_costo CHECK (costo_referencial >= 0)
) ENGINE=InnoDB;

CREATE TABLE existencia_material (
  id_ubicacion BIGINT UNSIGNED NOT NULL,
  id_material BIGINT UNSIGNED NOT NULL,
  cantidad_fisica DECIMAL(12,3) NOT NULL DEFAULT 0,
  PRIMARY KEY (id_ubicacion,id_material),
  CONSTRAINT ck_em_stock CHECK (cantidad_fisica >= 0),
  CONSTRAINT fk_em_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES ubicacion(id_ubicacion),
  CONSTRAINT fk_em_material FOREIGN KEY (id_material) REFERENCES material(id_material)
) ENGINE=InnoDB;

CREATE TABLE compra (
  id_compra BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_proveedor BIGINT UNSIGNED NOT NULL,
  fecha_compra DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado ENUM('REGISTRADA','RECIBIDA','ANULADA') NOT NULL DEFAULT 'REGISTRADA',
  nro_documento VARCHAR(60),
  CONSTRAINT fk_compra_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor)
) ENGINE=InnoDB;

-- Permite comprar insumos O accesorios/prendas terminadas de proveedores.
CREATE TABLE compra_detalle (
  id_compra_detalle BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_compra BIGINT UNSIGNED NOT NULL,
  id_material BIGINT UNSIGNED NULL,
  id_variante BIGINT UNSIGNED NULL,
  cantidad DECIMAL(12,3) NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  CONSTRAINT ck_compra_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_compra_detalle_precio CHECK (precio_unitario >= 0),
  CONSTRAINT ck_compra_detalle_destino CHECK ((id_material IS NOT NULL AND id_variante IS NULL) OR (id_material IS NULL AND id_variante IS NOT NULL)),
  CONSTRAINT ck_compra_producto_unidades CHECK (id_variante IS NULL OR cantidad = FLOOR(cantidad)),
  CONSTRAINT fk_cd_compra FOREIGN KEY (id_compra) REFERENCES compra(id_compra),
  CONSTRAINT fk_cd_material FOREIGN KEY (id_material) REFERENCES material(id_material),
  CONSTRAINT fk_cd_variante FOREIGN KEY (id_variante) REFERENCES producto_variante(id_variante)
) ENGINE=InnoDB;

CREATE TABLE consumo_material (
  id_consumo BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_orden_detalle BIGINT UNSIGNED NOT NULL,
  id_material BIGINT UNSIGNED NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cantidad DECIMAL(12,3) NOT NULL,
  costo_unitario_registrado DECIMAL(12,2) NOT NULL,
  CONSTRAINT ck_consumo_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_consumo_costo CHECK (costo_unitario_registrado >= 0),
  CONSTRAINT fk_consumo_opd FOREIGN KEY (id_orden_detalle) REFERENCES orden_produccion_detalle(id_orden_detalle),
  CONSTRAINT fk_consumo_material FOREIGN KEY (id_material) REFERENCES material(id_material)
) ENGINE=InnoDB;

-- VISTAS DE CONSULTA (NO reemplazan validaciones transaccionales del backend)
CREATE OR REPLACE SQL SECURITY INVOKER VIEW vw_stock_disponible AS
SELECT ep.id_ubicacion, ep.id_variante, ep.cantidad_fisica,
       COALESCE(r.cantidad_reservada, 0) AS cantidad_reservada,
       ep.cantidad_fisica - COALESCE(r.cantidad_reservada, 0) AS cantidad_disponible
FROM existencia_producto ep
LEFT JOIN (
  SELECT rs.id_ubicacion, pc.id_variante, SUM(rs.cantidad) AS cantidad_reservada
  FROM reserva_stock rs
  JOIN pedido_componente pc ON pc.id_pedido_componente=rs.id_pedido_componente
  WHERE rs.estado='ACTIVA'
  GROUP BY rs.id_ubicacion, pc.id_variante
) r ON r.id_ubicacion=ep.id_ubicacion AND r.id_variante=ep.id_variante;

CREATE OR REPLACE SQL SECURITY INVOKER VIEW vw_saldo_pedidos AS
SELECT p.id_pedido, c.nombre AS cliente,
       COALESCE(d.total_pedido,0) AS total_pedido,
       COALESCE(pa.total_pagado,0) AS total_pagado,
       COALESCE(d.total_pedido,0)-COALESCE(pa.total_pagado,0) AS saldo
FROM pedido p JOIN cliente c ON c.id_cliente=p.id_cliente
LEFT JOIN (
  SELECT id_pedido,SUM(cantidad * precio_unitario_acordado) AS total_pedido
  FROM pedido_detalle GROUP BY id_pedido
) d ON d.id_pedido=p.id_pedido
LEFT JOIN (
  SELECT id_pedido,SUM(monto) AS total_pagado
  FROM pago WHERE estado='CONFIRMADO' GROUP BY id_pedido
) pa ON pa.id_pedido=p.id_pedido;

CREATE OR REPLACE SQL SECURITY INVOKER VIEW vw_comision_validada AS
SELECT tr.id_trabajador, tr.nombre, t.id_trabajo, t.id_orden_detalle,
       t.etapa, t.fecha_trabajo, t.cantidad, t.tarifa_por_unidad,
       t.cantidad * t.tarifa_por_unidad AS importe,
       t.estado
FROM trabajo_realizado t JOIN trabajador tr ON tr.id_trabajador=t.id_trabajador
WHERE t.estado='VALIDADO';

-- ===== MEJORAS V2 + CORRECCIONES SOLICITADAS V3 =====
-- chinito_cambios.sql
-- Migracion sobre chinito_completo.sql (MySQL 8.4). Ejecutar UNA sola vez.
-- Orden: 1) receta  2) venta  3) cliente/DNI  4) stock minimo
--        5) pago.tipo  6) vistas de reportes  7) datos de ejemplo (opcional)
-- =====================================================================
USE `chinito`;

-- ---------------------------------------------------------------------
-- 1. Receta de materiales por variante (costo estimado de produccion)
-- ---------------------------------------------------------------------
CREATE TABLE `producto_material` (
  `id_variante` bigint unsigned NOT NULL,
  `id_material` bigint unsigned NOT NULL,
  `cantidad`    decimal(12,3)   NOT NULL COMMENT 'Cantidad de material por 1 unidad de la variante',
  PRIMARY KEY (`id_variante`,`id_material`),
  KEY `fk_pm_material` (`id_material`),
  CONSTRAINT `fk_pm_variante` FOREIGN KEY (`id_variante`) REFERENCES `producto_variante` (`id_variante`),
  CONSTRAINT `fk_pm_material` FOREIGN KEY (`id_material`) REFERENCES `material` (`id_material`),
  CONSTRAINT `ck_pm_cantidad` CHECK (`cantidad` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 2. Venta: mayorista/minorista y comprobante
-- ---------------------------------------------------------------------
ALTER TABLE `venta`
  ADD COLUMN `tipo_venta`        enum('MAYORISTA','MINORISTA') NOT NULL DEFAULT 'MINORISTA' AFTER `fecha_venta`,
  ADD COLUMN `tipo_comprobante`  enum('BOLETA','FACTURA','NOTA_VENTA') NOT NULL DEFAULT 'NOTA_VENTA' AFTER `tipo_venta`,
  ADD COLUMN `nro_comprobante`   varchar(30) DEFAULT NULL AFTER `tipo_comprobante`,
  ADD UNIQUE KEY `uq_venta_comprobante` (`tipo_comprobante`,`nro_comprobante`);


-- ---------------------------------------------------------------------
-- 3. Cliente: documento OPCIONAL; se valida cuando se proporciona
-- ---------------------------------------------------------------------
-- 'cliente.nombre' sigue siendo obligatorio. Para una venta sin documento,
-- la aplicacion puede utilizar un cliente generico como 'Consumidor final'.
ALTER TABLE `cliente`
  MODIFY `tipo_documento` enum('DNI','RUC','CE','OTRO') NULL DEFAULT NULL,
  MODIFY `numero_documento` varchar(20) NULL DEFAULT NULL,
  ADD CONSTRAINT `ck_cliente_documento_par`
      CHECK ((`tipo_documento` IS NULL AND `numero_documento` IS NULL)
          OR (`tipo_documento` IS NOT NULL AND `numero_documento` IS NOT NULL)),
  ADD CONSTRAINT `ck_cliente_dni`
      CHECK (`tipo_documento` <> 'DNI' OR `numero_documento` REGEXP '^[0-9]{8}$'),
  ADD CONSTRAINT `ck_cliente_ruc`
      CHECK (`tipo_documento` <> 'RUC' OR `numero_documento` REGEXP '^[0-9]{11}$');

-- ---------------------------------------------------------------------
-- 4. Stock minimo (alerta de quiebre)
-- ---------------------------------------------------------------------
ALTER TABLE `material`
  ADD COLUMN `stock_minimo` decimal(12,3) NOT NULL DEFAULT '0.000',
  ADD CONSTRAINT `ck_material_stock_min` CHECK (`stock_minimo` >= 0);

ALTER TABLE `producto_variante`
  ADD COLUMN `stock_minimo` int unsigned NOT NULL DEFAULT '0';

-- ---------------------------------------------------------------------
-- 5. Pago: distinguir adelanto de pago
-- ---------------------------------------------------------------------
ALTER TABLE `pago`
  ADD COLUMN `tipo` enum('ADELANTO','PAGO') NOT NULL DEFAULT 'PAGO' AFTER `fecha_pago`;


-- ---------------------------------------------------------------------
-- 6. Vistas de reportes (solo lectura -> DTOs en Java)
-- ---------------------------------------------------------------------
-- Primero, actualizar las 3 vistas base existentes para mayor portabilidad.
CREATE OR REPLACE SQL SECURITY INVOKER VIEW vw_stock_disponible AS
SELECT ep.id_ubicacion, ep.id_variante, ep.cantidad_fisica,
       COALESCE(r.cantidad_reservada, 0) AS cantidad_reservada,
       ep.cantidad_fisica - COALESCE(r.cantidad_reservada, 0) AS cantidad_disponible
FROM existencia_producto ep
LEFT JOIN (
  SELECT rs.id_ubicacion, pc.id_variante, SUM(rs.cantidad) AS cantidad_reservada
  FROM reserva_stock rs
  JOIN pedido_componente pc ON pc.id_pedido_componente=rs.id_pedido_componente
  WHERE rs.estado='ACTIVA'
  GROUP BY rs.id_ubicacion, pc.id_variante
) r ON r.id_ubicacion=ep.id_ubicacion AND r.id_variante=ep.id_variante;

CREATE OR REPLACE SQL SECURITY INVOKER VIEW vw_saldo_pedidos AS
SELECT p.id_pedido, c.nombre AS cliente,
       COALESCE(d.total_pedido,0) AS total_pedido,
       COALESCE(pa.total_pagado,0) AS total_pagado,
       COALESCE(d.total_pedido,0)-COALESCE(pa.total_pagado,0) AS saldo
FROM pedido p JOIN cliente c ON c.id_cliente=p.id_cliente
LEFT JOIN (
  SELECT id_pedido,SUM(cantidad * precio_unitario_acordado) AS total_pedido
  FROM pedido_detalle GROUP BY id_pedido
) d ON d.id_pedido=p.id_pedido
LEFT JOIN (
  SELECT id_pedido,SUM(monto) AS total_pagado
  FROM pago WHERE estado='CONFIRMADO' GROUP BY id_pedido
) pa ON pa.id_pedido=p.id_pedido;

CREATE OR REPLACE SQL SECURITY INVOKER VIEW vw_comision_validada AS
SELECT tr.id_trabajador, tr.nombre, t.id_trabajo, t.id_orden_detalle,
       t.etapa, t.fecha_trabajo, t.cantidad, t.tarifa_por_unidad,
       t.cantidad * t.tarifa_por_unidad AS importe,
       t.estado
FROM trabajo_realizado t JOIN trabajador tr ON tr.id_trabajador=t.id_trabajador
WHERE t.estado='VALIDADO';

-- Luego, crear las vistas nuevas de la version 2.

-- Reporte de ventas: una fila por venta activa
CREATE OR REPLACE SQL SECURITY INVOKER VIEW `vw_reporte_ventas` AS
SELECT v.id_venta, v.fecha_venta, v.tipo_venta, v.tipo_comprobante, v.nro_comprobante,
       s.id_pedido, s.cliente, s.total_pedido, s.total_pagado, s.saldo
FROM venta v
JOIN vw_saldo_pedidos s ON s.id_pedido = v.id_pedido
WHERE v.estado = 'ACTIVA';

-- Reporte de produccion: avance por linea de orden
CREATE OR REPLACE SQL SECURITY INVOKER VIEW `vw_reporte_produccion` AS
SELECT o.id_orden, o.tipo, o.estado, o.fecha_objetivo,
       d.id_orden_detalle, pr.nombre AS producto, t.etiqueta AS talla,
       d.cantidad_a_producir, d.cantidad_terminada,
       (d.cantidad_a_producir - d.cantidad_terminada) AS pendiente,
       ROUND(d.cantidad_terminada * 100 / d.cantidad_a_producir, 1) AS avance_pct
FROM orden_produccion_detalle d
JOIN orden_produccion o   ON o.id_orden = d.id_orden
JOIN producto_variante pv ON pv.id_variante = d.id_variante
JOIN producto pr          ON pr.id_producto = pv.id_producto
JOIN talla t              ON t.id_talla = pv.id_talla;

-- Costo estimado de materiales por variante (segun receta) y margen
CREATE OR REPLACE SQL SECURITY INVOKER VIEW `vw_costo_estimado_variante` AS
SELECT pv.id_variante, pv.sku, pr.nombre AS producto, t.etiqueta AS talla,
       pv.precio_venta_referencial,
       COALESCE(SUM(pm.cantidad * m.costo_referencial), 0) AS costo_materiales_estimado,
       pv.precio_venta_referencial - COALESCE(SUM(pm.cantidad * m.costo_referencial), 0) AS margen_estimado
FROM producto_variante pv
JOIN producto pr ON pr.id_producto = pv.id_producto
JOIN talla t     ON t.id_talla = pv.id_talla
LEFT JOIN producto_material pm ON pm.id_variante = pv.id_variante
LEFT JOIN material m           ON m.id_material = pm.id_material
GROUP BY pv.id_variante, pv.sku, pr.nombre, t.etiqueta, pv.precio_venta_referencial;

-- Costo real por linea de orden: materiales consumidos + mano de obra validada
CREATE OR REPLACE SQL SECURITY INVOKER VIEW `vw_costo_real_produccion` AS
SELECT d.id_orden_detalle, d.id_orden, d.id_variante, d.cantidad_terminada,
       COALESCE(c.costo_materiales, 0) AS costo_materiales,
       COALESCE(w.costo_mano_obra, 0)  AS costo_mano_obra,
       COALESCE(c.costo_materiales, 0) + COALESCE(w.costo_mano_obra, 0) AS costo_total
FROM orden_produccion_detalle d
LEFT JOIN (SELECT id_orden_detalle, SUM(cantidad * costo_unitario_registrado) AS costo_materiales
           FROM consumo_material GROUP BY id_orden_detalle) c ON c.id_orden_detalle = d.id_orden_detalle
LEFT JOIN (SELECT id_orden_detalle, SUM(cantidad * tarifa_por_unidad) AS costo_mano_obra
           FROM trabajo_realizado WHERE estado = 'VALIDADO'
           GROUP BY id_orden_detalle) w ON w.id_orden_detalle = d.id_orden_detalle;

-- Alertas de stock bajo
CREATE OR REPLACE SQL SECURITY INVOKER VIEW `vw_alerta_stock_producto` AS
SELECT pv.id_variante, pv.sku, pv.stock_minimo,
       COALESCE(SUM(sd.cantidad_disponible), 0) AS cantidad_disponible
FROM producto_variante pv
LEFT JOIN vw_stock_disponible sd ON sd.id_variante = pv.id_variante
WHERE pv.activo = 1
GROUP BY pv.id_variante, pv.sku, pv.stock_minimo
HAVING COALESCE(SUM(sd.cantidad_disponible), 0) < pv.stock_minimo;

CREATE OR REPLACE SQL SECURITY INVOKER VIEW `vw_alerta_stock_material` AS
SELECT m.id_material, m.nombre, m.unidad_medida, m.stock_minimo,
       COALESCE(SUM(em.cantidad_fisica), 0) AS cantidad_fisica
FROM material m
LEFT JOIN existencia_material em ON em.id_material = m.id_material
GROUP BY m.id_material, m.nombre, m.unidad_medida, m.stock_minimo
HAVING COALESCE(SUM(em.cantidad_fisica), 0) < m.stock_minimo;

-- ---------------------------------------------------------------------
-- 6b. Historial de movimientos de materiales (equivale a movimiento_inventario)
-- ---------------------------------------------------------------------
CREATE TABLE `movimiento_material` (
  `id_movimiento` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_ubicacion`  bigint unsigned NOT NULL,
  `id_material`   bigint unsigned NOT NULL,
  `fecha`         datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tipo`          enum('INGRESO','SALIDA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','TRASLADO_ENTRADA','TRASLADO_SALIDA') NOT NULL,
  `cantidad`      decimal(12,3) NOT NULL,
  `motivo`        varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `fk_mm_ubicacion` (`id_ubicacion`),
  KEY `fk_mm_material` (`id_material`),
  CONSTRAINT `fk_mm_ubicacion` FOREIGN KEY (`id_ubicacion`) REFERENCES `ubicacion` (`id_ubicacion`),
  CONSTRAINT `fk_mm_material` FOREIGN KEY (`id_material`) REFERENCES `material` (`id_material`),
  CONSTRAINT `ck_mm_cantidad` CHECK (`cantidad` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 6c. Precios especiales por cliente (clientes recurrentes)
-- ---------------------------------------------------------------------
CREATE TABLE `precio_cliente` (
  `id_cliente`      bigint unsigned NOT NULL,
  `id_variante`     bigint unsigned NOT NULL,
  `precio_especial` decimal(12,2) NOT NULL,
  `fecha_registro`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `observaciones`   varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_cliente`,`id_variante`),
  KEY `fk_pcli_variante` (`id_variante`),
  CONSTRAINT `fk_pcli_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `fk_pcli_variante` FOREIGN KEY (`id_variante`) REFERENCES `producto_variante` (`id_variante`),
  CONSTRAINT `ck_pcli_precio` CHECK (`precio_especial` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Precio aplicable: el especial del cliente si existe, si no el referencial
CREATE OR REPLACE SQL SECURITY INVOKER VIEW `vw_precio_aplicable` AS
SELECT c.id_cliente, pv.id_variante, pv.sku,
       pv.precio_venta_referencial,
       pc.precio_especial,
       COALESCE(pc.precio_especial, pv.precio_venta_referencial) AS precio_aplicable
FROM cliente c
CROSS JOIN producto_variante pv
LEFT JOIN precio_cliente pc ON pc.id_cliente = c.id_cliente AND pc.id_variante = pv.id_variante
WHERE pv.activo = 1;

-- ---------------------------------------------------------------------
-- 6d. Precios por cantidad (tramos configurables para cada talla/variante)
-- ---------------------------------------------------------------------
-- Cada fila indica el precio desde una cantidad minima.
-- Por ejemplo: 1, 6 y 24 como puntos de corte SI la empresa los decide.
-- No se guardan intervalos solapados: se elige el mayor minimo <= cantidad.
-- El precio acordado FINAL se mantiene en pedido_detalle.precio_unitario_acordado.
CREATE TABLE `precio_escalonado` (
  `id_variante` bigint unsigned NOT NULL,
  `cantidad_minima` int unsigned NOT NULL,
  `precio_unitario` decimal(12,2) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_variante`, `cantidad_minima`),
  CONSTRAINT `fk_pe_variante` FOREIGN KEY (`id_variante`)
    REFERENCES `producto_variante` (`id_variante`),
  CONSTRAINT `ck_pe_cantidad` CHECK (`cantidad_minima` > 0),
  CONSTRAINT `ck_pe_precio` CHECK (`precio_unitario` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
