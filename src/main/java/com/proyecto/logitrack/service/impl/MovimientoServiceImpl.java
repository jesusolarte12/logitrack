package com.proyecto.logitrack.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.logitrack.dto.MovimientoDTO;
import com.proyecto.logitrack.dto.MovimientoDTO.DetalleMovimientoDTO;
import com.proyecto.logitrack.entities.Bodega;
import com.proyecto.logitrack.entities.Movimiento;
import com.proyecto.logitrack.entities.MovimientoDetalle;
import com.proyecto.logitrack.entities.Producto;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.enums.TipoMovimiento;
import com.proyecto.logitrack.repository.BodegaRepository;
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

    @Override
    @Transactional
    public MovimientoDTO crearMovimiento(MovimientoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Bodega bodegaOrigen = dto.getBodegaOrigenId() != null
                ? bodegaRepository.findById(dto.getBodegaOrigenId())
                    .orElseThrow(() -> new EntityNotFoundException("Bodega origen no encontrada"))
                : null;

        Bodega bodegaDestino = dto.getBodegaDestinoId() != null
                ? bodegaRepository.findById(dto.getBodegaDestinoId())
                    .orElseThrow(() -> new EntityNotFoundException("Bodega destino no encontrada"))
                : null;

        // Convertir el tipoMovimiento String a Enum
        TipoMovimiento tipo;
        try {
            tipo = TipoMovimiento.valueOf(dto.getTipoMovimiento().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + dto.getTipoMovimiento());
        }

        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setUsuario(usuario);
        movimiento.setBodegaOrigen(bodegaOrigen);
        movimiento.setBodegaDestino(bodegaDestino);

        // Convertir los detalles DTO a entidades
        List<MovimientoDetalle> detalles = dto.getDetalles().stream()
                .map(detDTO -> {
                    Producto producto = productoRepository.findById(detDTO.getProductoId())
                            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

                    MovimientoDetalle det = new MovimientoDetalle();
                    det.setMovimiento(movimiento);
                    det.setProducto(producto);
                    det.setCantidad(detDTO.getCantidad());
                    return det;
                })
                .collect(Collectors.toList());

        movimiento.setDetalles(detalles);

        // Guardar movimiento + detalles (cascade se encarga del resto)
        movimientoRepository.save(movimiento);

        return mapToDTO(movimiento);
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

    @Override
    @Transactional
    public void eliminarMovimiento(Integer id) {
        if (!movimientoRepository.existsById(id)) {
            throw new EntityNotFoundException("Movimiento no encontrado");
        }
        movimientoRepository.deleteById(id);
    }

    // Mapper entidad → DTO
    private MovimientoDTO mapToDTO(Movimiento movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(movimiento.getId());
        dto.setFecha(movimiento.getFecha());
        dto.setTipoMovimiento(movimiento.getTipoMovimiento().name());
        dto.setUsuarioId(movimiento.getUsuario().getId());
        dto.setBodegaOrigenId(movimiento.getBodegaOrigen() != null ? movimiento.getBodegaOrigen().getId() : null);
        dto.setBodegaDestinoId(movimiento.getBodegaDestino() != null ? movimiento.getBodegaDestino().getId() : null);

        List<DetalleMovimientoDTO> detallesDTO = (movimiento.getDetalles() != null)
                ? movimiento.getDetalles().stream()
                    .map(d -> new DetalleMovimientoDTO(
                            d.getProducto().getId(),
                            d.getCantidad()
                    ))
                    .collect(Collectors.toList())
                : List.of();

        dto.setDetalles(detallesDTO);
        return dto;
    }
}
