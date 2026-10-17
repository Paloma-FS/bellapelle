package com.bellapelle.dto;

import com.bellapelle.model.ItemPacote;

import java.math.BigDecimal;

public record ItemPacoteResponse(
        Long id, Long idPacote, Long idProcedimento, String nomeProcedimento,
        BigDecimal valorProcedimento, Integer numeroSessoes, Integer periodicidadeDias
) {
    public static ItemPacoteResponse de(ItemPacote i) {
        return new ItemPacoteResponse(
                i.getId(), i.getPacote().getId(), i.getProcedimento().getId(),
                i.getProcedimento().getNome(), i.getProcedimento().getValor(),
                i.getNumeroSessoes(), i.getPeriodicidadeDias());
    }
}