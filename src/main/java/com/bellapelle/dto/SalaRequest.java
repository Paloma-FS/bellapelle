package com.bellapelle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalaRequest(
        @NotNull(message = "O número da sala é obrigatório")
        @Min(value = 1, message = "O número da sala deve ser maior que zero")
        @Max(value = 9999, message = "O número da sala deve ter no máximo 4 dígitos")
        Integer numero,

        // Opcional: se vier vazio, o sistema usa "Sala <número>".
        @Size(max = 50)
        String nome,

        @Size(max = 255)
        String descricao,

        String status
) {
}