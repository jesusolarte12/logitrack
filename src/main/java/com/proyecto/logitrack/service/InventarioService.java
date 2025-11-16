package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.dto.InventarioDetalleDTO;
import com.proyecto.logitrack.entities.Inventario;

public interface InventarioService {

    // Listar todos los inventarios
    List<Inventario> getAllInventarios();

    // Obtener inventario por ID
    Inventario getInventarioById(Integer id);

    // Crear inventario (si ya existe, suma el stock)
    Inventario createInventario(Inventario inventario);

    // Actualizar inventario (actualiza solo campos no nulos)
    Inventario updateInventario(Inventario inventario);

    // Eliminar inventario
    void deleteInventario(Integer id);

    // Buscar por bodega
    List<Inventario> findByBodega(Integer bodegaId);

    // Buscar por producto
    List<Inventario> findByProducto(Integer productoId);

    // Buscar por bodega y producto
    Inventario findByBodegaAndProducto(Integer bodegaId, Integer productoId);

    // Obtener detalle de inventario por nombre de bodega
    List<InventarioDetalleDTO> getInventarioDetalleByBodegaNombre(String nombreBodega);

    // Obtener todo el inventario detallado
    List<InventarioDetalleDTO> getAllInventarioDetalle();

    // Obtener inventario detallado por username del encargado
    List<InventarioDetalleDTO> getInventarioDetalleByEncargado(String username);
}
