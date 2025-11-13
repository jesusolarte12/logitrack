package com.proyecto.logitrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.logitrack.dto.MovimientoDTO;
import com.proyecto.logitrack.service.MovimientoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    // Crear un nuevo movimiento
    @PostMapping("/crear")
    public ResponseEntity<MovimientoDTO> crearMovimiento(@RequestBody MovimientoDTO dto) {
        MovimientoDTO nuevo = movimientoService.crearMovimiento(dto);
        return ResponseEntity.ok(nuevo);
    }

    // Obtener un movimiento por ID
    @GetMapping("/buscar/{id}")
    public ResponseEntity<MovimientoDTO> obtenerPorId(@PathVariable Integer id) {
        MovimientoDTO movimiento = movimientoService.obtenerPorId(id);
        return ResponseEntity.ok(movimiento);
    }

    // Listar todos los movimientos
    @GetMapping("/listar")
    public ResponseEntity<List<MovimientoDTO>> listarMovimientos() {
        return ResponseEntity.ok(movimientoService.listarMovimientos());
    }

    // Eliminar un movimiento
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Integer id) {
        movimientoService.eliminarMovimiento(id);
        return ResponseEntity.noContent().build();
    }
}
