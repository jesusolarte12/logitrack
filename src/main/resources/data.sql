USE LogiTrack;

-- =================== USUARIOS ===================
INSERT INTO usuario (username, password, nombre, email, rol) VALUES
('admin',    'admin_hash',     'Administrador General',     'admin@empresa.com',    'ADMIN'),
('bodega1',  'bodega1_hash',   'Ana Torres',                'ana@empresa.com',      'EMPLEADO'),
('bodega2',  'bodega2_hash',   'Luis Martinez',             'luis@empresa.com',     'EMPLEADO'),
('invitado', 'invitado_hash',  'Invitado de Auditoria',     'audit@empresa.com',    'EMPLEADO');

-- =================== BODEGAS ===================
INSERT INTO bodega (nombre, ubicacion, capacidad, encargado_id) VALUES
('Bodega Central',    'Cra 7 #45-60',            500,   2),
('Bodega Occidente',  'Av. 82 #118-30',          350,   3);

-- =================== PRODUCTOS ===================
INSERT INTO producto (nombre, categoria, stock, precio) VALUES
('Tornillos 5mm',     'Ferreteria',     2000,    100),
('Tuercas 5mm',       'Ferreteria',     2000,    120),
('Taladro',           'Herramienta',     15,   150000),
('Cemento gris',      'Construccion',   500,   34500),
('Pintura 4L',        'Construccion',    35,   52500),
('Lapiz HB',          'Papeleria',      400,      700),
('Cuaderno Argollado','Papeleria',      150,     2800);

-- =================== MOVIMIENTOS ===================
-- Entrada de productos a bodega central (por Ana)
INSERT INTO movimiento (tipo_movimiento, usuario_id, bodega_origen_id, bodega_destino_id)
VALUES ('ENTRADA', 2, NULL, 1);  -- Movimiento ID: 1

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(1, 1, 300),   -- 300 Tornillos 5mm
(1, 2, 200),   -- 200 Tuercas 5mm
(1, 4, 50);    -- 50 Cemento gris

-- Salida de productos desde bodega central (por Luis)
INSERT INTO movimiento (tipo_movimiento, usuario_id, bodega_origen_id, bodega_destino_id)
VALUES ('SALIDA', 3, 1, NULL);   -- Movimiento ID: 2

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(2, 1, 50),    -- 50 Tornillos 5mm
(2, 4, 10),    -- 10 Cemento gris
(2, 6, 20);    -- 20 Lapiz HB

-- Transferencia de productos entre bodegas (Ana transfiere de central a occidente)
INSERT INTO movimiento (tipo_movimiento, usuario_id, bodega_origen_id, bodega_destino_id)
VALUES ('TRANSFERENCIA', 2, 1, 2);   -- Movimiento ID: 3

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(3, 5, 5),    -- 5 Pintura 4L
(3, 7, 40);   -- 40 Cuaderno Argollado

-- Nueva entrada en bodega occidente por Luis
INSERT INTO movimiento (tipo_movimiento, usuario_id, bodega_origen_id, bodega_destino_id)
VALUES ('ENTRADA', 3, NULL, 2);   -- Movimiento ID: 4

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(4, 3, 2),   -- 2 Taladros
(4, 7, 25);  -- 25 Cuaderno Argollado

-- =================== AUDITORIA ===================
INSERT INTO auditoria (tipo_operacion, usuario_id, entidad, valor_antes, valor_despues) VALUES
('INSERT',  2, 'producto',  NULL, '{"nombre":"Tornillos 5mm","categoria":"Ferreteria","stock":2000,"precio":100}'),
('INSERT',  3, 'bodega',    NULL, '{"nombre":"Bodega Occidente","capacidad":350,"ubicacion":"Av. 82 #118-30"}'),
('UPDATE',  2, 'producto',  '{"stock":2000}', '{"stock":2300}'),
('UPDATE',  3, 'producto',  '{"stock":500}',  '{"stock":490}'),
('INSERT',  2, 'movimiento', NULL, '{"tipo_movimiento":"TRANSFERENCIA"}'),
('DELETE',  1, 'producto', '{"nombre":"Papel Bond"}', NULL),
('UPDATE',  2, 'bodega',   '{"capacidad":300}', '{"capacidad":350}');