package com.proyecto.logitrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.logitrack.dto.MovimientoDetalleDTO;
import com.proyecto.logitrack.service.MovimientoDetalleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimiento-detalle")
@RequiredArgsConstructor
public class MovimientoDetalleController {

    private final MovimientoDetalleService detalleService;

    @GetMapping("/listar")
    public ResponseEntity<List<MovimientoDetalleDTO>> listarTodos() {
        return ResponseEntity.ok(detalleService.listarTodos());
}


    @PostMapping("/crear")
    public ResponseEntity<MovimientoDetalleDTO> crear(@RequestBody MovimientoDetalleDTO dto) {
        return ResponseEntity.ok(detalleService.crear(dto));
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<MovimientoDetalleDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(detalleService.obtenerPorId(id));
    }

    @GetMapping("/movimiento/{movimientoId}")
    public ResponseEntity<List<MovimientoDetalleDTO>> listarPorMovimiento(@PathVariable Integer movimientoId) {
        return ResponseEntity.ok(detalleService.listarPorMovimiento(movimientoId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MovimientoDetalleDTO> actualizar(@PathVariable Integer id, @RequestBody MovimientoDetalleDTO dto) {
        return ResponseEntity.ok(detalleService.actualizar(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        detalleService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
