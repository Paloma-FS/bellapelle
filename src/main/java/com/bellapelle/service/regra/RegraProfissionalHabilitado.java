package com.bellapelle.service.regra;

import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.repository.AgendamentoRepository;
import com.bellapelle.service.ContextoAgendamento;
import org.springframework.stereotype.Component;

@Component
public class RegraProfissionalHabilitado implements RegraAgendamento {

    private final AgendamentoRepository repo;

    public RegraProfissionalHabilitado(AgendamentoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void validar(ContextoAgendamento ctx) {
        Long pf = ctx.req().idProfissional();
        if (repo.contarProfissionalAtivo(pf) == 0) {
            throw new RegraNegocioException("Profissional não encontrado ou inativo.");
        }
        if (repo.contarHabilitacao(pf, ctx.req().idProcedimento()) == 0) {
            throw new RegraNegocioException("Este profissional não realiza o procedimento escolhido.");
        }
    }
}