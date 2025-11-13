package com.proyecto.logitrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.logitrack.entities.Auditoria;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
	// Repositorio JPA para la entidad Auditoria
	// Métodos CRUD estándar heredados de JpaRepository
}
