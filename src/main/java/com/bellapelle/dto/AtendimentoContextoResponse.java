package com.bellapelle.dto;

import java.time.LocalDate;
import java.util.List;

public record AtendimentoContextoResponse(
        AgendamentoResponse agendamento,
        boolean injetavel,
        List<SessaoAtendimentoResponse> historico,
        List<ProcedimentoDisponivel> procedimentosDisponiveis
) {

    public record ProcedimentoDisponivel(
            Long idProcedimento,
            String nomeProcedimento,
            Integer quantidadeDisponivel,
            LocalDate dataUltimaSessao,
            boolean injetavel
    ) {
    }
}