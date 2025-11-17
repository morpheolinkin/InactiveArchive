package com.escola.inactivearchive.controllers;

import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.services.AlunoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/alunos")
public class AlunoController {
    public final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
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
    public String salvarAluno(@Valid @ModelAttribute("aluno")
                                  Aluno aluno, BindingResult result, Model model) { // 1. @Valid e BindingResult

        // 2. Se houver erro de validação (Ex: CPF inválido), volta para o formulário
        if (result.hasErrors()) {
            return "form-aluno";
        }

        alunoService.salvar(aluno);
        return "redirect:/alunos";
    }
}
