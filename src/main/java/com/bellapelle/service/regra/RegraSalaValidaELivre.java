package com.bellapelle.service.regra;

import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.repository.AgendamentoRepository;
import com.bellapelle.service.ContextoAgendamento;
import org.springframework.stereotype.Component;

@Component
public class RegraSalaValidaELivre implements RegraAgendamento {

    private final AgendamentoRepository repo;

    public RegraSalaValidaELivre(AgendamentoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void validar(ContextoAgendamento ctx) {
        String status = repo.statusSala(ctx.req().idSala())
                .orElseThrow(() -> new RegraNegocioException("Sala não encontrada."));
        if (!"Ativa".equals(status)) {
            throw new RegraNegocioException("Sala não está ativa.");
        }
        if (repo.conflitosSala(ctx.req().idSala(), ctx.inicio(), ctx.fim(), ctx.ignorarId()) > 0) {
            throw new RegraNegocioException("Sala já está ocupada neste horário.");
        }
    }
}