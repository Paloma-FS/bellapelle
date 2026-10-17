package com.bellapelle.service.regra;

import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.repository.AgendamentoRepository;
import com.bellapelle.service.ContextoAgendamento;
import org.springframework.stereotype.Component;

@Component
public class RegraProfissionalLivre implements RegraAgendamento {

    private final AgendamentoRepository repo;

    public RegraProfissionalLivre(AgendamentoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void validar(ContextoAgendamento ctx) {
        if (repo.conflitosProfissional(ctx.req().idProfissional(), ctx.inicio(), ctx.fim(), ctx.ignorarId()) > 0) {
            throw new RegraNegocioException("Profissional já tem agendamento neste horário.");
        }
    }
}