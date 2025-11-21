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
    @GetMapping("/movimientos/recientes")
    public ResponseEntity<List<MovimientoDTO>> listarRecientes(){ 
        return ResponseEntity.ok(movService.listarRecientes());
    }

    // Crear Endpoint reportes
    @GetMapping("/reportes/movimientos")
    public ResponseEntity<Map<String, Object>> reporteMovimientos() {
        return ResponseEntity.ok(movService.obtenerReporteMovimientos());
    }
}
