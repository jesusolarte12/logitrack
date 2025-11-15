package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.dto.UsuarioDTO;
import com.proyecto.logitrack.entities.Usuario;

public interface UsuarioService {

    // Lista todos los usuarios
    List<Usuario> listarUsuarios();

    // Obtiene un usuario por su nombre de usuario
    UsuarioDTO obtenerPorUsername(String username);

    // Crea un nuevo usuario
    Usuario crearUsuario(UsuarioDTO usuarioDTO);

    // Obtiene un usuario por su documento
    UsuarioDTO obtenerPorDocumento(String documento);

    // Elimina un usuario por su documento
    void eliminarPorDocumento(String documento);

    // Actualizar usuarios
    UsuarioDTO actualizarUsuarioParcial(String documento, UsuarioDTO datosActualizados);

}
