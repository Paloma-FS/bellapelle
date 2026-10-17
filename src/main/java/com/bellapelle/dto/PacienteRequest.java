package com.bellapelle.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PacienteRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 150) String nome,
        @NotBlank(message = "Telefone é obrigatório") @Size(max = 20) String telefone,
        @Email(message = "E-mail inválido") String email,
        @NotNull(message = "Data de nascimento é obrigatória") @Past(message = "Data de nascimento deve estar no passado") LocalDate dataNascimento,
        @NotBlank(message = "CPF é obrigatório") String cpf,
        String endereco,
        String genero,
        @DecimalMin(value = "1.0", message = "Peso inválido") @DecimalMax(value = "500.0", message = "Peso inválido") BigDecimal peso,
        @Min(value = 30, message = "Altura inválida") @Max(value = 250, message = "Altura inválida") Integer altura,
        String alergias,
        String restricoesSaude,
        String preferenciaBebida,
        @NotBlank(message = "Preferência de profissional é obrigatória")
        @Pattern(regexp = "[FMI]", message = "Preferência de profissional deve ser F, M ou I")
        String preferenciaProfissional,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String canalOrigem,
        String observacoes
) {}