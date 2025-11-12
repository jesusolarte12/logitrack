package com.proyecto.logitrack.service;

import com.proyecto.logitrack.dto.UsuarioDTO;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.repository.UsuarioRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //Metodo para listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    //Metodo crearUsuario para crear un nuevo usuario
    public Usuario crearUsuario(UsuarioDTO usuarioDTO) {
        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setPassword(usuarioDTO.getPassword());
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setCargo(usuarioDTO.getCargo());
        usuario.setDocumento(usuarioDTO.getDocumento());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setRol(usuarioDTO.getRol());

        return usuarioRepository.save(usuario);
    }

    //Metodo para buscar por el documento del usuario
    public UsuarioDTO obtenerPorDocumento(String documento) {
        Usuario usuario = usuarioRepository.findByDocumento(documento)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con documento: " + documento));

        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setUsername(usuario.getUsername());
        usuarioDTO.setPassword(usuario.getPassword());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setCargo(usuario.getCargo());
        usuarioDTO.setDocumento(usuario.getDocumento());
        usuarioDTO.setEmail(usuario.getEmail());
        usuarioDTO.setRol(usuario.getRol());
        return usuarioDTO;
    }

    //Metodo para eliminar por Documento
    public void eliminarPorDocumento(String documento) {
        Usuario usuario = usuarioRepository.findByDocumento(documento)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el documento: " + documento));

        usuarioRepository.delete(usuario);
    }

    public UsuarioDTO actualizarUsuarioParcial(String documento, UsuarioDTO datosActualizados) {
        Usuario usuarioExistente = usuarioRepository.findByDocumento(documento)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con documento: " + documento));

        usuarioRepository.actualizarUsuarioParcial(
            documento,
            datosActualizados.getUsername(),
            datosActualizados.getPassword(),
            datosActualizados.getNombre(),
            datosActualizados.getCargo(),
            datosActualizados.getEmail(),
            datosActualizados.getRol()
        );

        Usuario usuarioActualizado = usuarioRepository.findByDocumento(documento)
            .orElseThrow(() -> new RuntimeException("Error al actualizar el usuario"));

        UsuarioDTO dto = new UsuarioDTO();
        dto.setUsername(usuarioActualizado.getUsername());
        dto.setPassword(usuarioActualizado.getPassword());
        dto.setNombre(usuarioActualizado.getNombre());
        dto.setCargo(usuarioActualizado.getCargo());
        dto.setDocumento(usuarioActualizado.getDocumento());
        dto.setEmail(usuarioActualizado.getEmail());
        dto.setRol(usuarioActualizado.getRol());

        return dto;
    }
}
