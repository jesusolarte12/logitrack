package com.proyecto.logitrack.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.logitrack.entities.Movimiento;
import com.proyecto.logitrack.enums.TipoMovimiento;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {
    // Buscar por tipo de movimiento
    List<Movimiento> findByTipoMovimiento(TipoMovimiento tipoMovimiento);

    //Buscar por rango de fecha
    List<Movimiento> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    // Buscar por id de usuario
    List<Movimiento> findByUsuario_Id(Integer usuarioId);
    
    // Buscar movimientos donde la bodega de origen o destino sea la especificada
    List<Movimiento> findByBodegaOrigen_IdOrBodegaDestino_Id(Integer bodegaOrigenId, Integer bodegaDestinoId);
}
