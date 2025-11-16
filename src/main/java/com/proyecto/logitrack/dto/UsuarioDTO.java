package com.proyecto.logitrack.dto;

import com.proyecto.logitrack.enums.UsuarioRolEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UsuarioDTO {
    private Integer id;
    private String username;
    private String password;
    private String nombre;
    private String cargo;
    private String documento;
    private String email;
    private UsuarioRolEnum rol;
}