package com.proyecto.logitrack.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.logitrack.dto.AuditoriaDTO;
import com.proyecto.logitrack.entities.Auditoria;
import com.proyecto.logitrack.repository.AuditoriaRepository;
import com.proyecto.logitrack.service.AuditoriaService;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    // Si no se provee usuarioId por el contexto se usará 1 por defecto (asunción razonable).
    private final Integer DEFAULT_USER_ID = 1;

    @Override
    public AuditoriaDTO registrar(String tipoOperacion, String entidad, Integer registroId, Object valorAntes, Object valorDespues, Integer usuarioId) {
        // Construye entidad Auditoria y serializa valores antes/después a JSON cuando sea posible
        Auditoria a = new Auditoria();
        // Fecha/hora del evento
        a.setFechaHora(LocalDateTime.now());
        // Tipo de operación (INSERT/UPDATE/DELETE)
        a.setTipoOperacion(tipoOperacion);
        // Entidad afectada (nombre de la tabla/entidad)
        a.setEntidad(entidad);
        // Id del registro afectado
        a.setRegistroId(registroId);
        // Usuario que realizó la operación (usa valor por defecto si es null)
        a.setUsuarioId(usuarioId != null ? usuarioId : DEFAULT_USER_ID);

        // Valor antes: intenta serializar a JSON, si falla usa toString
        try {
            a.setValorAntes(valorAntes != null ? mapper.writeValueAsString(valorAntes) : null);
        } catch (JsonProcessingException e) {
            a.setValorAntes(valorAntes != null ? valorAntes.toString() : null);
        }
        // Valor después: intenta serializar a JSON, si falla usa toString
        try {
            a.setValorDespues(valorDespues != null ? mapper.writeValueAsString(valorDespues) : null);
        } catch (JsonProcessingException e) {
            a.setValorDespues(valorDespues != null ? valorDespues.toString() : null);
        }

        // Persiste y devuelve DTO
        Auditoria saved = auditoriaRepository.save(a);

        AuditoriaDTO dto = new AuditoriaDTO(saved.getId(), saved.getFechaHora(), saved.getTipoOperacion(), saved.getUsuarioId(), saved.getEntidad(), saved.getRegistroId(), saved.getValorAntes(), saved.getValorDespues());
        return dto;
    }

    @Override
    public List<AuditoriaDTO> listarTodos() {
        List<Auditoria> lista = auditoriaRepository.findAll();
        return lista.stream().map(a -> new AuditoriaDTO(a.getId(), a.getFechaHora(), a.getTipoOperacion(), a.getUsuarioId(), a.getEntidad(), a.getRegistroId(), a.getValorAntes(), a.getValorDespues())).collect(Collectors.toList());
    }
}
