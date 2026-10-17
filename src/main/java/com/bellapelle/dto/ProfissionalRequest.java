package com.bellapelle.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProfissionalRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150)
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        String genero,

        @Size(max = 20)
        String telefone,

        @Email(message = "E-mail inválido")
        String email,

        @Size(max = 30)
        String numeroConselho,

        @Size(max = 20)
        String tipoConselho,

        @Size(min = 2, max = 2, message = "UF deve ter 2 letras")
        String ufConselho,

        @NotBlank(message = "Função é obrigatória")
        @Size(max = 100)
        String funcao,

        @NotBlank(message = "Tipo de vínculo é obrigatório")
        String tipoVinculo,

        @DecimalMin(
                value = "0.0",
                message = "Comissão mínima é 0"
        )
        @DecimalMax(
                value = "100.0",
                message = "Comissão máxima é 100"
        )
        BigDecimal percentualComissao,

        @Size(max = 255)
        String diasHorarioTrabalho,

        LocalDate dataAdmissao,

        List<
                @NotNull(message = "Procedimento inválido")
                @Positive(message = "O identificador do procedimento deve ser positivo")
                Long
                > procedimentosIds
) {
}