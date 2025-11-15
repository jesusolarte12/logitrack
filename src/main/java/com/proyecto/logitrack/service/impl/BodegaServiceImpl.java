package com.proyecto.logitrack.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.proyecto.logitrack.entities.Bodega;
import com.proyecto.logitrack.repository.BodegaRepository;
import com.proyecto.logitrack.service.BodegaService;
import com.proyecto.logitrack.dto.BodegaDashboardDTO;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class BodegaServiceImpl implements BodegaService{
    
    private final BodegaRepository bodegaRepository;

    // Listar todas las bodegas
    @Override
    public List<Bodega> getAllBodegas() {
        return bodegaRepository.findAll();
    }

    // Buscar bodega por ID
    @Override
    public Bodega getBodegaById(Integer id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada con ID: " + id));
    }

    // Crear bodega
    @Override
    public Bodega createBodega(Bodega bodega) {
        return bodegaRepository.save(bodega);
    }

    // Actualizar bodega
    @Override
    public Bodega updateBodega(Bodega bodega) {
        // Validar existencia
        Bodega existente = bodegaRepository.findById(bodega.getId())
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada con ID: " + bodega.getId()));

        // Actualizar campos
        if (bodega.getNombre() != null)
            existente.setNombre(bodega.getNombre());

        if (bodega.getUbicacion() != null)
            existente.setUbicacion(bodega.getUbicacion());

        if (bodega.getEncargado() != null)
            existente.setEncargado(bodega.getEncargado());

        return bodegaRepository.save(existente);
    }

    // Eliminar bodega
    @Override
    public void deleteBodega(Integer id) {
        if (!bodegaRepository.existsById(id)) {
            throw new RuntimeException("Bodega no encontrada con ID: " + id);
        }
        bodegaRepository.deleteById(id);
    }

    // Buscar por nombre de bodega
    @Override
    public List<Bodega> findByNombre(String nombre) {
        return bodegaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // Buscar por nombre del encargado
    @Override
    public List<Bodega> findBynombreEncargado(String nombreEncargado) {
        return bodegaRepository.findByNombreEncargado(nombreEncargado);
    }

    // Obtener datos agregados para el dashboard
    @Override
    public List<BodegaDashboardDTO> getBodegaDashboard() {
        return bodegaRepository.findBodegaDashboard();
    }

}
