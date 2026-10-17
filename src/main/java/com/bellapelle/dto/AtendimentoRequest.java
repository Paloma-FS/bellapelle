package com.bellapelle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AtendimentoRequest(
        @NotBlank(message = "Informe as anotações da sessão.")
        String descricaoEvolucao,

        @Size(max = 50, message = "O lote deve ter no máximo 50 caracteres.")
        String numeroLote,

        @Size(max = 150, message = "O fabricante deve ter no máximo 150 caracteres.")
        String fabricante,

        LocalDate validadeLote
) {
}