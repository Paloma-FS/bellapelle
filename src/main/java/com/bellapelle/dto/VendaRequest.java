package com.bellapelle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VendaRequest(
        Integer idPaciente,
        Long idPacote,
        Long idProcedimento,
        String formaPagamento,
        LocalDate dataVenda,
        BigDecimal valorEntrada,
        Integer numeroParcelas
) {
}