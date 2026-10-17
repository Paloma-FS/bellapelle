package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pacote")
@Getter
@Setter
@NoArgsConstructor
public class Pacote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pacote")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "validade_dias", nullable = false)
    private Integer validadeDias = 365;

    @Column(nullable = false, length = 20)
    private String status = "Ativo";
}