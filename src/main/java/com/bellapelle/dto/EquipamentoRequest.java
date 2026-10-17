package com.bellapelle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EquipamentoRequest(
        @NotBlank(message = "Nome/modelo é obrigatório")
        @Size(max = 150)
        String nomeModelo,

        @NotBlank(message = "Tipo é obrigatório")
        @Size(max = 100)
        String tipo,

        @Size(max = 100)
        String numeroSerie,

        Long idSala,

        String status,

        LocalDate ultimaManutencao,

        LocalDate proximaManutencao
) {
}