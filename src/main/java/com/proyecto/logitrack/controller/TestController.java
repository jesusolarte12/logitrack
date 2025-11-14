package com.proyecto.logitrack.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok("Hola! Autenticado como: " + authentication);
    }

    @GetMapping("/auth-info")
    public ResponseEntity<Map<String, Object>> authInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> info = new HashMap<>();
        info.put("principal", auth.getPrincipal());
        info.put("name", auth.getName());
        info.put("authenticated", auth.isAuthenticated());
        info.put("authorities", auth.getAuthorities());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/public")
    public ResponseEntity<String> testPublic() {
        return ResponseEntity.ok("Esta es una ruta pública dentro de /api");
    }
}
