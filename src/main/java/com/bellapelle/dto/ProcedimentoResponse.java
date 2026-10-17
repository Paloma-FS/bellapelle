package com.bellapelle.dto;

import com.bellapelle.model.Procedimento;

import java.math.BigDecimal;

public record ProcedimentoResponse(
        Long id, String nome, String descricao, String categoria, BigDecimal valor,
        BigDecimal percentualComissao, Integer duracaoMinutos, Boolean requerEquipamento,
        String tipoEquipamentoRequerido, String contraindicacoes, Boolean injetavel, String status
) {
    public static ProcedimentoResponse de(Procedimento p) {
        return new ProcedimentoResponse(
                p.getId(), p.getNome(), p.getDescricao(), p.getCategoria(), p.getValor(),
                p.getPercentualComissao(), p.getDuracaoMinutos(), p.getRequerEquipamento(),
                p.getTipoEquipamentoRequerido(), p.getContraindicacoes(), p.getInjetavel(), p.getStatus());
    }
}