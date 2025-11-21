package com.proyecto.logitrack.test.service;

import java.util.List;

import com.proyecto.logitrack.dto.MovimientoDTO;

public interface MovimientoServiceTest {

    List<MovimientoDTO> listarRecientes();

    List<MovimientoDTO> listarUltimos10(); // Método para obtener los últimos 10 movimientos
}
