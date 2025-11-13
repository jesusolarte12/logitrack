package com.proyecto.logitrack.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
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
import com.proyecto.logitrack.entities.Bodega;
import com.proyecto.logitrack.entities.Inventario;
import com.proyecto.logitrack.entities.Producto;
import com.proyecto.logitrack.repository.BodegaRepository;
import com.proyecto.logitrack.repository.ProductoRepository;
import com.proyecto.logitrack.service.InventarioService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;

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
        Bodega bodega = bodegaRepository.findById(dto.getBodegaId())
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada con id: " + dto.getBodegaId()));

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + dto.getProductoId()));

        Inventario inventario = new Inventario();
        inventario.setBodega(bodega);
        inventario.setProducto(producto);
        inventario.setStock(dto.getStock());

        Inventario nuevo = inventarioService.createInventario(inventario);
        return ResponseEntity.ok(toDto(nuevo));
    }

    // Actualizar inventario
    @PutMapping("/update/{id}")
    public ResponseEntity<InventarioDTO> updateInventario(@PathVariable Integer id,
                                                       @RequestBody InventarioDTO dto) {
        Inventario inventario = new Inventario();
        inventario.setId(id);

        if (dto.getBodegaId() != null) {
            Bodega bodega = bodegaRepository.findById(dto.getBodegaId())
                    .orElseThrow(() -> new RuntimeException("Bodega no encontrada con id: " + dto.getBodegaId()));
            inventario.setBodega(bodega);
        }

        if (dto.getProductoId() != null) {
            Producto producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + dto.getProductoId()));
            inventario.setProducto(producto);
        }

        if (dto.getStock() != null) {
            inventario.setStock(dto.getStock());
        }

        Inventario updated = inventarioService.updateInventario(inventario);
        return ResponseEntity.ok(toDto(updated));
    }

    // Eliminar inventario
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteInventario(@PathVariable Integer id) {
        inventarioService.deleteInventario(id);
        return ResponseEntity.noContent().build();
    }

    // Buscar por bodega (devuelve DTOs)
    @GetMapping("/buscar/bodega")
    public ResponseEntity<List<InventarioDTO>> buscarPorBodega(@RequestParam Integer bodegaId) {
        List<Inventario> lista = inventarioService.findByBodega(bodegaId);
        List<InventarioDTO> dtos = lista.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Buscar por producto (devuelve DTOs)
    @GetMapping("/buscar/producto")
    public ResponseEntity<List<InventarioDTO>> buscarPorProducto(@RequestParam Integer productoId) {
        List<Inventario> lista = inventarioService.findByProducto(productoId);
        List<InventarioDTO> dtos = lista.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Helper: mapear entidad a DTO
    private InventarioDTO toDto(Inventario inv) {
        if (inv == null) return null;
        Integer bodegaId = inv.getBodega() != null ? inv.getBodega().getId() : null;
        Integer productoId = inv.getProducto() != null ? inv.getProducto().getId() : null;
        return new InventarioDTO(inv.getId(), bodegaId, productoId, inv.getStock());
    }
}
