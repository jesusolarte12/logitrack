package com.proyecto.logitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.logitrack.dto.InventarioDetalleDTO;
import com.proyecto.logitrack.entities.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    // Buscar inventario por bodega y producto
    Optional<Inventario> findByBodegaIdAndProductoId(Integer bodegaId, Integer productoId);

    // Listar inventario por bodega
    List<Inventario> findByBodegaId(Integer bodegaId);

    // Listar inventario por producto
    List<Inventario> findByProductoId(Integer productoId);

    // Obtener detalle de inventario por nombre de bodega
    @Query("SELECT new com.proyecto.logitrack.dto.InventarioDetalleDTO(" +
           "b.nombre, p.nombre, p.precioVenta, p.precioCompra, c.nombre, i.stock) " +
           "FROM Inventario i " +
           "JOIN i.producto p " +
           "JOIN i.bodega b " +
           "JOIN p.categoria c " +
           "WHERE b.nombre = :nombreBodega")
    List<InventarioDetalleDTO> findInventarioDetalleByBodegaNombre(@Param("nombreBodega") String nombreBodega);

    // Obtener todo el inventario detallado (todas las bodegas)
    @Query("SELECT new com.proyecto.logitrack.dto.InventarioDetalleDTO(" +
           "b.nombre, p.nombre, p.precioVenta, p.precioCompra, c.nombre, i.stock) " +
           "FROM Inventario i " +
           "JOIN i.producto p " +
           "JOIN i.bodega b " +
           "JOIN p.categoria c")
    List<InventarioDetalleDTO> findAllInventarioDetalle();

    // Obtener inventario detallado solo de las bodegas del empleado (por username)
    @Query("SELECT new com.proyecto.logitrack.dto.InventarioDetalleDTO(" +
           "b.nombre, p.nombre, p.precioVenta, p.precioCompra, c.nombre, i.stock) " +
           "FROM Inventario i " +
           "JOIN i.producto p " +
           "JOIN i.bodega b " +
           "JOIN p.categoria c " +
           "WHERE b.encargado.username = :username")
    List<InventarioDetalleDTO> findInventarioDetalleByEncargado(@Param("username") String username);
}
