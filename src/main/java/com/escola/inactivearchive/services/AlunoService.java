package com.escola.inactivearchive.services;

import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {
    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public List<Aluno> listarTodos(String termoBusca) {
        if (termoBusca != null && !termoBusca.isBlank()) {
            return alunoRepository.findByNomeContainingIgnoreCase(termoBusca);
        }

        return alunoRepository.findAll();
    }

    public Aluno buscarPorId(Long id) {
        Optional<Aluno> alunoPorId = alunoRepository.findById(id);
        return alunoPorId.orElse(null);
    }

    // Método para salvar um aluno
    public Aluno salvar(Aluno aluno) {
        // Exemplo de regra de negócio futura:
        // if (alunoRepository.existsByCpf(aluno.getCpf())) { lançar erro }

        // Por enquanto, apenas salva
        return alunoRepository.save(aluno);
    }

    public void excluir(Long id) {
        alunoRepository.deleteById(id);
    }
}
