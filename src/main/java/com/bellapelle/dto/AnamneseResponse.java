package com.bellapelle.dto;

import java.time.LocalDate;

public record AnamneseResponse(
        LocalDate dataPreenchimento,
        String alergias,
        String medicacoesContinuas,
        String cirurgiasPrevias,
        Boolean gestanteOuLactante,
        Boolean usoIsotretinoina,
        Boolean historicoQueloide,
        String doencasDePele,
        String contraindicacoesIdentificadas
) {
}