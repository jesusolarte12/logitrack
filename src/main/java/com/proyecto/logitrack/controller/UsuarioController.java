package com.proyecto.logitrack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.logitrack.dto.UsuarioDTO;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.service.UsuarioService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    //Lista todos los usuarios
    @GetMapping("/listar")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioDTO> obtenerPorUsername(@PathVariable String username) {
        UsuarioDTO usuarioDTO = usuarioService.obtenerPorUsername(username);
        return ResponseEntity.ok(usuarioDTO);
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
