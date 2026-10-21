package com.bellapelle.dto;

import java.time.LocalDate;

public record RelatorioFaltaResponse(
        String nomePaciente,
        LocalDate dataFalta,
        Long faltasAnteriores
) {
}