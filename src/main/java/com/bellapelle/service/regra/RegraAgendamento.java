package com.bellapelle.service.regra;

import com.bellapelle.service.ContextoAgendamento;

public interface RegraAgendamento {
    /** Lança RegraNegocioException se a regra for violada. */
    void validar(ContextoAgendamento ctx);
}