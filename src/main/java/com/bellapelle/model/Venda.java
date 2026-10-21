package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venda")
@Getter
@Setter
@NoArgsConstructor
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venda")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pacote")
    private Pacote pacote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_procedimento")
    private Procedimento procedimento;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_venda", nullable = false)
    private LocalDate dataVenda;

    @Column(name = "forma_pagamento", nullable = false)
    private String formaPagamento;

    @Column(name = "valor_entrada", precision = 10, scale = 2)
    private BigDecimal valorEntrada;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Parcela> parcelas = new ArrayList<>();
}