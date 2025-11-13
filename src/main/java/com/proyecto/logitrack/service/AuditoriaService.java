package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.dto.AuditoriaDTO;

public interface AuditoriaService {
    // Registrar una auditoría: guarda un evento con tipo, entidad, id del registro, valor antes/después y usuario
    AuditoriaDTO registrar(String tipoOperacion, String entidad, Integer registroId, Object valorAntes, Object valorDespues, Integer usuarioId);

    // Listar todas las auditorías (sin paginar)
    List<AuditoriaDTO> listarTodos();
}
