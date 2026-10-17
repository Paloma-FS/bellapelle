package com.bellapelle.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProcedimentoRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 150) String nome,
        String descricao,
        @Size(max = 100) String categoria,
        @NotNull(message = "Valor é obrigatório") @DecimalMin(value = "0.0", message = "Valor não pode ser negativo") BigDecimal valor,
        @DecimalMin(value = "0.0", message = "Comissão mínima é 0") @DecimalMax(value = "100.0", message = "Comissão máxima é 100") BigDecimal percentualComissao,
        @NotNull(message = "Duração é obrigatória") @Min(value = 1, message = "Duração mínima é 1 minuto") Integer duracaoMinutos,
        Boolean requerEquipamento,
        @Size(max = 100) String tipoEquipamentoRequerido,
        String contraindicacoes,
        Boolean injetavel
) {
}