package com.bellapelle.dto;

import java.time.LocalDate;
import java.util.List;

public record ProntuarioConsultaResponse(
        LocalDate dataCriacao,
        AnamneseProntuario anamnese,
        List<SessaoProntuario> sessoes
) {
    public record AnamneseProntuario(
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

    public record SessaoProntuario(
            LocalDate data,
            String procedimento,
            String profissional,
            String equipamento,
            String regiaoTratada,
            String tecnicaUtilizada,
            String numeroLote,
            LocalDate validadeLote,
            String descricaoEvolucao,
            String intercorrencias
    ) {
    }
}