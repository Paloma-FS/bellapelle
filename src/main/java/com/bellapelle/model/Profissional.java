package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "profissional")
@Getter
@Setter
@NoArgsConstructor
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profissional")
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 14, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "genero", length = 30)
    private String genero;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(name = "numero_conselho", length = 30)
    private String numeroConselho;

    @Column(name = "tipo_conselho", length = 20)
    private String tipoConselho;

    @Column(name = "uf_conselho", length = 2)
    private String ufConselho;

    @Column(nullable = false, length = 100)
    private String funcao;

    @Column(name = "tipo_vinculo", nullable = false)
    private String tipoVinculo;

    @Column(name = "percentual_comissao")
    private BigDecimal percentualComissao;

    @Column(name = "dias_horario_trabalho")
    private String diasHorarioTrabalho;

    @Column(nullable = false)
    private String status = "Ativo";

    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

    @Column(name = "data_desligamento")
    private LocalDate dataDesligamento;
}