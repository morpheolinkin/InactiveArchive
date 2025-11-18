package com.escola.inactivearchive.controllers;

import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.services.AlunoService;
import com.escola.inactivearchive.services.RelatorioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/alunos")
public class AlunoController {
    public final AlunoService alunoService;
    public final RelatorioService relatorioService;

    public AlunoController(AlunoService alunoService, RelatorioService relatorioService) {
        this.alunoService = alunoService;
        this.relatorioService = relatorioService;
    }

    @GetMapping("/relatorio")
    public void gerarRelatorioPdf(HttpServletResponse response) throws IOException {
        // 1. Define que o retorno é um PDF
        response.setContentType("application/pdf");

        // 2. Define o cabeçalho para forçar o download com um nome bonito (com data e hora atual)
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=alunos_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        // 3. Chama o serviço para escrever o PDF direto na resposta
        relatorioService.exportarPdf(response);
    }

    // ATUALIZADO: Agora aceita um parâmetro de busca opcional
    @GetMapping
    public String listarAlunos(@RequestParam(value = "nome", required = false) String nome, Model model) {
        List<Aluno> alunos = alunoService.listarTodos(nome);
        model.addAttribute("alunos", alunos);
        // Envia o termo pesquisado de volta para o HTML (para manter no campo de busca)
        model.addAttribute("termoBusca", nome);
        return "lista-alunos";
    }

    @GetMapping("/novo")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("aluno", new Aluno());
        return "form-aluno";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable Long id, Model model) {
        // Se o ID não existir, o service lança RecursoNaoEncontradoException
        // O GlobalExceptionHandler pega o erro e mostra a tela bonita.
        // Não precisamos de 'if' aqui.
        Aluno aluno = alunoService.buscarPorId(id);

        model.addAttribute("aluno", aluno);
        return "form-aluno";
    }

    // NOVO: Exclusão
    @GetMapping("/excluir/{id}")
    public String excluirAluno(@PathVariable Long id) {
        alunoService.excluir(id);
        return "redirect:/alunos";
    }

    @PostMapping("/salvar")
    public String salvarAluno(@Valid @ModelAttribute("aluno") Aluno aluno, BindingResult result) { // 1. @Valid e BindingResult

        // 2. Se houver erro de validação (Ex: CPF inválido), volta para o formulário
        if (result.hasErrors()) {
            return "form-aluno";
        }

        alunoService.salvar(aluno);
        return "redirect:/alunos";
    }
}
