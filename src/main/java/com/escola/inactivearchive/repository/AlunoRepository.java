package com.escola.inactivearchive.repository;

import com.escola.inactivearchive.models.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    Aluno findByCpf(String cpf);

    // 1. Busca Customizada com JPQL: Procura no NOME ou no CPF
    @Query("SELECT a FROM Aluno a WHERE lower(a.nome) LIKE lower(concat('%', :termo, '%')) OR a.cpf LIKE concat('%', :termo, '%')")
    List<Aluno> buscarPorNomeOuCpf(@Param("termo") String termo);

    // 2. Busca por Data de Nascimento exata
    List<Aluno> findByDataNascimento(LocalDate dataNascimento);
}
