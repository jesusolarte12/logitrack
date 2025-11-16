package com.proyecto.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDetalleDTO {
    private Integer inventarioId;
    private Integer productoId;
    private String nombreBodega;
    private String nombreProducto;
    private Double precioVenta;
    private Double precioCompra;
    private String categoriaProducto;
    private Integer stock;
}
