package com.proyecto.logitrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.logitrack.dto.ProductoDTO;
import com.proyecto.logitrack.entities.Categoria;
import com.proyecto.logitrack.entities.Producto;
import com.proyecto.logitrack.repository.CategoriaRepository;
import com.proyecto.logitrack.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import com.proyecto.logitrack.service.AuditoriaService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/producto")
public class ProductoController {
    
    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository; 
    @Autowired
    private AuditoriaService auditoriaService;
    
    // Listar todos los productos
    @GetMapping("/listar")
    public ResponseEntity<List<Producto>> getAllProductos() {
        List<Producto> productos = productoService.getAllProductos();
        return ResponseEntity.ok(productos);
    }

    // Obtener producto por ID
    @GetMapping("/buscar/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Integer id) {
        Producto producto = productoService.getProductoById(id);
        return ResponseEntity.ok(producto);
    }

    // Crear producto
    @PostMapping("/create")
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        // Buscar categoría una sola vez
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + productoDTO.getCategoriaId()));

        // Mapear DTO a entidad
        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre());
        producto.setCategoria(categoria);
        producto.setPrecioCompra(productoDTO.getPrecioCompra());
        producto.setPrecioVenta(productoDTO.getPrecioVenta());

        Producto nuevoProducto = productoService.createProducto(producto);
        // Registrar auditoría (INSERT)
        auditoriaService.registrar("INSERT", "producto", nuevoProducto.getId(), null, nuevoProducto, null);
        return ResponseEntity.ok(nuevoProducto);
    }



    // Actualizar producto
    @PutMapping("/update/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable Integer id,
                                                    @RequestBody ProductoDTO productoDTO) {
        // obtener estado anterior
        Producto antes = productoService.getProductoById(id);

        Producto producto = new Producto();
        producto.setId(id);
        if (productoDTO.getNombre() != null) {
            producto.setNombre(productoDTO.getNombre());
        }
        if (productoDTO.getCategoriaId() != null) {
            producto.setCategoria(
                categoriaRepository.findById(productoDTO.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + productoDTO.getCategoriaId()))
            );
        }
        if (productoDTO.getPrecioCompra() != null) {
            producto.setPrecioCompra(productoDTO.getPrecioCompra());
        }
        if (productoDTO.getPrecioVenta() != null) {
            producto.setPrecioVenta(productoDTO.getPrecioVenta());
        }

        Producto updatedProducto = productoService.updateProducto(producto);
        // Registrar auditoría (UPDATE)
        auditoriaService.registrar("UPDATE", "producto", updatedProducto.getId(), antes, updatedProducto, null);
        return ResponseEntity.ok(updatedProducto);
    }

    // Eliminar producto
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Integer id) {
        // obtener antes de eliminar
        Producto antes = productoService.getProductoById(id);
        productoService.deleteProducto(id);
        auditoriaService.registrar("DELETE", "producto", id, antes, null, null);
        return ResponseEntity.noContent().build();
    }

    // Buscar por nombre
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        List<Producto> productos = productoService.findByNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    // Buscar por categoría
    @GetMapping("/buscar/categoria")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@RequestParam String categoria) {
        List<Producto> productos = productoService.findByCategoriaNombre(categoria);
        return ResponseEntity.ok(productos);
    }

    // Buscar por rango de precios
    @GetMapping("/buscar/precios")
    public ResponseEntity<List<Producto>> buscarPorRangoPrecios(@RequestParam Double min,
                                                                 @RequestParam Double max) {
        List<Producto> productos = productoService.findByPrecioRango(min, max);
        return ResponseEntity.ok(productos);
    }
}
