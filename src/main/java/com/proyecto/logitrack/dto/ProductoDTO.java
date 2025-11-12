package com.proyecto.logitrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
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
public class ProductoDTO {

    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 80, message = "El nombre no puede tener más de 80 caracteres")
    private String nombre;

    @NotNull(message = "La categoría es obligatoria")
    @JsonProperty("categoria_id")
    private Integer categoriaId;

    @NotNull(message = "El precio de compra es obligatorio")
    @PositiveOrZero(message = "El precio de compra no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio de compra debe tener hasta 10 dígitos y 2 decimales")
    @JsonProperty("precio_compra")
    private Double precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2, message = "El precio de venta debe tener hasta 10 dígitos y 2 decimales")
    @JsonProperty("precio_venta")
    private Double precioVenta;

    @AssertTrue(message = "El precio de venta debe ser mayor o igual al precio de compra")
    public boolean isPrecioValido() {
        return precioVenta == null || precioCompra == null || precioVenta >= precioCompra;
    }
}
