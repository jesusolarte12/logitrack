USE LogiTrack;

-- ===================== USUARIOS =====================
INSERT INTO usuario (username, password, nombre, cargo, documento, email, rol) VALUES
('admin',  '$2b$12$iimfIRd4UapUcepDh6bl1.V/gRXoVzJmHiZMrLVZzMeTGvWIRhHyW', -- password: admin123
 'Administrador General', 'Administrador', '100000001', 'admin@empresa.com', 'ADMIN'),

('ana',    '$2b$12$mnril1/B7v2k72Rf/o5boe6XedrSef/ZSEf.3YtbmIIUXnTeH66JS', -- password: ana123
 'Ana Torres', 'Operaria', '100000002', 'ana@empresa.com', 'EMPLEADO'),

('luis',   '$2b$12$Z7k/gldg8QigU7lozbx8heGvJsoG71G5eVVBW.AzYVrvlr24gWh.y', -- password: luis123
 'Luis Martinez', 'Auxiliar', '100000003', 'luis@empresa.com', 'EMPLEADO'),

('audit',  '$2b$12$uYAQsH8hSpJNoKFKEgtZNea4r3tX91pX/fhrgmYv2cpd2Nhc/1Yq.', -- password: audit123
 'Auditoria Interna', 'Auditor', '100000004', 'audit@empresa.com', 'EMPLEADO');


-- ===================== CATEGORIAS =====================
INSERT INTO categoria (nombre) VALUES
('Ferreteria'),   -- ID 1
('Herramienta'),  -- ID 2
('Construccion'), -- ID 3
('Papeleria');    -- ID 4


-- ===================== PRODUCTOS =====================
INSERT INTO producto (nombre, categoria_id, precio_compra, precio_venta) VALUES
('Tornillos 5mm',       1, 80,     100),
('Tuercas 5mm',         1, 90,     120),
('Taladro',             2, 120000, 150000),
('Cemento gris 50kg',   3, 29000,  34500),
('Pintura 4L',          3, 44000,  52500),
('Lapiz HB',            4, 500,    700),
('Cuaderno Argollado',  4, 2200,   2800);


-- ===================== BODEGAS =====================
INSERT INTO bodega (nombre, ubicacion, capacidad, encargado_id) VALUES
('Bodega Central',   'Cra 7 #45-60',    500, 2),  -- Ana
('Bodega Occidente', 'Av 82 #118-30',   350, 3);  -- Luis


-- ===================== INVENTARIO =====================
-- INVENTARIO BASE PARA PRUEBAS
-- Bodega Central (ID 1)
INSERT INTO inventario (bodega_id, producto_id, stock) VALUES
(1, 1, 1000), -- Tornillos
(1, 2, 800),  -- Tuercas
(1, 3, 10),   -- Taladro
(1, 4, 200),  -- Cemento
(1, 5, 20),   -- Pintura
(1, 6, 300),  -- Lapiz
(1, 7, 100);  -- Cuadernos

-- Bodega Occidente (ID 2)
INSERT INTO inventario (bodega_id, producto_id, stock) VALUES
(2, 1, 300),
(2, 3, 5),
(2, 4, 100),
(2, 7, 40);


-- ===================== MOVIMIENTOS =====================
-- 1) ENTRADA a Bodega Central por ANA
INSERT INTO movimiento (tipo_movimiento, usuario_id, bodega_destino_id)
VALUES ('ENTRADA', 2, 1); -- ID: 1

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(1, 1, 300),
(1, 2, 200),
(1, 4, 50);


-- 2) SALIDA desde Bodega Central por LUIS
INSERT INTO movimiento (tipo_movimiento, usuario_id, bodega_origen_id)
VALUES ('SALIDA', 3, 1); -- ID: 2

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(2, 1, 50),
(2, 4, 10),
(2, 6, 20);


-- 3) TRANSFERENCIA Central -> Occidente (por ANA)
INSERT INTO movimiento (tipo_movimiento, usuario_id, bodega_origen_id, bodega_destino_id)
VALUES ('TRANSFERENCIA', 2, 1, 2); -- ID: 3

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(3, 5, 5),
(3, 7, 40);


-- 4) ENTRADA a Bodega Occidente (por LUIS)
INSERT INTO movimiento (tipo_movimiento, usuario_id, bodega_destino_id)
VALUES ('ENTRADA', 3, 2); -- ID: 4

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(4, 3, 2),
(4, 7, 25);


-- ===================== AUDITORIA =====================
INSERT INTO auditoria (tipo_operacion, usuario_id, entidad, registro_id, valor_antes, valor_despues) VALUES
('INSERT', 2, 'producto',   1, NULL, '{"nombre":"Tornillos 5mm"}'),
('INSERT', 3, 'bodega',     2, NULL, '{"nombre":"Bodega Occidente"}'),
('UPDATE', 2, 'inventario', 1, '{"stock":1000}', '{"stock":1300}'),
('UPDATE', 3, 'inventario', 4, '{"stock":100}',  '{"stock":90}'),
('INSERT', 2, 'movimiento', 3, NULL, '{"tipo_movimiento":"TRANSFERENCIA"}'),
('DELETE', 1, 'producto',   99, '{"nombre":"Producto Eliminado"}', NULL),
('UPDATE', 2, 'bodega',     2, '{"capacidad":300}', '{"capacidad":350}');
