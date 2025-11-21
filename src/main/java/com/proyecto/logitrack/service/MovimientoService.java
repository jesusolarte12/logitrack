package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.dto.MovimientoDTO;

public interface MovimientoService {

    MovimientoDTO crearMovimiento(MovimientoDTO dto);

    MovimientoDTO obtenerPorId(Integer id);

    List<MovimientoDTO> listarMovimientos();

    List<MovimientoDTO> listarRecientes();

    @Query("SELECT m.tipoMovimiento, COUNT(m) FROM Movimiento m GROUP BY m.tipoMovimiento")
    List<Object[]> contarMovimientosPorTipo();
    List<MovimientoDTO> listarMovimientosPorUsuario(String username);

    void eliminarMovimiento(Integer id);
}
