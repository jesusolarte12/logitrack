package com.proyecto.logitrack.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.logitrack.dto.UsuarioDTO;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.repository.UsuarioRepository;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    public UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO){
        Usuario usuario = new Usuario();

        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setPassword(usuarioDTO.getPassword());
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setRol(usuarioDTO.getRol());

        Usuario guardado = usuarioRepository.save(usuario);

        return new UsuarioDTO(
            guardado.getUsername(),
            guardado.getPassword(),
            guardado.getNombre(),
            guardado.getEmail(),
            guardado.getRol()
        );
    }

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
            .map(u -> new UsuarioDTO(
                    u.getUsername(),
                    u.getPassword(),
                    u.getNombre(),
                    u.getEmail(),
                    u.getRol()
            ))
            .collect(Collectors.toList());
    }
}
