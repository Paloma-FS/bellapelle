package com.bellapelle.service.regra;

import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.service.ContextoAgendamento;
import com.bellapelle.service.HorarioClinica;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.Set;

@Component
public class RegraHorarioFuncionamento implements RegraAgendamento {

    private static final Set<DayOfWeek> DIAS_FUNCIONAMENTO = Set.of(
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    );

    @Override
    public void validar(ContextoAgendamento ctx) {
        boolean diaFechado =
                !DIAS_FUNCIONAMENTO.contains(ctx.inicio().getDayOfWeek());

        boolean antes = ctx.inicio()
                .toLocalTime()
                .isBefore(HorarioClinica.ABERTURA);

        boolean outroDia = !ctx.fim()
                .toLocalDate()
                .equals(ctx.inicio().toLocalDate());

        boolean depois = ctx.fim()
                .toLocalTime()
                .isAfter(HorarioClinica.FECHAMENTO);

        if (diaFechado) {
            throw new RegraNegocioException(
                    "A clínica funciona de terça-feira a sábado."
            );
        }

        if (antes || outroDia || depois) {
            throw new RegraNegocioException(
                    "Fora do horário de atendimento: início a partir das 09:00 e término até as 19:00."
            );
        }
    }
}