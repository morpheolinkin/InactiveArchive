package com.escola.inactivearchive.controllers;

import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.services.AlunoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    // NOVO: Prepara a edição
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable Long id, Model model) {
        Aluno alunoExistente = alunoService.buscarPorId(id);

        if (alunoExistente != null) {
            model.addAttribute("aluno", alunoExistente);
            return "form-aluno"; // Reusa o mesmo formulário!
        }

        return "redirect:/alunos"; // Se não achar, volta pra lista
    }

    // NOVO: Exclusão
    @GetMapping("/excluir/{id}")
    public String excluirAluno(@PathVariable Long id) {
        alunoService.excluir(id);
        return "redirect:/alunos";
    }

    @PostMapping("/salvar")
    public String salvarAluno(@ModelAttribute("aluno") Aluno aluno) {
        alunoService.salvar(aluno);
        return "redirect:/alunos";
    }
}
