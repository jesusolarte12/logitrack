package com.proyecto.logitrack.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.logitrack.dto.MovimientoDTO;
import com.proyecto.logitrack.dto.MovimientoDTO.DetalleMovimientoDTO;
import com.proyecto.logitrack.entities.Bodega;
import com.proyecto.logitrack.entities.Inventario;
import com.proyecto.logitrack.entities.Movimiento;
import com.proyecto.logitrack.entities.MovimientoDetalle;
import com.proyecto.logitrack.entities.Producto;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.enums.TipoMovimiento;
import com.proyecto.logitrack.repository.BodegaRepository;
import com.proyecto.logitrack.repository.InventarioRepository;
import com.proyecto.logitrack.repository.MovimientoDetalleRepository;
import com.proyecto.logitrack.repository.MovimientoRepository;
import com.proyecto.logitrack.repository.ProductoRepository;
import com.proyecto.logitrack.repository.UsuarioRepository;
import com.proyecto.logitrack.service.MovimientoService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final MovimientoDetalleRepository movimientoDetalleRepository;
    private final InventarioRepository inventarioRepository;

    @Override
    @Transactional
    public MovimientoDTO crearMovimiento(MovimientoDTO dto) {
        // Validar que el usuario exista
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        Bodega bodegaOrigen = dto.getBodegaOrigenId() != null
                ? bodegaRepository.findById(dto.getBodegaOrigenId())
                    .orElseThrow(() -> new EntityNotFoundException("Bodega origen no encontrada"))
                : null;

        Bodega bodegaDestino = dto.getBodegaDestinoId() != null
                ? bodegaRepository.findById(dto.getBodegaDestinoId())
                    .orElseThrow(() -> new EntityNotFoundException("Bodega destino no encontrada"))
                : null;

        TipoMovimiento tipo;
        try {
            tipo = TipoMovimiento.valueOf(dto.getTipoMovimiento().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + dto.getTipoMovimiento());
        }

        // Crear el movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setUsuario(usuario);
        movimiento.setBodegaOrigen(bodegaOrigen);
        movimiento.setBodegaDestino(bodegaDestino);

        // Crear detalles y actualizar inventario
        List<MovimientoDetalle> detalles = dto.getDetalles().stream()
                .map(detDTO -> {
                    Producto producto = productoRepository.findById(detDTO.getProductoId())
                            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

                    MovimientoDetalle det = new MovimientoDetalle();
                    det.setMovimiento(movimiento);
                    det.setProducto(producto);
                    det.setCantidad(detDTO.getCantidad());
                    
                    // Actualizar inventario según el tipo de movimiento
                    actualizarInventario(tipo, bodegaOrigen, bodegaDestino, producto, detDTO.getCantidad());
                    
                    return det;
                })
                .collect(Collectors.toList());

        movimiento.setDetalles(detalles);
        movimientoRepository.save(movimiento);

        return mapToDTO(movimiento);
    }

    /**
     * Actualiza el inventario según el tipo de movimiento
     */
    private void actualizarInventario(TipoMovimiento tipo, Bodega bodegaOrigen, 
                                     Bodega bodegaDestino, Producto producto, Integer cantidad) {
        switch (tipo) {
            case ENTRADA:
                // Sumar stock en bodega destino
                agregarStock(bodegaDestino, producto, cantidad);
                break;
                
            case SALIDA:
                // Restar stock de bodega origen
                restarStock(bodegaOrigen, producto, cantidad);
                break;
                
            case TRANSFERENCIA:
                // Restar de origen y sumar en destino
                restarStock(bodegaOrigen, producto, cantidad);
                agregarStock(bodegaDestino, producto, cantidad);
                break;
        }
    }

    /**
     * Agrega stock a una bodega (crea registro si no existe)
     */
    private void agregarStock(Bodega bodega, Producto producto, Integer cantidad) {
        Inventario inventario = inventarioRepository
                .findByBodegaIdAndProductoId(bodega.getId(), producto.getId())
                .orElse(null);
        
        if (inventario == null) {
            // Crear nuevo registro en inventario
            inventario = new Inventario();
            inventario.setBodega(bodega);
            inventario.setProducto(producto);
            inventario.setStock(cantidad);
        } else {
            // Actualizar stock existente
            inventario.setStock(inventario.getStock() + cantidad);
        }
        
        inventarioRepository.save(inventario);
    }

    /**
     * Resta stock de una bodega (valida que haya suficiente)
     */
    private void restarStock(Bodega bodega, Producto producto, Integer cantidad) {
        Inventario inventario = inventarioRepository
                .findByBodegaIdAndProductoId(bodega.getId(), producto.getId())
                .orElseThrow(() -> new RuntimeException(
                    "No hay inventario del producto '" + producto.getNombre() + 
                    "' en la bodega '" + bodega.getNombre() + "'"));
        
        if (inventario.getStock() < cantidad) {
            throw new RuntimeException(
                "Stock insuficiente. Disponible: " + inventario.getStock() + 
                ", Requerido: " + cantidad + 
                " para el producto '" + producto.getNombre() + 
                "' en la bodega '" + bodega.getNombre() + "'");
        }
        
        inventario.setStock(inventario.getStock() - cantidad);
        inventarioRepository.save(inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoDTO obtenerPorId(Integer id) {
        Movimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));
        return mapToDTO(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> listarMovimientos() {
        return movimientoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

public List<MovimientoDTO> listarRecientes() {
        return repo.findTop10ByOrderByFechaDesc()
                   .stream()
                   .map(mapper::toDTO)
                   .toList();
    
    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> listarMovimientosPorUsuario(String username) {
        // Buscar el usuario por username
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
        
        // Si es ADMIN, devolver todos los movimientos
        if (usuario.getRol().name().equals("ADMIN")) {
            return listarMovimientos();
        }
        
        // Si es EMPLEADO, buscar las bodegas donde es encargado
        List<Bodega> bodegasEncargadas = bodegaRepository.findByEncargado_Id(usuario.getId());
        
        if (bodegasEncargadas.isEmpty()) {
            // Si no tiene bodegas asignadas, no devolver movimientos
            return List.of();
        }
        
        // Filtrar movimientos donde la bodega origen o destino sea una de las bodegas del empleado
        List<Movimiento> movimientos = bodegasEncargadas.stream()
                .flatMap(bodega -> movimientoRepository
                        .findByBodegaOrigen_IdOrBodegaDestino_Id(bodega.getId(), bodega.getId())
                        .stream())
                .distinct()
                .collect(Collectors.toList());
        
        return movimientos.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarMovimiento(Integer id) {
        if (!movimientoRepository.existsById(id)) {
            throw new EntityNotFoundException("Movimiento no encontrado");
        }
        // IMPORTANTE: Al eliminar un movimiento, NO revertimos el inventario
        // porque podría causar inconsistencias. Si se requiere, debe hacerse manualmente.
        movimientoRepository.deleteById(id);
    }

    /**
     * Mapper que convierte Movimiento a MovimientoDTO con toda la información
     */
    private MovimientoDTO mapToDTO(Movimiento movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        
        // Información básica
        dto.setId(movimiento.getId());
        dto.setFecha(movimiento.getFecha());
        dto.setTipoMovimiento(movimiento.getTipoMovimiento().name());
        
        // IDs de las relaciones
        dto.setUsuarioId(movimiento.getUsuario().getId());
        dto.setBodegaOrigenId(movimiento.getBodegaOrigen() != null ? movimiento.getBodegaOrigen().getId() : null);
        dto.setBodegaDestinoId(movimiento.getBodegaDestino() != null ? movimiento.getBodegaDestino().getId() : null);

        // Nombres de las relaciones
        dto.setResponsable(movimiento.getUsuario().getNombre());
        dto.setBodegaOrigen(movimiento.getBodegaOrigen() != null ? movimiento.getBodegaOrigen().getNombre() : null);
        dto.setBodegaDestino(movimiento.getBodegaDestino() != null ? movimiento.getBodegaDestino().getNombre() : null);

        // Mapear detalles con información completa
        List<DetalleMovimientoDTO> detallesDTO = (movimiento.getDetalles() != null && !movimiento.getDetalles().isEmpty())
                ? movimiento.getDetalles().stream()
                    .map(detalle -> {
                        DetalleMovimientoDTO detalleDTO = new DetalleMovimientoDTO();
                        detalleDTO.setProductoId(detalle.getProducto().getId());
                        detalleDTO.setCantidad(detalle.getCantidad());
                        detalleDTO.setNombreProducto(detalle.getProducto().getNombre());
                        detalleDTO.setCategoriaProducto(detalle.getProducto().getCategoria().getNombre());
                        return detalleDTO;
                    })
                    .collect(Collectors.toList())
                : List.of();

        dto.setDetalles(detallesDTO);
        
        // Información resumida para la tabla
        if (!detallesDTO.isEmpty()) {
            DetalleMovimientoDTO primerDetalle = detallesDTO.get(0);
            
            if (detallesDTO.size() == 1) {
                // Un solo producto
                dto.setProducto(primerDetalle.getNombreProducto());
                dto.setCantidad(primerDetalle.getCantidad());
            } else {
                // Múltiples productos
                dto.setProducto(primerDetalle.getNombreProducto() + " (+" + (detallesDTO.size() - 1) + " más)");
                dto.setCantidad(detallesDTO.stream().mapToInt(DetalleMovimientoDTO::getCantidad).sum());
            }
        }
        
        return dto;
    }
}