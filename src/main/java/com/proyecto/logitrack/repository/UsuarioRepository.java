package com.proyecto.logitrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.proyecto.logitrack.entities.Usuario;
import com.proyecto.logitrack.enums.UsuarioRolEnum;

import jakarta.transaction.Transactional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
    
    //Encontrar por documento
    Optional<Usuario> findByDocumento(String documento);
    
    //Eliminar por documento
    @Transactional
    void deleteByDocumento(String documento);

    //Actualizar por documento
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET " + 
           "u.username = COALESCE(:username, u.username), " + 
           "u.password = COALESCE(:password, u.password), " + 
           "u.nombre = COALESCE(:nombre, u.nombre), " + 
           "u.cargo = COALESCE(:cargo, u.cargo), " +
           "u.email = COALESCE(:email, u.email), " + 
           "u.rol = COALESCE(:rol, u.rol) " + 
           "WHERE u.documento = :documento"
        )
    int actualizarUsuarioParcial(
        @Param("documento") String documento,
        @Param("username") String username,
        @Param("password") String password,
        @Param("nombre") String nombre,
        @Param("cargo") String cargo,
        @Param("email") String email,
        @Param("rol") UsuarioRolEnum rol
    );

    //Verifica la Existencia
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByDocumento(String Documento);
}
