package com.proyecto.logitrack.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proyecto.logitrack.entities.Usuario;
import jakarta.transaction.Transactional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
    
    Optional<Usuario> findByDocumento(String documento);
    
    @Transactional
    void deleteByDocumento(String documento);

    //Verifica la Existencia
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByDocumento(String Documento);
}
