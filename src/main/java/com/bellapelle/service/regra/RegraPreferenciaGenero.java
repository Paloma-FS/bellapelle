package com.bellapelle.service.regra;

import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.repository.AgendamentoRepository;
import com.bellapelle.service.ContextoAgendamento;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class RegraPreferenciaGenero implements RegraAgendamento {

    private final AgendamentoRepository repo;

    public RegraPreferenciaGenero(AgendamentoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void validar(ContextoAgendamento ctx) {
        var info = repo.infoPreferenciaGenero(ctx.req().idPaciente(), ctx.req().idProfissional())
                .orElseThrow(() -> new RegraNegocioException("Paciente ou profissional não encontrado."));

        String pref = info.getPreferencia() == null ? "" : info.getPreferencia().trim().toUpperCase(Locale.ROOT);

        if ("I".equals(pref)) {
            return; // indiferente
        }

        String descricao;
        switch (pref) {
            case "F" -> descricao = "uma profissional mulher";
            case "M" -> descricao = "um profissional homem";
            default -> throw new RegraNegocioException(
                    "A preferência de atendimento da paciente deve ser F, M ou I. Revise o cadastro da paciente.");
        }

        String genero = info.getGenero() == null ? "" : info.getGenero().trim().toUpperCase(Locale.ROOT);
        if (genero.isEmpty()) {
            throw new RegraNegocioException(
                    "O profissional selecionado está sem gênero informado. Atualize o cadastro para validar a preferência da paciente.");
        }
        if (!Set.of("F", "M").contains(genero)) {
            throw new RegraNegocioException(
                    "O gênero do profissional selecionado deve ser F ou M para validar a preferência da paciente. Atualize o cadastro do profissional.");
        }

        if (!genero.equals(pref)) {
            throw new RegraNegocioException(
                    "A paciente prefere atendimento por " + descricao + ". Selecione um profissional compatível.");
        }
    }
}