package com.proyecto.logitrack.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.logitrack.entities.Producto;
import com.proyecto.logitrack.repository.CategoriaRepository;
import com.proyecto.logitrack.repository.ProductoRepository;
import com.proyecto.logitrack.service.ProductoService;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    @Override
    public Producto getProductoById(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto con id " + id + " no encontrado"));
    }

    // ----------------- Crear producto -----------------
    @Override
    public Producto createProducto(Producto producto) {
        // Buscar productos con el mismo nombre y categoría
        List<Producto> productosExistentes = productoRepository.findByNombreContainingIgnoreCase(producto.getNombre());

        for (Producto p : productosExistentes) {
            boolean mismaCategoria = p.getCategoria().getId().equals(producto.getCategoria().getId());

            if (mismaCategoria) {
                // Redirigir al update si se encuentra un producto con igual
                producto.setId(p.getId());
                return updateProducto(producto); 
            }
        }

        // Crear nuevo producto si no existe
        return productoRepository.save(producto);
    }


    // ----------------- Actualizar producto -----------------
    @Override
    public Producto updateProducto(Producto producto) {
        if (producto.getId() == null) {
            throw new RuntimeException("El id del producto es obligatorio para actualizar");
        }

        Producto existingProducto = productoRepository.findById(producto.getId())
                .orElseThrow(() -> new RuntimeException("Producto con id " + producto.getId() + " no encontrado"));

        // Actualizar solo los datos que no son nulos
        if (producto.getNombre() != null) {
            existingProducto.setNombre(producto.getNombre());
        }
        if (producto.getCategoria() != null) {
            existingProducto.setCategoria(producto.getCategoria());
        }
        if (producto.getPrecioCompra() != null) {
            existingProducto.setPrecioCompra(producto.getPrecioCompra());
        }
        if (producto.getPrecioVenta() != null) {
            existingProducto.setPrecioVenta(producto.getPrecioVenta());
        }

        return productoRepository.save(existingProducto);
    }

    @Override
    public void deleteProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<Producto> findByNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    public List<Producto> findByCategoriaNombre(String nombreCategoria) {
        return productoRepository.findByCategoriaNombre(nombreCategoria);
    }

    @Override
    public List<Producto> findByPrecioRango(Double precioMin, Double precioMax) {
        return productoRepository.findByPrecioVentaBetween(precioMin, precioMax);
    }
}
