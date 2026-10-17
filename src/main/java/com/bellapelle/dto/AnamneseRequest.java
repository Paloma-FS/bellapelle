package com.bellapelle.dto;

import jakarta.validation.constraints.NotNull;

public record AnamneseRequest(
        String alergias,
        String medicacoesContinuas,
        String cirurgiasPrevias,
        @NotNull(message = "Informe se a paciente está gestante ou lactante.") Boolean gestanteOuLactante,
        @NotNull(message = "Informe se há uso de isotretinoína.") Boolean usoIsotretinoina,
        @NotNull(message = "Informe se há histórico de queloide.") Boolean historicoQueloide,
        String doencasDePele,
        String contraindicacoesIdentificadas
) {
}