package com.bellapelle.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PainelResponse(
        Resumo resumo,
        List<AgendaItem> agenda,
        List<Alerta> alertas,
        int janelaHoras
) {
    public record Resumo(
            long atendimentosHoje,
            long faltasNoMes,
            long parcelasVencidas,
            long pacientesSaldoBaixo
    ) {
    }

    public record AgendaItem(
            Long id,
            String nomePaciente,
            String nomeProcedimento,
            String nomeProfissional,
            String nomeSala,
            String nomeEquipamento,
            LocalDateTime dataHora,
            Integer duracaoMinutos,
            String status,
            boolean emAndamento
    ) {
    }

    public record Alerta(
            String tipo,
            String severidade,
            String titulo,
            String detalhe
    ) {
    }
}