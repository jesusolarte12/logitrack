package com.proyecto.logitrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.logitrack.dto.UsuarioDTO;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.repository.UsuarioRepository;
import com.proyecto.logitrack.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Usuario registrar(@RequestBody UsuarioDTO dto) {
        Usuario u = new Usuario();
        u.setUsername(dto.getUsername());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setNombre(dto.getNombre());
        u.setCargo(dto.getCargo());
        u.setDocumento(dto.getDocumento());
        u.setEmail(dto.getEmail());
        u.setRol(dto.getRol());
        return usuarioRepo.save(u);
    }

    @PostMapping("/login")
    public String login(@RequestBody UsuarioDTO dto) {
        Usuario u = usuarioRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        if (!passwordEncoder.matches(dto.getPassword(), u.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return jwtUtil.generarToken(u.getUsername());
    }
}
