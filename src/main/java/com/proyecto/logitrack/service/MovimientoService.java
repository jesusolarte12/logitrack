package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.dto.MovimientoDTO;

public interface MovimientoService {

    MovimientoDTO crearMovimiento(MovimientoDTO dto);

    MovimientoDTO obtenerPorId(Integer id);

    List<MovimientoDTO> listarMovimientos();

    void eliminarMovimiento(Integer id);
}
