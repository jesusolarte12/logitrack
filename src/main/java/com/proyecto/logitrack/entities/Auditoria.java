package com.proyecto.logitrack.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auditoria")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Identificador único del registro de auditoría
    private Integer id;

    @Column(name = "fecha_hora")
    // Fecha y hora en que se realizó la operación
    private LocalDateTime fechaHora;

    @Column(name = "tipo_operacion")
    // Tipo de operación: INSERT, UPDATE o DELETE
    private String tipoOperacion;

    @Column(name = "usuario_id")
    // Id del usuario que realizó la operación (FK a tabla usuario)
    private Integer usuarioId;

    @Column(name = "entidad")
    // Nombre de la entidad afectada (ej. "producto", "inventario")
    private String entidad;

    @Column(name = "registro_id")
    // Id del registro afectado en la entidad
    private Integer registroId;

    @Column(name = "valor_antes", columnDefinition = "TEXT")
    // Valor serializado antes del cambio (JSON o texto)
    private String valorAntes;

    @Column(name = "valor_despues", columnDefinition = "TEXT")
    // Valor serializado después del cambio (JSON o texto)
    private String valorDespues;
}
