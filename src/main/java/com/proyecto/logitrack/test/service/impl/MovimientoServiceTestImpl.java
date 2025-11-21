package com.proyecto.logitrack.test.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.proyecto.logitrack.dto.MovimientoDTO;
import com.proyecto.logitrack.test.service.MovimientoServiceTest;

@Service
public class MovimientoServiceTestImpl implements MovimientoServiceTest {

    @Override
    public List<MovimientoDTO> listarRecientes() {
        // Implementación simulada
        return List.of(); // Retorna una lista vacía por ahora
    }

    @Override
    public List<MovimientoDTO> listarUltimos10() {
        // Implementación simulada
        return List.of(); // Retorna una lista vacía por ahora
    }
}