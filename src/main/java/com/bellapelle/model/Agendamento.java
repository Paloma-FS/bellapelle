package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
@Getter
@Setter
@NoArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agendamento")
    private Long id;

    @Column(name = "id_paciente", nullable = false)
    private Long idPaciente;

    @Column(name = "id_procedimento", nullable = false)
    private Long idProcedimento;

    @Column(name = "id_profissional", nullable = false)
    private Long idProfissional;

    @Column(name = "id_sala", nullable = false)
    private Long idSala;

    @Column(name = "id_equipamento")
    private Long idEquipamento;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Column(name = "status", nullable = false)
    private String status = "Agendado";

    @Column(name = "tipo_cancelamento")
    private String tipoCancelamento;

    @Column(name = "anexo_justificativa_medica")
    private String anexoJustificativaMedica;
}