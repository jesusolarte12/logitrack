package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.entities.Bodega;

public interface BodegaService {

    //Listar todas las bodegas
    List<Bodega> getAllBodegas();

    // Buscar bodega por id
    Bodega getBodegaById(Integer id);

    // Crear bodega
    Bodega createBodega(Bodega bodega);

    // Actualizar bodega
    Bodega updateBodega(Bodega bodega);

    // Eliminar bodega
    void deleteBodega(Integer id);

    // Buscar por nombre de bodega
    List<Bodega> findByNombre(String nombre);

    // Buscar por nombre del encargado
    List<Bodega> findBynombreEncargado(String nombreEncargado);

}
