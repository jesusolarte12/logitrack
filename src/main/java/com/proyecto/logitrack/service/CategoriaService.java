package com.proyecto.logitrack.service;

import java.util.List;

import com.proyecto.logitrack.entities.Categoria;

public interface CategoriaService {

    // Lista todas las categorias
    List<Categoria> listar();
    
    // Crea una nueva categoria
    Categoria crear(Categoria categoria);

    // Busca una categoria por su nombre
    Categoria buscarPorNombre(String nombre);

}
