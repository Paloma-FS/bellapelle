package com.bellapelle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoRequest(
        @NotNull(message = "Paciente é obrigatório") Long idPaciente,
        @NotNull(message = "Procedimento é obrigatório") Long idProcedimento,
        @NotNull(message = "Profissional é obrigatório") Long idProfissional,
        @NotNull(message = "Sala é obrigatória") Long idSala,
        Long idEquipamento,
        @NotNull(message = "Data e hora são obrigatórias") LocalDateTime dataHora,
        @Min(value = 1, message = "Duração mínima é 1 minuto") Integer duracaoMinutos
) {
}