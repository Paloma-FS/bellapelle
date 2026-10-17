package com.bellapelle.dto;

import com.bellapelle.model.Pacote;

import java.math.BigDecimal;

public record PacoteResponse(
        Long id, String nome, String descricao, BigDecimal valorTotal, Integer validadeDias, String status
) {
    public static PacoteResponse de(Pacote p) {
        return new PacoteResponse(p.getId(), p.getNome(), p.getDescricao(),
                p.getValorTotal(), p.getValidadeDias(), p.getStatus());
    }
}