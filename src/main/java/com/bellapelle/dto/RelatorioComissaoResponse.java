package com.bellapelle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RelatorioComissaoResponse(
        String nomeProfissional,
        BigDecimal valorComissao,
        String nomeProcedimento,
        LocalDate data
) {
}