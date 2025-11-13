package com.proyecto.logitrack.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovimientoDetalleDTO {

    private Integer id;

    @NotNull(message = "El ID del movimiento es obligatorio")
    private Integer movimientoId;

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;
}
