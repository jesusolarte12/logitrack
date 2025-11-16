package com.proyecto.logitrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodegaDTO {
    private Integer id;

    @NotBlank(message = "El nombre de la bodega es obligatorio")
    
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 150, message = "La ubicación no puede exceder los 150 caracteres")
    private String ubicacion;

    @PositiveOrZero(message = "La capacidad debe ser un número positivo o cero")
    private Integer capacidad;

    @NotNull(message = "El ID del encargado es obligatorio")

    @Positive(message = "El ID del encargado debe ser un número positivo")
    private Integer encargadoId;
}
