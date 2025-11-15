package com.proyecto.logitrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.logitrack.entities.Bodega;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.enums.UsuarioRolEnum;
import com.proyecto.logitrack.repository.UsuarioRepository;
import com.proyecto.logitrack.service.AuditoriaService;
import com.proyecto.logitrack.service.BodegaService;
import com.proyecto.logitrack.dto.BodegaDashboardDTO;
import com.proyecto.logitrack.dto.BodegaDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bodega")
public class BodegaController {

    private final BodegaService bodegaService;
    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;

    public BodegaController(BodegaService bodegaService, AuditoriaService auditoriaService, UsuarioRepository usuarioRepository) {
        this.bodegaService = bodegaService;
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
    }
    
    // Listar todas las bodegas
    @GetMapping("/listar")
    public ResponseEntity<List<Bodega>> getAllBodegas() {
        List<Bodega> bodegas = bodegaService.getAllBodegas();
        return ResponseEntity.ok(bodegas);
    }

    // Buscar bodega por id
    @GetMapping("/buscar/{id}")
    public ResponseEntity<Bodega> getBodegaById(@PathVariable Integer id) {
        Bodega bodega = bodegaService.getBodegaById(id);
        return ResponseEntity.ok(bodega);
    }

    // Crear bodega
    @PostMapping("/create")
    public ResponseEntity<?> createBodega (@Valid @RequestBody BodegaDTO bodegaDTO) {
        try {
            // Buscar el encargado
            Usuario encargado = usuarioRepository.findById(bodegaDTO.getEncargadoId())
                    .orElseThrow(() -> new RuntimeException("Encargado no encontrado con ID: " + bodegaDTO.getEncargadoId()));

            // Crear la entidad Bodega
            Bodega bodega = new Bodega();
            bodega.setNombre(bodegaDTO.getNombre());
            bodega.setUbicacion(bodegaDTO.getUbicacion());
            bodega.setCapacidad(bodegaDTO.getCapacidad());
            bodega.setEncargado(encargado);

            Bodega newBodega = bodegaService.createBodega(bodega);
            
            // Auditoría
            ObjectMapper mapper = new ObjectMapper();
            Object despues = mapper.convertValue(newBodega, java.util.Map.class);
            auditoriaService.registrar("INSERT", "bodega", newBodega.getId(), null, despues, null);
            
            return ResponseEntity.ok(newBodega);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Actualizar bodega
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBodega(@PathVariable Integer id, @Valid @RequestBody BodegaDTO bodegaDTO) {
        try {
            Bodega antes = bodegaService.getBodegaById(id);
            ObjectMapper mapper = new ObjectMapper();
            Object antesSnapshot = mapper.convertValue(antes, java.util.Map.class);
            
            // Buscar el nuevo encargado
            Usuario encargado = usuarioRepository.findById(bodegaDTO.getEncargadoId())
                    .orElseThrow(() -> new RuntimeException("Encargado no encontrado con ID: " + bodegaDTO.getEncargadoId()));
            
            // Actualizar campos
            antes.setNombre(bodegaDTO.getNombre());
            antes.setUbicacion(bodegaDTO.getUbicacion());
            antes.setCapacidad(bodegaDTO.getCapacidad());
            antes.setEncargado(encargado);
            
            Bodega actualizada = bodegaService.updateBodega(antes);
            Object despuesSnapshot = mapper.convertValue(actualizada, java.util.Map.class);
            auditoriaService.registrar("UPDATE", "bodega", actualizada.getId(), antesSnapshot, despuesSnapshot, null);
            
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Eliminar bodega
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBodega(@PathVariable Integer id) {
        Bodega antes = bodegaService.getBodegaById(id);
        ObjectMapper mapper = new ObjectMapper();
        Object antesSnapshot = mapper.convertValue(antes, java.util.Map.class);
        bodegaService.deleteBodega(id); // o deleteBodega(id) si renombras el método
        auditoriaService.registrar("DELETE", "bodega", id, antesSnapshot, null, null);
        return ResponseEntity.noContent().build();
    }

    // Buscar bodega por nombre
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<Bodega>> findByNombre(@RequestParam String nombre) {
        List<Bodega> bodegas = bodegaService.findByNombre(nombre);
        return ResponseEntity.ok(bodegas);
    }

    // Buscar por nombre del encargado
    @GetMapping("/buscar/encargado")
    public ResponseEntity<List<Bodega>> findByEncargado(@RequestParam String encargado) {
        List<Bodega> bodegas = bodegaService.findBynombreEncargado(encargado);
        return ResponseEntity.ok(bodegas);
    }

    // Dashboard de bodegas (filtrado según rol)
    @GetMapping("/dashboard")
    public ResponseEntity<List<BodegaDashboardDTO>> getBodegaDashboard() {
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        
        // Buscar el usuario en la base de datos para obtener su rol
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        List<BodegaDashboardDTO> dashboard;

        // Si es ADMIN, mostrar todas las bodegas
        if (usuario.getRol() == UsuarioRolEnum.ADMIN) {
            dashboard = bodegaService.getBodegaDashboard();
        } 
        // Si es EMPLEADO, mostrar solo sus bodegas
        else {
            dashboard = bodegaService.getBodegaDashboardByUsername(username);
        }

        return ResponseEntity.ok(dashboard);
    }

    // Obtener lista de usuarios disponibles para asignar como encargados
    @GetMapping("/encargados-disponibles")
    public ResponseEntity<List<Usuario>> getEncargadosDisponibles() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }
    
}
