package com.proyecto.logitrack.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.proyecto.logitrack.dto.InventarioDTO;
import com.proyecto.logitrack.dto.InventarioDetalleDTO;
import com.proyecto.logitrack.entities.Bodega;
import com.proyecto.logitrack.entities.Inventario;
import com.proyecto.logitrack.entities.Producto;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.enums.UsuarioRolEnum;
import com.proyecto.logitrack.repository.BodegaRepository;
import com.proyecto.logitrack.repository.ProductoRepository;
import com.proyecto.logitrack.repository.UsuarioRepository;
import com.proyecto.logitrack.service.AuditoriaService;
import com.proyecto.logitrack.service.InventarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    @Autowired
    private AuditoriaService auditoriaService;

    public InventarioController(InventarioService inventarioService,
                                BodegaRepository bodegaRepository,
                                ProductoRepository productoRepository,
                                UsuarioRepository usuarioRepository,
                                AuditoriaService auditoriaService) {
        this.inventarioService = inventarioService;
        this.bodegaRepository = bodegaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    // Listar todos los inventarios (devuelve DTOs para evitar problemas de serialización)
    @GetMapping("/listar")
    public ResponseEntity<List<InventarioDTO>> getAllInventarios() {
        List<Inventario> inventarios = inventarioService.getAllInventarios();
        List<InventarioDTO> dtos = inventarios.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Obtener inventario por ID (devuelve DTO)
    @GetMapping("/buscar/{id}")
    public ResponseEntity<InventarioDTO> getInventarioById(@PathVariable Integer id) {
        Inventario inventario = inventarioService.getInventarioById(id);
        return ResponseEntity.ok(toDto(inventario));
    }

    // Crear inventario
    @PostMapping("/create")
    public ResponseEntity<InventarioDTO> createInventario(@Valid @RequestBody InventarioDTO dto) {
        // Validar existencia de referencias sin cargar entidades completas (evita SELECT con joins)
        if (dto.getBodegaId() == null || !bodegaRepository.existsById(dto.getBodegaId())) {
            throw new RuntimeException("Bodega no encontrada con id: " + dto.getBodegaId());
        }
        if (dto.getProductoId() == null || !productoRepository.existsById(dto.getProductoId())) {
            throw new RuntimeException("Producto no encontrado con id: " + dto.getProductoId());
        }

        // Crear referencias ligeras (solo id) para evitar que JPA haga un SELECT completo
        Bodega bodega = new Bodega();
        bodega.setId(dto.getBodegaId());

        Producto producto = new Producto();
        producto.setId(dto.getProductoId());

        Inventario inventario = new Inventario();
        inventario.setBodega(bodega);
        inventario.setProducto(producto);
        inventario.setStock(dto.getStock());

        Inventario nuevo = inventarioService.createInventario(inventario);
        auditoriaService.registrar("INSERT", "inventario", nuevo.getId(), null, toDto(nuevo), null);
        return ResponseEntity.ok(toDto(nuevo));
    }

    // Actualizar inventario
    @PutMapping("/update/{id}")
    public ResponseEntity<InventarioDTO> updateInventario(@PathVariable Integer id,
                                                       @RequestBody InventarioDTO dto) {
        // Obtener estado anterior y crear una copia (DTO) para evitar que JPA lo modifique
        Inventario antes = inventarioService.getInventarioById(id);
        InventarioDTO antesDto = toDto(antes); // snapshot antes de la actualización

        Inventario inventario = new Inventario();
        inventario.setId(id);

        if (dto.getBodegaId() != null) {
            if (!bodegaRepository.existsById(dto.getBodegaId())) {
                throw new RuntimeException("Bodega no encontrada con id: " + dto.getBodegaId());
            }
            Bodega bodega = new Bodega();
            bodega.setId(dto.getBodegaId());
            inventario.setBodega(bodega);
        }

        if (dto.getProductoId() != null) {
            if (!productoRepository.existsById(dto.getProductoId())) {
                throw new RuntimeException("Producto no encontrado con id: " + dto.getProductoId());
            }
            Producto producto = new Producto();
            producto.setId(dto.getProductoId());
            inventario.setProducto(producto);
        }

        if (dto.getStock() != null) {
            inventario.setStock(dto.getStock());
        }

        Inventario updated = inventarioService.updateInventario(inventario);
        // Crear snapshot del estado después en DTO (detached)
        InventarioDTO despuesDto = toDto(updated);
        // Registrar auditoría usando snapshots DTO para valorAntes/valorDespues
        auditoriaService.registrar("UPDATE", "inventario", updated.getId(), antesDto, despuesDto, null);
        return ResponseEntity.ok(despuesDto);
    }

    // Eliminar inventario
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteInventario(@PathVariable Integer id) {
        // Tomar snapshot antes de eliminar
        Inventario antes = inventarioService.getInventarioById(id);
        InventarioDTO antesDto = toDto(antes);
        inventarioService.deleteInventario(id);
        auditoriaService.registrar("DELETE", "inventario", id, antesDto, null, null);
        return ResponseEntity.noContent().build();
    }

    // Buscar por bodega (devuelve DTOs)
    @GetMapping("/buscar/bodega")
    public ResponseEntity<List<InventarioDTO>> buscarPorBodega(@RequestParam Integer bodegaId) {
        List<Inventario> lista = inventarioService.findByBodega(bodegaId);
        List<InventarioDTO> dtos = lista.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Obtener inventario por bodega
    @GetMapping("/bodega/{bodegaId}")
    public ResponseEntity<List<InventarioDTO>> getInventarioPorBodega(@PathVariable Integer bodegaId) {
        List<Inventario> inventarios = inventarioService.findByBodega(bodegaId);
        List<InventarioDTO> dtos = inventarios.stream()
                .filter(inv -> inv.getStock() > 0) // Solo productos con stock
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
}
    // Buscar por producto (devuelve DTOs)
    @GetMapping("/buscar/producto")
    public ResponseEntity<List<InventarioDTO>> buscarPorProducto(@RequestParam Integer productoId) {
        List<Inventario> lista = inventarioService.findByProducto(productoId);
        List<InventarioDTO> dtos = lista.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Obtener inventario detallado por nombre de bodega
    @GetMapping("/detalle/bodega/{nombreBodega}")
    public ResponseEntity<List<InventarioDetalleDTO>> getInventarioDetallePorBodega(@PathVariable String nombreBodega) {
        List<InventarioDetalleDTO> detalle = inventarioService.getInventarioDetalleByBodegaNombre(nombreBodega);
        return ResponseEntity.ok(detalle);
    }

    // Obtener inventario detallado con filtrado por rol (ADMIN ve todo, EMPLEADO solo sus bodegas)
    @GetMapping("/detalle")
    public ResponseEntity<List<InventarioDetalleDTO>> getAllInventarioDetalle() {
        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Buscar usuario para obtener su rol
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<InventarioDetalleDTO> detalle;
        
        // Filtrar según el rol
        if (usuario.getRol() == UsuarioRolEnum.ADMIN) {
            // ADMIN ve todo el inventario
            detalle = inventarioService.getAllInventarioDetalle();
        } else {
            // EMPLEADO solo ve el inventario de sus bodegas
            detalle = inventarioService.getInventarioDetalleByEncargado(username);
        }
        
        return ResponseEntity.ok(detalle);
    }

    // Helper: mapear entidad a DTO
    private InventarioDTO toDto(Inventario inv) {
        if (inv == null) return null;
        Integer bodegaId = inv.getBodega() != null ? inv.getBodega().getId() : null;
        Integer productoId = inv.getProducto() != null ? inv.getProducto().getId() : null;
        return new InventarioDTO(inv.getId(), bodegaId, productoId, inv.getStock());
    }
}
