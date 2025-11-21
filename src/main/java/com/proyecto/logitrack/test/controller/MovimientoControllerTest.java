package com.proyecto.logitrack.test.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyecto.logitrack.dto.MovimientoDTO;
import com.proyecto.logitrack.test.service.MovimientoServiceTest;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoControllerTest {

    private final MovimientoServiceTest movimientoServiceTest;

    @GetMapping("/recientes")
    public ResponseEntity<List<MovimientoDTO>> listarRecientes() {
        List<MovimientoDTO> movimientosRecientes = movimientoServiceTest.listarUltimos10();
        return ResponseEntity.ok(movimientosRecientes);
    }
}
