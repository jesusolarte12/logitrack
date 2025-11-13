package com.proyecto.logitrack.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.logitrack.dto.MovimientoDetalleDTO;
import com.proyecto.logitrack.entities.Movimiento;
import com.proyecto.logitrack.entities.MovimientoDetalle;
import com.proyecto.logitrack.entities.Producto;
import com.proyecto.logitrack.repository.MovimientoDetalleRepository;
import com.proyecto.logitrack.repository.MovimientoRepository;
import com.proyecto.logitrack.repository.ProductoRepository;
import com.proyecto.logitrack.service.MovimientoDetalleService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoDetalleServiceImpl implements MovimientoDetalleService {

    private final MovimientoDetalleRepository detalleRepository;
    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDetalleDTO> listarTodos() {
        return detalleRepository.findAll()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
}


    @Override
    @Transactional
    public MovimientoDetalleDTO crear(MovimientoDetalleDTO dto) {
        Movimiento movimiento = movimientoRepository.findById(dto.getMovimientoId())
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        MovimientoDetalle detalle = new MovimientoDetalle();
        detalle.setMovimiento(movimiento);
        detalle.setProducto(producto);
        detalle.setCantidad(dto.getCantidad());

        detalleRepository.save(detalle);
        return mapToDTO(detalle);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoDetalleDTO obtenerPorId(Integer id) {
        MovimientoDetalle det = detalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado"));
        return mapToDTO(det);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDetalleDTO> listarPorMovimiento(Integer movimientoId) {
        return detalleRepository.findByMovimientoId(movimientoId)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MovimientoDetalleDTO actualizar(Integer id, MovimientoDetalleDTO dto) {
        MovimientoDetalle det = detalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado"));

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        det.setProducto(producto);
        det.setCantidad(dto.getCantidad());

        detalleRepository.save(det);
        return mapToDTO(det);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!detalleRepository.existsById(id)) {
            throw new EntityNotFoundException("Detalle no encontrado");
        }
        detalleRepository.deleteById(id);
    }

    // 🔄 Mapper entidad → DTO
    private MovimientoDetalleDTO mapToDTO(MovimientoDetalle detalle) {
        MovimientoDetalleDTO dto = new MovimientoDetalleDTO();
        dto.setId(detalle.getId());
        dto.setMovimientoId(detalle.getMovimiento().getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setCantidad(detalle.getCantidad());
        return dto;
    }
}
