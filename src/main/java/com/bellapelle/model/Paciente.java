package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "paciente")
@Getter
@Setter
@NoArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 14, unique = true)
    private String cpf;

    private String endereco;

    @Column(length = 30)
    private String genero;

    private BigDecimal peso;

    private Integer altura;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(name = "restricoes_saude", columnDefinition = "TEXT")
    private String restricoesSaude;

    @Column(name = "preferencia_bebida", nullable = false)
    private String preferenciaBebida = "Sem preferência";

    @Column(name = "preferencia_profissional", nullable = false, length = 1)
    private String preferenciaProfissional = "I";

    @Column(name = "contato_emergencia_nome", length = 150)
    private String contatoEmergenciaNome;

    @Column(name = "contato_emergencia_telefone", length = 20)
    private String contatoEmergenciaTelefone;

    @Column(nullable = false)
    private String status = "Ativo";

    @Column(name = "canal_origem", length = 20)
    private String canalOrigem;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}