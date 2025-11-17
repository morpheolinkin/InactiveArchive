package com.escola.inactivearchive.repository;

import com.escola.inactivearchive.models.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    // Método Mágico do Spring Data JPA
    // Containing: busca por partes do texto (como LIKE %texto%)
    // IgnoreCase: ignora maiúsculas e minúsculas
    List<Aluno> findByNomeContainingIgnoreCase(String nome);
}
