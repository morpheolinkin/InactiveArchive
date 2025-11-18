package com.escola.inactivearchive.services;

import com.escola.inactivearchive.exception.RecursoNaoEncontradoException;
import com.escola.inactivearchive.exception.RegraNegocioException;
import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AlunoService {
    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public List<Aluno> listarTodos(String termoBusca) {
        if (termoBusca != null && !termoBusca.isBlank()) {
            // Tenta verificar se o termo é uma Data (ex: 10/12/2000)
            LocalDate dataBusca = tentarConverterParaData(termoBusca);

            if (dataBusca != null) {
                // Se conseguiu converter, busca por data
                return alunoRepository.findByDataNascimento(dataBusca);
            } else {
                // Se não é data, busca por Nome ou CPF
                return alunoRepository.buscarPorNomeOuCpf(termoBusca);
            }
        }
        return alunoRepository.findAll();
    }

    // Método auxiliar privado para tentar converter String em LocalDate
    private LocalDate tentarConverterParaData(String termo) {
        try {
            // Tenta formato brasileiro dd/MM/yyyy
            return LocalDate.parse(termo, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            try {
                // Tenta formato ISO yyyy-MM-dd (caso o usuário digite assim)
                return LocalDate.parse(termo);
            } catch (DateTimeParseException ex) {
                // Não é uma data válida
                return null;
            }
        }
    }

    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Aluno com ID " + id + " não foi encontrado."));
    }

    public Aluno salvar(Aluno aluno) {
        // 1. Verifica se já existe ALGUÉM com esse CPF
        // O repositório retorna o Aluno se achar, ou null se não achar (precisamos criar esse método findByCpf)
        Aluno alunoComCpfExistente = alunoRepository.findByCpf(aluno.getCpf());

        // 2. Validação:
        // Se achou alguém com esse CPF E (esse alguém não sou eu mesmo)
        if (alunoComCpfExistente != null && !alunoComCpfExistente.getId().equals(aluno.getId())) {
            throw new RegraNegocioException("Já existe um aluno cadastrado com o CPF: " + aluno.getCpf());
        }

        return alunoRepository.save(aluno);
    }

    public void excluir(Long id) {
        if (!alunoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(
                    "Impossível excluir. Aluno com ID " + id + " não existe.");
        }
        alunoRepository.deleteById(id);
    }
}
