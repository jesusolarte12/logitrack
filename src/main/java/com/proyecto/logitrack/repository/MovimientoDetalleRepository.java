package com.proyecto.logitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.logitrack.entities.MovimientoDetalle;

@Repository
public interface MovimientoDetalleRepository extends JpaRepository<MovimientoDetalle, Integer> {

    List<MovimientoDetalle> findByMovimientoId(Integer movimientoId);
}
