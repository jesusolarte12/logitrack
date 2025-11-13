package com.proyecto.logitrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.logitrack.service.BodegaService;
import org.springframework.beans.factory.annotation.Autowired;
import com.proyecto.logitrack.service.AuditoriaService;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.proyecto.logitrack.entities.Bodega;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;





@RestController
@AllArgsConstructor
@RequestMapping("/api/bodega")
public class BodegaController {

    private final BodegaService bodegaService;
    @Autowired
    private AuditoriaService auditoriaService;
    
    // Listar todas las bodegas
    @GetMapping("/listar")
    public ResponseEntity<List<Bodega>> getAllBodegas() {
        List<Bodega> bodegas = bodegaService.getAllBodegas();
        return ResponseEntity.ok(bodegas);
    }

    // Buscar bodega por id
    @GetMapping("/buscar/{id}")
    public ResponseEntity<Bodega> getBodegaById(@PathVariable Integer id) {
        Bodega bodega = bodegaService.getBodegaById(id);
        return ResponseEntity.ok(bodega);
    }

    // Crear bodega
    @PostMapping("/create")
    public ResponseEntity<Bodega> createBodega (@Valid @RequestBody Bodega bodega) {

        Bodega newBodega = bodegaService.createBodega(bodega);
        auditoriaService.registrar("INSERT", "bodega", newBodega.getId(), null, newBodega, null);
        return ResponseEntity.ok(newBodega);
    }

    // Actualizar bodega
    @PutMapping("/update/{id}")
    public ResponseEntity<Bodega> updateBodega(@PathVariable Integer id, @RequestBody Bodega bodega) {
        Bodega antes = bodegaService.getBodegaById(id);
        bodega.setId(id);
        Bodega actualizada = bodegaService.updateBodega(bodega);
        auditoriaService.registrar("UPDATE", "bodega", actualizada.getId(), antes, actualizada, null);
        return ResponseEntity.ok(actualizada);
    }
    
    // Eliminar bodega
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBodega(@PathVariable Integer id) {
        Bodega antes = bodegaService.getBodegaById(id);
        bodegaService.deleteBodega(id); // o deleteBodega(id) si renombras el método
        auditoriaService.registrar("DELETE", "bodega", id, antes, null, null);
        return ResponseEntity.noContent().build();
    }

    // Buscar bodega por nombre
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<Bodega>> findByNombre(@RequestParam String nombre) {
        List<Bodega> bodegas = bodegaService.findByNombre(nombre);
        return ResponseEntity.ok(bodegas);
    }

    // Buscar por nombre del encargado
    @GetMapping("/buscar/encargado")
    public ResponseEntity<List<Bodega>> findByEncargado(@RequestParam String encargado) {
        List<Bodega> bodegas = bodegaService.findBynombreEncargado(encargado);
        return ResponseEntity.ok(bodegas);
    }
    
}
