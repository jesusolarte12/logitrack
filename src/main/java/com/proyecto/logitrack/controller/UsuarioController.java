package com.proyecto.logitrack.controller;

import com.proyecto.logitrack.dto.UsuarioDTO;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.service.UsuarioService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    //Lista todos los usuarios
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    //Crear Nuevo Usuario
    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuarioDTO);
        return ResponseEntity.ok(nuevoUsuario);
    }

    //Obtener usuario por documento
    @GetMapping("/buscar/{documento}")
    public ResponseEntity<UsuarioDTO> obtenerPorDocumento(@PathVariable String documento) {
        UsuarioDTO usuario = usuarioService.obtenerPorDocumento(documento);
        return ResponseEntity.ok(usuario);
    }

    //Eliminar por el documento del usuario
    @DeleteMapping("/eliminar/{documento}")
    public ResponseEntity<String> eliminarPorDocumento(@PathVariable String documento) {
        usuarioService.eliminarPorDocumento(documento);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @PatchMapping("/actualizar/{documento}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @PathVariable String documento,
            @RequestBody UsuarioDTO usuarioDTO) {

        UsuarioDTO actualizado = usuarioService.actualizarUsuarioParcial(documento, usuarioDTO);
        return ResponseEntity.ok(actualizado);
    }
    
}
