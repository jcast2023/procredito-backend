package com.procredito.backend.repository;

import com.procredito.backend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDocumento(String documento);
    boolean existsByDocumento(String documento);
    boolean existsByDocumentoAndIdNot(String documento, Long id);
    List<Cliente> findByAnalistaId(Long analistaId);
    Optional<Cliente> findByIdAndAnalistaId(Long id, Long analistaId);
    long countByAnalistaId(Long analistaId);
}
