package com.proyecto.logitrack.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.proyecto.logitrack.entities.Categoria;
import com.proyecto.logitrack.repository.CategoriaRepository;
import com.proyecto.logitrack.service.CategoriaService;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * Servicio que maneja operaciones sobre la entidad Categoria.
     * Implementa la interfaz `CategoriaService` y delega al repository.
     */

    @Override
    public List<Categoria> listar() {
        // Devuelve todas las categorías registradas
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria crear(Categoria categoria) {
        // Verifica que no exista otra categoría con el mismo nombre
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La categoria ya existe.");
        }

        // Guarda y devuelve la nueva categoría
        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria buscarPorNombre(String nombre) {
        // Busca la categoría por nombre y lanza 404 si no existe
        Categoria categoria = categoriaRepository.findByNombre(nombre);

        if (categoria == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe.");
        }

        return categoria;
    }

}
