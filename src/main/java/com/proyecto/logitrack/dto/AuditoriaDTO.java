package com.proyecto.logitrack.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaDTO {
    // Identificador único del registro de auditoría
    private Integer id;

    // Fecha y hora en que se registró la auditoría
    private LocalDateTime fechaHora;

    // Tipo de operación auditada: INSERT, UPDATE, DELETE
    private String tipoOperacion;

    // Id del usuario que realizó la operación
    private Integer usuarioId;

    // Nombre de la entidad afectada
    private String entidad;

    // Id del registro afectado en la entidad
    private Integer registroId;

    // Valor antes de la operación (serializado)
    private String valorAntes;

    // Valor después de la operación (serializado)
    private String valorDespues;
}
