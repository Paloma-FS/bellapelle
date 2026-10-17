package com.bellapelle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SalaRequest(
        @NotBlank(message = "Nome da sala é obrigatório")
        @Size(max = 50)
        String nome,

        @Size(max = 255)
        String descricao,

        String status
) {
}