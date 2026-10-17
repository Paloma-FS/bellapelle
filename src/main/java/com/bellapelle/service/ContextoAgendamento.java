package com.bellapelle.service;

import com.bellapelle.dto.AgendamentoRequest;
import com.bellapelle.repository.AgendamentoRepository.ProcedimentoInfo;

import java.time.LocalDateTime;

public record ContextoAgendamento(AgendamentoRequest req, ProcedimentoInfo procedimento,
                                  LocalDateTime inicio, LocalDateTime fim, Long ignorarId) {
}