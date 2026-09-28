package com.example.sessionbased.session_based;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // O Spring Data JPA já cria os métodos save(), findAll(), findById() automaticamente
}
