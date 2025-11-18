package com.escola.inactivearchive.services;

import com.escola.inactivearchive.models.Aluno;
import com.escola.inactivearchive.repository.AlunoRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RelatorioService {
    private final AlunoRepository alunoRepository;

    public RelatorioService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public void exportarPdf(HttpServletResponse response) throws IOException {
        // 1. Busca todos os dados
        List<Aluno> alunos = alunoRepository.findAll();

        // 2. Configuração do documento PDF
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // 3. Adiciona Título
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitulo.setSize(18);
        fontTitulo.setColor(Color.BLUE);

        Paragraph p = new Paragraph("Relatório de Alunos Inativos", fontTitulo);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        document.add(new Paragraph(" ")); // Espaço em branco

        // 4. Cria a Tabela (4 colunas: ID, Nome, CPF, Data)
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.0f, 3.5f, 2.0f, 2.0f}); // Largura relativa das colunas
        table.setSpacingBefore(10);

        // 5. Cabeçalho da Tabela
        escreverCabecalho(table);

        // 6. Dados da Tabela
        escreverDados(table, alunos);

        document.add(table);
        document.close();
    }

    private void escreverCabecalho(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        cell.setPhrase(new Phrase("ID", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Nome Completo", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("CPF", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Nascimento", font));
        table.addCell(cell);
    }

    private void escreverDados(PdfPTable table, List<Aluno> alunos) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Aluno aluno : alunos) {
            table.addCell(String.valueOf(aluno.getId()));
            table.addCell(aluno.getNome());
            table.addCell(aluno.getCpf());

            // Tratamento para data (caso seja nula por algum erro antigo, não quebra o PDF)
            if (aluno.getDataNascimento() != null) {
                table.addCell(aluno.getDataNascimento().format(formatter));
            } else {
                table.addCell("");
            }
        }
    }
}
