package com.proyecto.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodegaDashboardDTO {
    private Integer id;
    private String nombre;
    private String ubicacion;
    private Long totalProducto;
    private Double ocupacion;
    private Long espacio;
    private String encargado;
    private Integer capacidad;
}
