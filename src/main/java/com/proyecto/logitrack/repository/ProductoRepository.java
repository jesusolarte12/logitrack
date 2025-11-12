package com.proyecto.logitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.logitrack.entities.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>{
    // Buscar productos por nombre
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar por nombre de categoria
    @Query("SELECT P FROM Producto P JOIN P.categoria C WHERE LOWER(C.nombre) = LOWER(:nombreCategoria)")
    List<Producto> findByCategoriaNombre(@Param("nombreCategoria") String nombreCategoria);

    // Buscar por rango de precios
    List<Producto> findByPrecioVentaBetween(Double precioMin, Double precioMax);

}
