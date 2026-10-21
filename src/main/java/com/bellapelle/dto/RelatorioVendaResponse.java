package com.bellapelle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RelatorioVendaResponse(
        String tipo,
        String nomeItem,
        Long quantidadeVendida,
        BigDecimal valorTotalVendas,
        LocalDate dataUltimaVenda
) {
}