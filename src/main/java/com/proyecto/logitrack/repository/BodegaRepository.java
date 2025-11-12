package com.proyecto.logitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.logitrack.entities.Bodega;

@Repository
public interface BodegaRepository extends JpaRepository<Bodega, Integer> {
    // Buscar bodegas por nombre de bodega 
    List<Bodega> findByNombreContainingIgnoreCase(String nombre);

    // Buscar por nombre del encargado
    @Query("SELECT b FROM Bodega b JOIN b.encargado u " +
       "WHERE LOWER(u.nombre) = LOWER(:nombreEncargado)")
    List<Bodega> findByNombreEncargado(@Param("nombreEncargado") String nombreEncargado);

}
