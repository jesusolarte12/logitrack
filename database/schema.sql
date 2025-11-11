create database if not exists LogiTrack;

USE LogiTrack;

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    rol ENUM('ADMIN', 'EMPLEADO') NOT NULL
);

CREATE TABLE bodega (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150) NOT NULL,
    capacidad INT NOT NULL,
    encargado_id INT NOT NULL,
    FOREIGN KEY (encargado_id) REFERENCES usuario(id)
);

CREATE TABLE producto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    stock INT DEFAULT 0 CHECK (stock >= 0),
    precio DECIMAL(12,2) NOT NULL CHECK (precio >= 0)
);

CREATE TABLE movimiento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento ENUM('ENTRADA','SALIDA','TRANSFERENCIA') NOT NULL,
    usuario_id INT NOT NULL,
    bodega_origen_id INT,
    bodega_destino_id INT,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (bodega_origen_id) REFERENCES bodega(id),
    FOREIGN KEY (bodega_destino_id) REFERENCES bodega(id)
);

CREATE TABLE movimiento_detalle (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movimiento_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    FOREIGN KEY (movimiento_id) REFERENCES movimiento(id),
    FOREIGN KEY (producto_id) REFERENCES producto(id)
);

CREATE TABLE auditoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    tipo_operacion ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    usuario_id INT NOT NULL,
    entidad VARCHAR(50) NOT NULL,
    valor_antes TEXT,
    valor_despues TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Indices para eficiencia
CREATE INDEX idx_producto_categoria ON producto(categoria);
CREATE INDEX idx_movimiento_fecha ON movimiento(fecha);
CREATE INDEX idx_movimiento_usuario ON movimiento(usuario_id);
CREATE INDEX idx_auditoria_usuario ON auditoria(usuario_id);
