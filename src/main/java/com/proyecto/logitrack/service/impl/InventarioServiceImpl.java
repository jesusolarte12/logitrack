package com.proyecto.logitrack.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.logitrack.dto.InventarioDetalleDTO;
import com.proyecto.logitrack.entities.Bodega;
import com.proyecto.logitrack.entities.Inventario;
import com.proyecto.logitrack.entities.Producto;
import com.proyecto.logitrack.repository.BodegaRepository;
import com.proyecto.logitrack.repository.InventarioRepository;
import com.proyecto.logitrack.repository.ProductoRepository;
import com.proyecto.logitrack.service.InventarioService;

@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;

    public InventarioServiceImpl(InventarioRepository inventarioRepository,
                                 BodegaRepository bodegaRepository,
                                 ProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.bodegaRepository = bodegaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<Inventario> getAllInventarios() {
        List<Inventario> lista = inventarioRepository.findAll();
        // Inicializar relaciones perezosas dentro de la transacción
        return lista.stream().map(inv -> {
            if (inv.getBodega() != null) inv.getBodega().getId();
            if (inv.getProducto() != null) inv.getProducto().getId();
            return inv;
        }).collect(Collectors.toList());
    }

    @Override
    public Inventario getInventarioById(Integer id) {
        Inventario inv = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario con id " + id + " no encontrado"));
        if (inv.getBodega() != null) inv.getBodega().getId();
        if (inv.getProducto() != null) inv.getProducto().getId();
        return inv;
    }

    @Override
    public Inventario createInventario(Inventario inventario) {
        if (inventario.getBodega() == null || inventario.getProducto() == null) {
            throw new RuntimeException("La bodega y el producto son obligatorios");
        }

        // Validar existencia sin cargar entidades completas (evita SELECT que traiga columnas inexistentes)
        Integer bodegaId = inventario.getBodega().getId();
        Integer productoId = inventario.getProducto().getId();
        if (bodegaId == null || !bodegaRepository.existsById(bodegaId)) {
            throw new RuntimeException("Bodega no encontrada con id: " + bodegaId);
        }
        if (productoId == null || !productoRepository.existsById(productoId)) {
            throw new RuntimeException("Producto no encontrado con id: " + productoId);
        }

        // Crear referencias ligeras en lugar de cargar las entidades (solo id)
        Bodega bodega = new Bodega();
        bodega.setId(bodegaId);

        Producto producto = new Producto();
        producto.setId(productoId);

        return inventarioRepository.findByBodegaIdAndProductoId(bodega.getId(), producto.getId())
                .map(existing -> {
                    int added = inventario.getStock() == null ? 0 : inventario.getStock();
                    existing.setStock(existing.getStock() + added);
                    if (existing.getBodega() != null) existing.getBodega().getId();
                    if (existing.getProducto() != null) existing.getProducto().getId();
                    return inventarioRepository.save(existing);
                })
                .orElseGet(() -> {
                    if (inventario.getStock() == null) inventario.setStock(0);
                    inventario.setBodega(bodega);
                    inventario.setProducto(producto);
                    Inventario saved = inventarioRepository.save(inventario);
                    if (saved.getBodega() != null) saved.getBodega().getId();
                    if (saved.getProducto() != null) saved.getProducto().getId();
                    return saved;
                });
    }

    @Override
    public Inventario updateInventario(Inventario inventario) {
        if (inventario.getId() == null) {
            throw new RuntimeException("El id del inventario es obligatorio para actualizar");
        }

        Inventario existing = inventarioRepository.findById(inventario.getId())
                .orElseThrow(() -> new RuntimeException("Inventario con id " + inventario.getId() + " no encontrado"));

        if (inventario.getBodega() != null) {
            Integer bodegaId = inventario.getBodega().getId();
            if (bodegaId == null || !bodegaRepository.existsById(bodegaId)) {
                throw new RuntimeException("Bodega no encontrada con id: " + bodegaId);
            }
            Bodega bodega = new Bodega();
            bodega.setId(bodegaId);
            existing.setBodega(bodega);
        }

        if (inventario.getProducto() != null) {
            Integer productoId = inventario.getProducto().getId();
            if (productoId == null || !productoRepository.existsById(productoId)) {
                throw new RuntimeException("Producto no encontrado con id: " + productoId);
            }
            Producto producto = new Producto();
            producto.setId(productoId);
            existing.setProducto(producto);
        }

        if (inventario.getStock() != null) {
            existing.setStock(inventario.getStock());
        }

        Inventario saved = inventarioRepository.save(existing);
        if (saved.getBodega() != null) saved.getBodega().getId();
        if (saved.getProducto() != null) saved.getProducto().getId();
        return saved;
    }

    @Override
    public void deleteInventario(Integer id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByBodega(Integer bodegaId) {
        List<Inventario> lista = inventarioRepository.findByBodegaId(bodegaId);
        return lista.stream().map(inv -> {
            if (inv.getBodega() != null) inv.getBodega().getId();
            if (inv.getProducto() != null) inv.getProducto().getId();
            return inv;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Inventario> findByProducto(Integer productoId) {
        List<Inventario> lista = inventarioRepository.findByProductoId(productoId);
        return lista.stream().map(inv -> {
            if (inv.getBodega() != null) inv.getBodega().getId();
            if (inv.getProducto() != null) inv.getProducto().getId();
            return inv;
        }).collect(Collectors.toList());
    }

    @Override
    public Inventario findByBodegaAndProducto(Integer bodegaId, Integer productoId) {
        Inventario inv = inventarioRepository.findByBodegaIdAndProductoId(bodegaId, productoId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para bodega " + bodegaId + " y producto " + productoId));
        if (inv.getBodega() != null) inv.getBodega().getId();
        if (inv.getProducto() != null) inv.getProducto().getId();
        return inv;
    }

    @Override
    public List<InventarioDetalleDTO> getInventarioDetalleByBodegaNombre(String nombreBodega) {
        return inventarioRepository.findInventarioDetalleByBodegaNombre(nombreBodega);
    }

    @Override
    public List<InventarioDetalleDTO> getAllInventarioDetalle() {
        return inventarioRepository.findAllInventarioDetalle();
    }
}
