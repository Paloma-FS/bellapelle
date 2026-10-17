package com.bellapelle.dto;

import com.bellapelle.repository.AgendamentoRepository.AgendamentoView;

import java.time.LocalDateTime;

public record AgendamentoResponse(
        Long id, Long idPaciente, String nomePaciente, Long idProcedimento, String nomeProcedimento,
        Long idProfissional, String nomeProfissional, Long idSala, String nomeSala,
        Long idEquipamento, String nomeEquipamento, LocalDateTime dataHora, Integer duracaoMinutos,
        String status, String tipoCancelamento
) {
    public static AgendamentoResponse de(AgendamentoView v) {
        return new AgendamentoResponse(v.getId(), v.getIdPaciente(), v.getNomePaciente(),
                v.getIdProcedimento(), v.getNomeProcedimento(), v.getIdProfissional(), v.getNomeProfissional(),
                v.getIdSala(), v.getNomeSala(), v.getIdEquipamento(), v.getNomeEquipamento(),
                v.getDataHora(), v.getDuracaoMinutos(), v.getStatus(), v.getTipoCancelamento());
    }
}