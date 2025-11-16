package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.entities.Producto;

public interface ProductoService {

    // Listar todos los productos
    List<Producto> getAllProductos();

    // Buscar producto por ID
    Producto getProductoById(Integer id);

    // Crear producto
    Producto createProducto(Producto producto);
    
    // Actualizar producto
    Producto updateProducto(Producto producto);

    // Eliminar producto
    void deleteProducto(Integer id);

    // Buscar productos por nombre
    List<Producto> findByNombre(String nombre);

    // Buscar productos por nombre de categoria
    List<Producto> findByCategoriaNombre(String nombreCategoria);

    // Buscar productos por rango de precios
    List<Producto> findByPrecioRango(Double precioMin, Double precioMax);
}
