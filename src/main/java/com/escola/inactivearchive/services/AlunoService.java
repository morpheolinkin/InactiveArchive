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

/**
 * Serviço responsável pelas Regras de Negócio relacionadas aos Alunos.
 * <p>
 * Aqui são realizadas as validações de CPF duplicado, conversões de data
 * para a busca inteligente e a lógica de paginação.
 * </p>
 *
 * @author Jefferson Medeiros
 * @since 1.0
 */
@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    /**
     * Realiza a busca paginada de alunos.
     * <p>
     * Este método identifica automaticamente se o termo de busca é uma DATA ou um TEXTO.
     * Se for data, busca exata. Se for texto, busca parcial por Nome ou CPF.
     * </p>
     *
     * @param termoBusca Pode ser um Nome, um CPF ou uma Data (dd/MM/yyyy ou yyyy-MM-dd).
     * @param pagina     O índice da página atual (inicia em 0).
     * @return Uma página (Page) contendo os alunos encontrados, ordenada por ‘ID’.
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
     * <p>
     * public List<Aluno> listarTodosParaRelatorio() {
     * return alunoRepository.findAll(Sort.by("id").ascending());
     * }
     */

    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno com ID " + id + " não encontrado."));
    }

    public Long obterProximoId() {
        return alunoRepository.obterMaiorId() + 1;
    }

    /**
     * Salva ou atualiza um aluno no banco de dados.
     * <p>
     * Regra de Negócio: Não permite dois alunos com o mesmo CPF,
     * exceto se for o próprio aluno sendo editado.
     * </p>
     *
     * @param aluno Objeto aluno vindo do formulário.
     * @throws RegraNegocioException se o CPF já existir em outro cadastro.
     */
    public void salvar(Aluno aluno) {
        // 1. VALIDAÇÃO DE CPF
        // Busca no banco se já existe alguém com esse CPF
        Aluno alunoExistente = alunoRepository.findByCpf(aluno.getCpf());

        if (alunoExistente != null) {
            // Entramos aqui se o CPF já existe no banco. Mas precisamos saber:
            // É um aluno NOVO tentando roubar um CPF? (aluno.getId() == null)
            // OU é uma EDIÇÃO de um aluno tentando usar o CPF de outro? (!alunoExistente.getId().equals(aluno.getId()))
            if (aluno.getId() == null || !alunoExistente.getId().equals(aluno.getId())) {
                throw new RegraNegocioException("Já existe um aluno cadastrado com o CPF: " + aluno.getCpf());
            }
        }

        // 2. ATRIBUIÇÃO MANUAL DO ID SEQUENCIAL
        // Se o ID for nulo, significa que é um NOVO cadastro.
        // É aqui que garantimos que ele pegue o próximo número sem deixar buracos!
        if (aluno.getId() == null) {
            Long proximoId = obterProximoId();
            aluno.setId(proximoId);
        }

        // Se for edição (ID não nulo), o Spring Data JPA vai apenas atualizar (UPDATE).
        // Se for novo (ID que acabamos de setar), ele vai inserir (INSERT).
        alunoRepository.save(aluno);
    }

    public void excluir(Long id) {
        if (!alunoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Impossível excluir. Aluno com ID " + id + " não existe.");
        }
        alunoRepository.deleteById(id);
    }

    /**
     * Tenta converter uma ‘String’ genérica para um objeto LocalDate.
     * Suporta formatos brasileiros (dd/MM/yyyy) e ISO (yyyy-MM-dd).
     *
     * @param termo A ‘string’ digitada pelo usuário.
     * @return LocalDate se a conversão for bem-sucedida, ou null se não for uma data.
     */
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
