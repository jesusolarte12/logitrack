package com.proyecto.logitrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.logitrack.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
