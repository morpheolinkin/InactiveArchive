package com.escola.inactivearchive.controllers;

import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.services.AlunoService;
import com.escola.inactivearchive.services.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Controller
@RequestMapping("/alunos")
@Tag(name = "Alunos", description = "Gerenciamento de cadastro, edição e listagem de alunos") // <--- Título da seção
public class AlunoController {

    private final AlunoService alunoService;
    private final RelatorioService relatorioService;

    public AlunoController(AlunoService alunoService, RelatorioService relatorioService) {
        this.alunoService = alunoService;
        this.relatorioService = relatorioService;
    }

    @GetMapping
    @Operation(summary = "Listar Alunos",
            description = "Retorna a página HTML com a lista paginada de alunos. Permite busca por nome/cpf.") // <--- Explicação
    public String listarAlunos(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "page", defaultValue = "0") int page, // Recebe a página da URL (ex: ?page=1)
            Model model) {

        // Chama o serviço pedindo a página X
        Page<Aluno> paginaAlunos = alunoService.listarTodos(nome, page);

        // Passa o CONTEÚDO da página (a lista de 10 alunos)
        model.addAttribute("alunos", paginaAlunos.getContent());

        // Passa metadados para controlar os botões "Anterior/Próximo" no HTML
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", paginaAlunos.getTotalPages());
        model.addAttribute("totalItens", paginaAlunos.getTotalElements());

        // Mantém o termo pesquisado na barra de busca
        model.addAttribute("termoBusca", nome);

        return "lista-alunos";
    }

    @GetMapping("/novo")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("aluno", new Aluno());
        return "form-aluno";
    }

    @GetMapping("/editar/{id}")
    @Operation(
            summary = "Mostrar formulário de edição",
            description = "Carrega os dados do aluno identificado por 'id' e exibe o formulário para edição.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID do aluno", required = true)
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Formulário de edição carregado com sucesso"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Aluno não encontrado")
            }
    )
    public String mostrarFormularioEdicao(@PathVariable Long id, Model model) {
        Aluno aluno = alunoService.buscarPorId(id);
        model.addAttribute("aluno", aluno);
        return "form-aluno";
    }

    @PostMapping("/salvar")
    @Operation(summary = "Salvar Aluno",
            description = "Recebe os dados do formulário e salva um novo aluno ou atualiza um existente.")
    public String salvarAluno(@Valid @ModelAttribute("aluno") Aluno aluno, BindingResult result, Model model) {
        // Se houver erro de validação (CPF inválido, Nome vazio), volta para o form
        if (result.hasErrors()) {
            return "form-aluno";
        }

        alunoService.salvar(aluno);
        return "redirect:/alunos";
    }

    @GetMapping("/excluir/{id}")
    @Operation(summary = "Excluir Aluno", description = "Exclui um aluno pelo ID.")
    public String excluirAluno(@PathVariable Long id) {
        alunoService.excluir(id);
        return "redirect:/alunos";
    }

    @GetMapping("/relatorio")
    @Operation(summary = "Gerar Relatório PDF",
            description = "Gera e baixa um relatório em PDF contendo a lista de todos os alunos.")
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
