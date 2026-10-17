package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "procedimento")
@Getter
@Setter
@NoArgsConstructor
public class Procedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_procedimento")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(length = 100)
    private String categoria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "percentual_comissao", precision = 5, scale = 2)
    private BigDecimal percentualComissao;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Column(name = "requer_equipamento", nullable = false)
    private Boolean requerEquipamento = false;

    @Column(name = "tipo_equipamento_requerido", length = 100)
    private String tipoEquipamentoRequerido;

    @Column(columnDefinition = "text")
    private String contraindicacoes;

    @Column(name = "is_injetavel", nullable = false)
    private Boolean injetavel = false;

    @Column(nullable = false, length = 20)
    private String status = "Ativo";
}