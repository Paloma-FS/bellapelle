package com.bellapelle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPacoteRequest(
        @NotNull(message = "Procedimento é obrigatório") Long idProcedimento,
        @NotNull(message = "Número de sessões é obrigatório") @Min(value = 1, message = "Mínimo de 1 sessão") Integer numeroSessoes,
        @Min(value = 1, message = "Periodicidade mínima é 1 dia") Integer periodicidadeDias
) {
}