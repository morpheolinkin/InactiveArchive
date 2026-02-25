package com.escola.inactivearchive.controllers;

import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.services.AlunoService;
import com.escola.inactivearchive.services.RelatorioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controller Spring MVC responsável por gerenciar operações relacionadas a alunos.
 *
 * <p>Fornece endpoints para:
 * <ul>
 *   <li>Listar alunos com paginação e filtro por nome</li>
 *   <li>Exibir formulário de cadastro/edição</li>
 *   <li>Salvar e excluir alunos</li>
 *   <li>Gerar relatório em PDF</li>
 * </ul>
 *
 * Mapeado em '/alunos'.
 * Depende de {@link com.escola.inactivearchive.services.AlunoService} e
 * {@link com.escola.inactivearchive.services.RelatorioService}.
 *
 * @since 1.0
 */
@Controller
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final RelatorioService relatorioService;

    /**
     * Construtor do controlador de alunos.
     *
     * @param alunoService Serviço responsável por operações relacionadas a alunos.
     * @param relatorioService Serviço responsável por gerar relatórios.
     */
    public AlunoController(AlunoService alunoService, RelatorioService relatorioService) {
        this.alunoService = alunoService;
        this.relatorioService = relatorioService;
    }

    /**
     * Lista alunos com suporte a paginação e filtro por nome.
     *
     * @param nome Nome do aluno para filtro (opcional).
     * @param page Número da página a ser exibida (padrão: 0).
     * @param model Objeto para adicionar atributos à view.
     * @return Nome da view para exibir a lista de alunos.
     */
    @GetMapping
    public String listarAlunos(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Page<Aluno> paginaAlunos = alunoService.listarTodos(nome, page);
        model.addAttribute("alunos", paginaAlunos.getContent());
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", paginaAlunos.getTotalPages());
        model.addAttribute("totalItens", paginaAlunos.getTotalElements());
        model.addAttribute("termoBusca", nome);

        return "lista-alunos";
    }

    /**
     * Exibe o formulário para cadastrar um novo aluno.
     *
     * @param model Objeto para adicionar atributos à view.
     * @return Nome da view para o formulário de cadastro.
     */
    @GetMapping("/novo")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("aluno", new Aluno());

        // Envia o próximo ID calculado para a caixinha azul no HTML
        model.addAttribute("proximoId", alunoService.obterProximoId());

        return "form-aluno";
    }

    /**
     * Exibe o formulário para editar um aluno existente.
     *
     * @param id ID do aluno a ser editado.
     * @param model Objeto para adicionar atributos à view.
     * @return Nome da view para o formulário de edição.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable Long id, Model model) {
        Aluno aluno = alunoService.buscarPorId(id);
        model.addAttribute("aluno", aluno);
        return "form-aluno";
    }

    /**
     * Salva um aluno (novo ou editado) no banco de dados.
     *
     * @param aluno Objeto aluno a ser salvo.
     * @param result Resultado da validação do formulário.
     * @param model Objeto para adicionar atributos à view.
     * @return Redireciona para a lista de alunos ou retorna ao formulário em caso de erro.
     */
    @PostMapping("/salvar")
    public String salvarAluno(@Valid @ModelAttribute("aluno") Aluno aluno, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mensagemErro", "Corrija os erros do formulário.");
            model.addAttribute("erros", result.getAllErrors());
            return "form-aluno";
        }

        alunoService.salvar(aluno);
        return "redirect:/alunos";
    }

    /**
     * Exclui um aluno pelo ID.
     *
     * @param id ID do aluno a ser excluído.
     * @return Redireciona para a lista de alunos.
     */
    @GetMapping("/excluir/{id}")
    public String excluirAluno(@PathVariable Long id) {
        alunoService.excluir(id);
        return "redirect:/alunos";
    }

    /**
     * Gera um relatório em PDF contendo os dados dos alunos.
     *
     * @param response Objeto HttpServletResponse para configurar o cabeçalho e o conteúdo do PDF.
     * @throws IOException Se ocorrer um erro ao escrever o PDF na resposta.
     */
    @GetMapping("/relatorio")
    public void gerarRelatorioPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=alunos_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        relatorioService.exportarPdf(response);
    }
}