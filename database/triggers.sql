-- 1) Validar y actualizar stock en entrada
CREATE TRIGGER trg_mov_det_entrada
AFTER INSERT ON movimiento_detalle
FOR EACH ROW
BEGIN
    DECLARE tipo VARCHAR(20);
    DECLARE destino INT;

    SELECT tipo_movimiento, bodega_destino_id
    INTO tipo, destino
    FROM movimiento
    WHERE id = NEW.movimiento_id;

    IF tipo = 'ENTRADA' THEN
        UPDATE inventario
        SET stock = stock + NEW.cantidad
        WHERE bodega_id = destino AND producto_id = NEW.producto_id;

        -- Si no existía inventario, créalo
        IF ROW_COUNT() = 0 THEN
            INSERT INTO inventario (bodega_id, producto_id, stock)
            VALUES (destino, NEW.producto_id, NEW.cantidad);
        END IF;
    END IF;
END;

-- 2) Validar stock y descontar en salida
CREATE TRIGGER trg_mov_det_salida
BEFORE INSERT ON movimiento_detalle
FOR EACH ROW
BEGIN
    DECLARE tipo VARCHAR(20);
    DECLARE origen INT;
    DECLARE stock_actual INT;

    SELECT tipo_movimiento, bodega_origen_id
    INTO tipo, origen
    FROM movimiento
    WHERE id = NEW.movimiento_id;

    IF tipo = 'SALIDA' THEN

        SELECT stock INTO stock_actual
        FROM inventario
        WHERE bodega_id = origen AND producto_id = NEW.producto_id;

        IF stock_actual IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'No existe inventario del producto en la bodega origen.';
        END IF;

        IF stock_actual < NEW.cantidad THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Stock insuficiente para la salida.';
        END IF;

        UPDATE inventario
        SET stock = stock - NEW.cantidad
        WHERE bodega_id = origen AND producto_id = NEW.producto_id;
    END IF;
END;


-- 3) Validar y operar stock en transferencia
CREATE TRIGGER trg_mov_det_transferencia
BEFORE INSERT ON movimiento_detalle
FOR EACH ROW
BEGIN
    DECLARE tipo VARCHAR(20);
    DECLARE origen INT;
    DECLARE destino INT;
    DECLARE stock_actual INT;

    SELECT tipo_movimiento, bodega_origen_id, bodega_destino_id
    INTO tipo, origen, destino
    FROM movimiento
    WHERE id = NEW.movimiento_id;

    IF tipo = 'TRANSFERENCIA' THEN

        -- Validar stock origen
        SELECT stock INTO stock_actual
        FROM inventario
        WHERE bodega_id = origen AND producto_id = NEW.producto_id;

        IF stock_actual IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'No existe inventario del producto en la bodega origen.';
        END IF;

        IF stock_actual < NEW.cantidad THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Stock insuficiente para la transferencia.';
        END IF;

        -- Descontar origen
        UPDATE inventario
        SET stock = stock - NEW.cantidad
        WHERE bodega_id = origen AND producto_id = NEW.producto_id;

        -- Sumar destino
        UPDATE inventario
        SET stock = stock + NEW.cantidad
        WHERE bodega_id = destino AND producto_id = NEW.producto_id;

        -- Crear inventario si no existe
        IF ROW_COUNT() = 0 THEN
            INSERT INTO inventario (bodega_id, producto_id, stock)
            VALUES (destino, NEW.producto_id, NEW.cantidad);
        END IF;

    END IF;
END;
