package com.bellapelle.service.regra;

import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.repository.AgendamentoRepository;
import com.bellapelle.service.ContextoAgendamento;
import org.springframework.stereotype.Component;

@Component
public class RegraEquipamento implements RegraAgendamento {

    private final AgendamentoRepository repo;

    public RegraEquipamento(AgendamentoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void validar(ContextoAgendamento ctx) {
        Long idEq = ctx.req().idEquipamento();
        boolean requer = ctx.procedimento().getRequer() != null && ctx.procedimento().getRequer() == 1;
        if (idEq == null) {
            if (requer) {
                throw new RegraNegocioException("Este procedimento exige um equipamento do tipo "
                        + ctx.procedimento().getTipoEquipamento() + ".");
            }
            return;
        }
        var eq = repo.infoEquipamento(idEq)
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado."));
        if (!"Disponível".equals(eq.getStatus())) {
            throw new RegraNegocioException("Equipamento não está disponível.");
        }
        if (requer && !eq.getTipo().equals(ctx.procedimento().getTipoEquipamento())) {
            throw new RegraNegocioException("Equipamento incompatível: o procedimento exige tipo "
                    + ctx.procedimento().getTipoEquipamento() + ".");
        }
        if (repo.conflitosEquipamento(idEq, ctx.inicio(), ctx.fim(), ctx.ignorarId()) > 0) {
            throw new RegraNegocioException("Equipamento já está em uso neste horário.");
        }
    }
}