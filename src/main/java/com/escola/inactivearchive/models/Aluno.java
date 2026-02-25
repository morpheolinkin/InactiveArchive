package com.escola.inactivearchive.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "alunos")
@Entity
public class Aluno {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, unique = true)
    @CPF(message = "CPF inválido")
    private String cpf;
    @Column(name = "data_nascimento",nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;
}
