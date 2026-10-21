package com.bellapelle.dto;

import com.bellapelle.model.Parcela;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaResponse(
        Integer id, Integer numero, BigDecimal valor,
        LocalDate vencimento, LocalDate pagamento, String status
) {
    public static ParcelaResponse de(Parcela p) {
        return new ParcelaResponse(p.getId(), p.getNumeroParcela(), p.getValorParcela(),
                p.getDataVencimento(), p.getDataPagamento(), p.getStatus());
    }
}