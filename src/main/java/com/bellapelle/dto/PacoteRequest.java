package com.bellapelle.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PacoteRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 150) String nome,
        String descricao,
        @NotNull(message = "Valor total é obrigatório") @DecimalMin(value = "0.0", message = "Valor não pode ser negativo") BigDecimal valorTotal,
        @NotNull(message = "Validade é obrigatória") @Min(value = 1, message = "Validade mínima é 1 dia") Integer validadeDias,
        @NotEmpty(message = "O pacote deve ter ao menos um procedimento.")
        List<@NotNull(message = "Item inválido.") @Valid ItemPacoteRequest> itens
) {
}