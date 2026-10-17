package com.bellapelle.service.regra;

import com.bellapelle.dto.AtendimentoRequest;
import com.bellapelle.exception.RegraNegocioException;
import org.springframework.stereotype.Component;

@Component
public class RegraDadosAtendimento {

    public void validar(AtendimentoRequest req, boolean injetavel) {
        if (req == null || vazio(req.descricaoEvolucao())) {
            throw new RegraNegocioException(
                    "Informe as anotações da sessão.");
        }

        if (!injetavel) {
            return;
        }

        if (vazio(req.fabricante())) {
            throw new RegraNegocioException(
                    "Informe o fabricante do injetável.");
        }

        if (req.fabricante().trim().length() > 150) {
            throw new RegraNegocioException(
                    "O fabricante deve ter no máximo 150 caracteres.");
        }

        if (vazio(req.numeroLote())) {
            throw new RegraNegocioException(
                    "Informe o lote do injetável.");
        }

        if (req.numeroLote().trim().length() > 50) {
            throw new RegraNegocioException(
                    "O lote deve ter no máximo 50 caracteres.");
        }

        if (req.validadeLote() == null) {
            throw new RegraNegocioException(
                    "Informe a validade do injetável.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}