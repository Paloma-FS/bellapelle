package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "saldo_sessao")
@Getter
@Setter
@NoArgsConstructor
public class SaldoSessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_saldo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_procedimento", nullable = false)
    private Procedimento procedimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venda_origem", nullable = false)
    private Venda vendaOrigem;

    @Column(name = "quantidade_total", nullable = false)
    private Integer quantidadeTotal;

    @Column(name = "quantidade_utilizada", nullable = false)
    private Integer quantidadeUtilizada = 0;

    // coluna gerada pelo banco (quantidade_total - quantidade_utilizada): somente leitura
    @Column(name = "quantidade_disponivel", insertable = false, updatable = false)
    private Integer quantidadeDisponivel;

    @Column(name = "data_ultima_sessao")
    private LocalDate dataUltimaSessao;
}