package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.dto.MovimientoDetalleDTO;

public interface MovimientoDetalleService {

    List<MovimientoDetalleDTO> listarTodos();
    
    MovimientoDetalleDTO crear(MovimientoDetalleDTO dto);

    MovimientoDetalleDTO obtenerPorId(Integer id);

    List<MovimientoDetalleDTO> listarPorMovimiento(Integer movimientoId);

    MovimientoDetalleDTO actualizar(Integer id, MovimientoDetalleDTO dto);

    void eliminar(Integer id);
}
