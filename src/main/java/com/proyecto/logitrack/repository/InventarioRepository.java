package com.proyecto.logitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.logitrack.entities.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    // Buscar inventario por bodega y producto
    Optional<Inventario> findByBodegaIdAndProductoId(Integer bodegaId, Integer productoId);

    // Listar inventario por bodega
    List<Inventario> findByBodegaId(Integer bodegaId);

    // Listar inventario por producto
    List<Inventario> findByProductoId(Integer productoId);
}
