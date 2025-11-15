package com.proyecto.logitrack.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.logitrack.dto.UsuarioDTO;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.repository.UsuarioRepository;
import com.proyecto.logitrack.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Servicio que maneja operaciones relacionadas con la entidad Usuario.
     * Implementa la interfaz `UsuarioService` y delega operaciones CRUD al repository.
     */

    @Override
    public List<Usuario> listarUsuarios() {
        // Devuelve todos los usuarios persistidos en la base de datos
        return usuarioRepository.findAll();
    }

    @Override
    public UsuarioDTO obtenerPorUsername(String username) {
        // Busca la entidad Usuario por su username; si no existe lanza RuntimeException
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con username: " + username));

        // Mapea la entidad a DTO para evitar exponer la entidad directamente
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

    @Override
    public Usuario crearUsuario(UsuarioDTO usuarioDTO) {
        // Crea una nueva entidad Usuario a partir del DTO recibido y la persiste
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

    @Override
    public UsuarioDTO obtenerPorDocumento(String documento) {
        // Busca por documento y mapea a DTO
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

    @Override
    public void eliminarPorDocumento(String documento) {
        // Busca la entidad por documento y la elimina; lanza excepción si no existe
        Usuario usuario = usuarioRepository.findByDocumento(documento)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el documento: " + documento));

        usuarioRepository.delete(usuario);
    }

    @Override
    public UsuarioDTO actualizarUsuarioParcial(String documento, UsuarioDTO datosActualizados) {
        // Verifica existencia del usuario antes de actualizar (se usa solo para lanzar excepción si no existe)
        usuarioRepository.findByDocumento(documento)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con documento: " + documento));

        // Llamada al repository para actualizar campos permitidos
        usuarioRepository.actualizarUsuarioParcial(
            documento,
            datosActualizados.getUsername(),
            datosActualizados.getPassword(),
            datosActualizados.getNombre(),
            datosActualizados.getCargo(),
            datosActualizados.getEmail(),
            datosActualizados.getRol()
        );

        // Recupera la entidad actualizada y la mapea a DTO para devolverla
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
