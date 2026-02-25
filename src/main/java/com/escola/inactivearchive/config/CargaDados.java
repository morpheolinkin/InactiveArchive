package com.escola.inactivearchive.config;

import com.escola.inactivearchive.repository.AlunoRepository;
import com.escola.inactivearchive.util.GeradorCpf;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import javax.sql.DataSource;

@Configuration
public class CargaDados {

    @Bean
    CommandLineRunner iniciarBancoDeDados(AlunoRepository alunoRepository, JdbcTemplate jdbcTemplate) {
        return args -> {
            System.out.println(">>> VERIFICANDO IMPORTAÇÃO <<<");

            if (alunoRepository.count() == 0) {
                System.out.println(">>> Banco vazio. Preparando leitura do arquivo... <<<");

                try {
                    // 1. Detectar separador
                    char separador = ',';
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            new ClassPathResource("dados-iniciais.csv").getInputStream(), StandardCharsets.UTF_8))) {
                        String primeiraLinha = br.readLine();
                        if (primeiraLinha != null && primeiraLinha.contains(";")) {
                            separador = ';';
                        }
                    }

                    // 2. Configurar parser
                    CSVParser parser = new CSVParserBuilder().withSeparator(separador).build();
                    InputStreamReader reader = new InputStreamReader(
                            new ClassPathResource("dados-iniciais.csv").getInputStream(), StandardCharsets.UTF_8);

                    try (CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build()) {

                        String[] linha;
                        int importados = 0;
                        int ignorados = 0;
                        int linhaAtual = 0;

                        // Prepara o SQL de inserção direta (MUITO mais rápido e seguro para IDs fixos)
                        String sqlInsert = "INSERT INTO alunos (id, nome, cpf, data_nascimento) VALUES (?, ?, ?, ?)";

                        while ((linha = csvReader.readNext()) != null) {
                            linhaAtual++;

                            // Correção do erro da Linha 1: Ignora se for "N", "Nº", etc.
                            if (linha.length < 3 || linha[0].trim().startsWith("N") || linha[0].trim().isEmpty()) {
                                ignorados++;
                                continue;
                            }

                            try {
                                // Ler dados
                                Long idExcel = Long.parseLong(linha[0].trim());
                                String nome = linha[1].trim().toUpperCase();
                                String cpf = GeradorCpf.gerarCPF(); // Gera CPF fake válido

                                LocalDate dataNasc;
                                String dataStr = linha[2].trim();

                                // Tenta converter Data
                                try {
                                    // Tenta formato do Excel/CSV (geralmente vem como yyyy-MM-dd ou dd/MM/yyyy)
                                    if (dataStr.contains("/")) {
                                        dataNasc = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                    } else {
                                        dataNasc = LocalDate.parse(dataStr);
                                    }
                                } catch (Exception e) {
                                    // Se falhar, define data padrão nula ou fixa para não perder o registro
                                    // System.out.println("Aviso: Data inválida na linha " + linhaAtual + ". Usando data padrão.");
                                    dataNasc = LocalDate.of(2000, 1, 1);
                                }

                                // EXECUTA O INSERT DIRETO (Pula o Hibernate)
                                jdbcTemplate.update(sqlInsert, idExcel, nome, cpf, dataNasc);

                                importados++;

                            } catch (Exception e) {
                                System.err.println("ERRO CRÍTICO na linha " + linhaAtual + ": " + e.getMessage());
                                ignorados++;
                            }
                        }

                        System.out.println(">>> RESUMO: Importados: " + importados + " | Ignorados: " + ignorados + " <<<");

                        // Ajusta sequência de acordo com o banco ativo (Postgres x H2)
                        DataSource ds = jdbcTemplate.getDataSource();
                        if (ds == null) {
                            logger.warn("DataSource é null - não é possível ajustar sequência automaticamente.");
                        } else {
                            try (Connection conn = ds.getConnection()) {
                                String dbName = conn.getMetaData().getDatabaseProductName().toLowerCase();
                                if (dbName.contains("postgres")) {
                                    // Postgres: usa setval para a sequência
                                    jdbcTemplate.execute("SELECT setval(pg_get_serial_sequence('public.alunos', 'id'), COALESCE((SELECT MAX(id) FROM public.alunos) + 1, 1), false)");
                                } else if (dbName.contains("h2")) {
                                    // H2: reinicia a identity da coluna
                                    Long max = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM alunos", Long.class);
                                    long next = (max == null ? 1L : max + 1L);
                                    jdbcTemplate.execute("ALTER TABLE alunos ALTER COLUMN id RESTART WITH " + next);
                                } else {
                                    System.out.println(">>> Banco não suportado para ajuste automático de sequência: " + dbName + " - pulando.");
                                }
                                System.out.println(">>> Sequência do banco ajustada! <<<");
                            } catch (Exception e) {
                                logger.error(">>> Erro ajustando sequência: {}", e.getMessage(), e);
                            }
                        }

                    }

                } catch (Exception e) {
                    System.err.println(">>> ERRO GERAL: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println(">>> O Banco já tem dados. Nada foi feito. <<<");
            }
        };
    }
}
