package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "sessao_atendimento")
@Getter
@Setter
@NoArgsConstructor
public class SessaoAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sessao")
    private Long id;

    @Column(name = "id_agendamento", nullable = false, unique = true)
    private Long idAgendamento;

    @Column(name = "id_prontuario", nullable = false)
    private Long idProntuario;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "id_procedimento", nullable = false)
    private Long idProcedimento;

    @Column(name = "id_profissional", nullable = false)
    private Long idProfissional;

    @Column(name = "id_equipamento")
    private Long idEquipamento;

    @Column(name = "regiao_tratada", length = 150)
    private String regiaoTratada;

    @Column(name = "tecnica_utilizada", length = 150)
    private String tecnicaUtilizada;

    @Column(name = "numero_lote", length = 50)
    private String numeroLote;

    @Column(name = "fabricante", length = 150)
    private String fabricante;

    @Column(name = "validade_lote")
    private LocalDate validadeLote;

    @Column(name = "descricao_evolucao", columnDefinition = "text")
    private String descricaoEvolucao;

    @Column(name = "intercorrencias", columnDefinition = "text")
    private String intercorrencias;
}