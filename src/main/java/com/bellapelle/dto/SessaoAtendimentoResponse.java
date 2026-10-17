package com.bellapelle.dto;

import com.bellapelle.repository.SessaoAtendimentoRepository.HistoricoView;

import java.time.LocalDate;

public record SessaoAtendimentoResponse(
        Long id,
        LocalDate data,
        String nomeProcedimento,
        String nomeProfissional,
        String descricaoEvolucao,
        String intercorrencias,
        String numeroLote,
        String fabricante,
        LocalDate validadeLote
) {
    public static SessaoAtendimentoResponse de(HistoricoView v) {
        return new SessaoAtendimentoResponse(
                v.getId(),
                v.getData(),
                v.getNomeProcedimento(),
                v.getNomeProfissional(),
                v.getDescricaoEvolucao(),
                v.getIntercorrencias(),
                v.getNumeroLote(),
                v.getFabricante(),
                v.getValidadeLote());
    }
}