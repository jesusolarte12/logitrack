package com.proyecto.logitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.logitrack.entities.Bodega;
import com.proyecto.logitrack.dto.BodegaDashboardDTO;

@Repository
public interface BodegaRepository extends JpaRepository<Bodega, Integer> {
    // Buscar bodegas por nombre de bodega 
    List<Bodega> findByNombreContainingIgnoreCase(String nombre);

    // Buscar por nombre del encargado
    @Query("SELECT b FROM Bodega b JOIN b.encargado u " +
       "WHERE LOWER(u.nombre) = LOWER(:nombreEncargado)")
    List<Bodega> findByNombreEncargado(@Param("nombreEncargado") String nombreEncargado);

    @Query("SELECT new com.proyecto.logitrack.dto.BodegaDashboardDTO("
        + "b.id, b.nombre, b.ubicacion, "
        + "COALESCE((SELECT SUM(i.stock) FROM Inventario i WHERE i.bodega = b), 0), "
        + "CASE WHEN COALESCE(b.capacidad,0) > 0 THEN (COALESCE((SELECT SUM(i.stock) FROM Inventario i WHERE i.bodega = b), 0) * 100.0 / COALESCE(b.capacidad,0)) ELSE 0 END, "
        + "(COALESCE(b.capacidad,0) - COALESCE((SELECT SUM(i.stock) FROM Inventario i WHERE i.bodega = b), 0)), "
        + "b.encargado.nombre, b.capacidad) "
        + "FROM Bodega b")
    List<BodegaDashboardDTO> findBodegaDashboard();

    // Dashboard filtrado por username del encargado (para EMPLEADO)
    @Query("SELECT new com.proyecto.logitrack.dto.BodegaDashboardDTO("
        + "b.id, b.nombre, b.ubicacion, "
        + "COALESCE((SELECT SUM(i.stock) FROM Inventario i WHERE i.bodega = b), 0), "
        + "CASE WHEN COALESCE(b.capacidad,0) > 0 THEN (COALESCE((SELECT SUM(i.stock) FROM Inventario i WHERE i.bodega = b), 0) * 100.0 / COALESCE(b.capacidad,0)) ELSE 0 END, "
        + "(COALESCE(b.capacidad,0) - COALESCE((SELECT SUM(i.stock) FROM Inventario i WHERE i.bodega = b), 0)), "
        + "b.encargado.nombre, b.capacidad) "
        + "FROM Bodega b "
        + "WHERE b.encargado.username = :username")
    List<BodegaDashboardDTO> findBodegaDashboardByEncargado(@Param("username") String username);

}
