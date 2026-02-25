package com.escola.inactivearchive.repository;

import com.escola.inactivearchive.models.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    Aluno findByCpf(String cpf);

    // IMPORTANTE: Mudamos o retorno de List<Aluno> para Page<Aluno> e adicionamos Pageable
    @Query("SELECT a FROM Aluno a WHERE lower(a.nome) LIKE lower(concat('%', :termo, '%')) OR a.cpf LIKE concat('%', :termo, '%')")
    Page<Aluno> buscarPorNomeOuCpf(@Param("termo") String termo, Pageable pageable);

    List<Aluno> findByDataNascimento(LocalDate dataNascimento);

    // Busca o maior ID. O COALESCE garante que, se o banco estiver vazio, ele retorne 0.
    @Query("SELECT COALESCE(MAX(a.id), 0) FROM Aluno a")
    Long obterMaiorId();
}
