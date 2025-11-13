package com.proyecto.logitrack.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovimientoDTO {

    private Integer id;

    private LocalDateTime fecha;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private String tipoMovimiento; // ENTRADA, SALIDA o TRANSFERENCIA

    @NotNull(message = "Debe especificarse el usuario que realiza el movimiento")
    private Integer usuarioId;

    // Puede ser null si es una entrada
    private Integer bodegaOrigenId;

    // Puede ser null si es una salida
    private Integer bodegaDestinoId;

    @NotEmpty(message = "Debe incluir al menos un producto en el movimiento")
    private List<DetalleMovimientoDTO> detalles;

    // Subclase interna para los productos del movimiento
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class DetalleMovimientoDTO {
        @NotNull(message = "Debe especificarse el producto")
        private Integer productoId;

        @Positive(message = "La cantidad debe ser mayor que cero")
        private Integer cantidad;
    }
}
