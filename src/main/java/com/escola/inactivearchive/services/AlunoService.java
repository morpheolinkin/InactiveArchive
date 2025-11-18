package com.escola.inactivearchive.services;

import com.escola.inactivearchive.exception.RecursoNaoEncontradoException;
import com.escola.inactivearchive.exception.RegraNegocioException;
import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.repository.AlunoRepository;
import org.springframework.data.domain.*;
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

    /**
     * Lista alunos com paginação e ordenação.
     * @param termoBusca Nome, CPF ou Data (pode ser nulo).
     * @param pagina Número da página atual (começa em 0).
     * @return Uma Página (Page) de alunos.
     */
    public Page<Aluno> listarTodos(String termoBusca, int pagina) {
        int itensPorPagina = 10; // Define quantos alunos aparecem por página

        // IMPORTANTE: Sort.by("id").ascending() garante que o 177 apareça antes do 2288
        Pageable pageable = PageRequest.of(pagina, itensPorPagina, Sort.by("id").ascending());

        if (termoBusca != null && !termoBusca.isBlank()) {

            // 1. Tenta verificar se é uma busca por DATA
            LocalDate dataBusca = tentarConverterParaData(termoBusca);
            if (dataBusca != null) {
                // A busca por data retorna List, então convertemos manualmente para Page
                List<Aluno> listaPorData = alunoRepository.findByDataNascimento(dataBusca);
                // Cria uma página contendo a lista inteira (sem quebra de página para datas, pois são poucos registos)
                return new PageImpl<>(listaPorData, pageable, listaPorData.size());
            }

            // 2. Se não for data, busca por NOME ou CPF (paginado pelo banco)
            return alunoRepository.buscarPorNomeOuCpf(termoBusca, pageable);
        }

        // 3. Se não tiver busca, retorna tudo paginado e ordenado
        return alunoRepository.findAll(pageable);
    }

    /**
     * Método auxiliar para o Relatório PDF (que precisa da lista completa, sem páginas).
     */
    public List<Aluno> listarTodosParaRelatorio() {
        return alunoRepository.findAll(Sort.by("id").ascending());
    }

    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno com ID " + id + " não encontrado."));
    }

    public Aluno salvar(Aluno aluno) {
        // Verifica duplicidade de CPF apenas na criação ou se o CPF mudou na edição
        Aluno alunoExistente = alunoRepository.findByCpf(aluno.getCpf());

        if (alunoExistente != null && !alunoExistente.getId().equals(aluno.getId())) {
            throw new RegraNegocioException("Já existe um aluno cadastrado com o CPF: " + aluno.getCpf());
        }

        return alunoRepository.save(aluno);
    }

    public void excluir(Long id) {
        if (!alunoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Impossível excluir. Aluno com ID " + id + " não existe.");
        }
        alunoRepository.deleteById(id);
    }

    // Método utilitário para detetar data
    private LocalDate tentarConverterParaData(String termo) {
        try {
            return LocalDate.parse(termo, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(termo); // Tenta yyyy-MM-dd
            } catch (DateTimeParseException ex) {
                return null; // Não é data
            }
        }
    }
}
