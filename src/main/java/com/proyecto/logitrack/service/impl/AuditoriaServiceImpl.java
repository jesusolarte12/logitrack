package com.proyecto.logitrack.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;
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

        // Sanitizar objetos para que las relaciones solo aparezcan como ids y no expandan otras tablas
        Object safeAntes = sanitizeForAudit(valorAntes);
        Object safeDespues = sanitizeForAudit(valorDespues);

        // Si ambos son mapas, calcular diff y guardar solo los campos que cambiaron
        if (safeAntes instanceof Map && safeDespues instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapaAntes = (Map<String, Object>) safeAntes;
            @SuppressWarnings("unchecked")
            Map<String, Object> mapaDespues = (Map<String, Object>) safeDespues;

            Map<String, Object> beforeDiff = new HashMap<>();
            Map<String, Object> afterDiff = new HashMap<>();

            Set<String> keys = new HashSet<>();
            keys.addAll(mapaAntes.keySet());
            keys.addAll(mapaDespues.keySet());

            for (String key : keys) {
                Object b = mapaAntes.get(key);
                Object d = mapaDespues.get(key);
                if (!Objects.equals(b, d)) {
                    beforeDiff.put(key, b);
                    afterDiff.put(key, d);
                }
            }

            try {
                a.setValorAntes(!beforeDiff.isEmpty() ? mapper.writeValueAsString(beforeDiff) : null);
            } catch (JsonProcessingException e) {
                a.setValorAntes(!beforeDiff.isEmpty() ? beforeDiff.toString() : null);
            }

            try {
                a.setValorDespues(!afterDiff.isEmpty() ? mapper.writeValueAsString(afterDiff) : null);
            } catch (JsonProcessingException e) {
                a.setValorDespues(!afterDiff.isEmpty() ? afterDiff.toString() : null);
            }

        } else {
            // No son mapas comparables: serializar objetos sanitizados completos
            try {
                a.setValorAntes(safeAntes != null ? mapper.writeValueAsString(safeAntes) : null);
            } catch (JsonProcessingException e) {
                a.setValorAntes(safeAntes != null ? safeAntes.toString() : null);
            }
            try {
                a.setValorDespues(safeDespues != null ? mapper.writeValueAsString(safeDespues) : null);
            } catch (JsonProcessingException e) {
                a.setValorDespues(safeDespues != null ? safeDespues.toString() : null);
            }
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

    // Sanitiza un objeto para auditoría: reemplaza relaciones por sus ids y evita expandir otras tablas.
    // Devuelve un Map/List/primitive adecuado para serializar.
    private Object sanitizeForAudit(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) return obj;
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) obj;
            return sanitizeMapRecursive(m);
        }
        if (obj instanceof Collection) {
            Collection<?> coll = (Collection<?>) obj;
            List<Object> out = new ArrayList<>();
            for (Object e : coll) out.add(sanitizeForAudit(e));
            return out;
        }

        // Intentar convertir POJO a Map y procesarlo
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = mapper.convertValue(obj, Map.class);
            return sanitizeMapRecursive(map);
        } catch (Exception e) {
            // Fallback: devolver toString para evitar referencias a otras entidades
            return obj.toString();
        }
    }

    // Recorre el mapa y reemplaza mapas anidados que tengan 'id' por ese id. Procesa colecciones recursivamente.
    private Map<String, Object> sanitizeMapRecursive(Map<String, Object> map) {
        Map<String, Object> out = new HashMap<>();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String k = en.getKey();
            Object v = en.getValue();
            if (v == null) {
                out.put(k, null);
                continue;
            }
            if (v instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> vm = (Map<String, Object>) v;
                if (vm.containsKey("id") && vm.size() <= 5) {
                    // Si es una referencia probable a otra entidad, solo devolver su id
                    out.put(k, vm.get("id"));
                } else {
                    out.put(k, sanitizeMapRecursive(vm));
                }
            } else if (v instanceof Collection) {
                Collection<?> coll = (Collection<?>) v;
                List<Object> listOut = new ArrayList<>();
                for (Object e : coll) listOut.add(sanitizeForAudit(e));
                out.put(k, listOut);
            } else {
                out.put(k, v);
            }
        }
        return out;
    }
}
