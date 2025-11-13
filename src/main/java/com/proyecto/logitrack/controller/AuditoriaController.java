package com.proyecto.logitrack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.logitrack.dto.AuditoriaDTO;
import com.proyecto.logitrack.service.AuditoriaService;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping("/listar")
    public ResponseEntity<List<AuditoriaDTO>> listar() {
        // Lista todas las auditorías guardadas
        return ResponseEntity.ok(auditoriaService.listarTodos());
    }
}
