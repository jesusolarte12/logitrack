CREATE DATABASE IF NOT EXISTS LogiTrack;
USE LogiTrack;

-- 1) Usuarios
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    documento VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    rol ENUM('ADMIN', 'EMPLEADO') NOT NULL
);

-- 2) Bodegas
CREATE TABLE bodega (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150) NOT NULL,
    capacidad INT NOT NULL,
    encargado_id INT NOT NULL,
    FOREIGN KEY (encargado_id) REFERENCES usuario(id)
);

-- 3) Categorías (evita duplicados y errores)
CREATE TABLE categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- 4) Productos (sin stock global)
CREATE TABLE producto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    categoria_id INT NOT NULL,
    precio DECIMAL(12,2) NOT NULL CHECK (precio >= 0),

    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

-- 5) Inventario por bodega
CREATE TABLE inventario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bodega_id INT NOT NULL,
    producto_id INT NOT NULL,
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),

    FOREIGN KEY (bodega_id) REFERENCES bodega(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE,

    UNIQUE KEY ux_bodega_producto (bodega_id, producto_id)
);

-- 6) Movimiento (cabecera)
CREATE TABLE movimiento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento ENUM('ENTRADA','SALIDA','TRANSFERENCIA') NOT NULL,
    usuario_id INT NOT NULL,
    bodega_origen_id INT,
    bodega_destino_id INT,

    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (bodega_origen_id) REFERENCES bodega(id),
    FOREIGN KEY (bodega_destino_id) REFERENCES bodega(id),

    CHECK (
        NOT (
            tipo_movimiento = 'TRANSFERENCIA'
            AND (bodega_origen_id IS NULL
                 OR bodega_destino_id IS NULL
                 OR bodega_origen_id = bodega_destino_id)
        )
    )
);

-- 7) Movimiento detalle
CREATE TABLE movimiento_detalle (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movimiento_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),

    FOREIGN KEY (movimiento_id) REFERENCES movimiento(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id)
);

-- 8) Auditoria
CREATE TABLE auditoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    tipo_operacion ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    usuario_id INT NOT NULL,
    entidad VARCHAR(50) NOT NULL,
    registro_id INT NOT NULL,
    valor_antes TEXT,
    valor_despues TEXT,

    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- 9) Índices
CREATE INDEX idx_producto_categoria ON producto(categoria_id);
CREATE INDEX idx_movimiento_fecha ON movimiento(fecha);
CREATE INDEX idx_movimiento_usuario ON movimiento(usuario_id);
CREATE INDEX idx_auditoria_usuario ON auditoria(usuario_id);
CREATE INDEX idx_mov_det_mov_prod ON movimiento_detalle(movimiento_id, producto_id);
