package com.proyecto.logitrack.test;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.logitrack.dto.MovimientoDTO;
import com.proyecto.logitrack.repository.MovimientoRepository;
import com.proyecto.logitrack.service.MovimientoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimiento/test")
@RequiredArgsConstructor
public class MovimientoControllerTest {
    
    private final MovimientoService movService;

    // Crear Endpoint recientes
    @GetMapping("/recientes")
    public ResponseEntity<List<MovimientoDTO>> listarRecientes(){ 
        List<MovimientoDTO> movimientos = movService.listarMovimientos();
        return ResponseEntity.ok(movimientos);
    }

    // Crear Endpoint reportes
    @GetMapping("/reportes")
    public ResponseEntity<List<MovimientoDTO>> verReportes(){ 
        List<MovimientoDTO> movimientos = movService.listarMovimientos();
        return ResponseEntity.ok(movimientos);
    }
}
