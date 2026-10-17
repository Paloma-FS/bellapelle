package com.bellapelle.dto;

import java.util.List;

public record AtendimentoContextoResponse(
        AgendamentoResponse agendamento,
        boolean injetavel,
        List<SessaoAtendimentoResponse> historico
) {
}