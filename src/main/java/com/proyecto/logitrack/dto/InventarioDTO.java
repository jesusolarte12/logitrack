package com.proyecto.logitrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDTO {

	private Integer id;

	@NotNull(message = "La bodega es obligatoria")
	@JsonProperty("bodega_id")
	private Integer bodegaId;

	@NotNull(message = "El producto es obligatorio")
	@JsonProperty("producto_id")
	private Integer productoId;

	@NotNull(message = "El stock es obligatorio")
	@Min(value = 0, message = "El stock no puede ser negativo")
	private Integer stock;
}
