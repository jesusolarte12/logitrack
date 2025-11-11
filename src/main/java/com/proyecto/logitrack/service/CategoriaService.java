package com.proyecto.logitrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.proyecto.logitrack.entities.Categoria;
import com.proyecto.logitrack.repository.CategoriaRepository;

@Service
public class CategoriaService {
    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria crear(Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La categoria ya existe.");
        } 
        return categoriaRepository.save(categoria);
    }

    public Categoria buscarPorNombre(String nombre) {
    Categoria categoria = categoriaRepository.findByNombre(nombre);

    if (categoria == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe.");
    }

    return categoria;
}

}
