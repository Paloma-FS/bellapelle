package com.bellapelle.service;

import com.bellapelle.dto.AtendimentoContextoResponse;
import com.bellapelle.dto.AtendimentoRequest;
import com.bellapelle.dto.SessaoAtendimentoResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.Agendamento;
import com.bellapelle.model.Prontuario;
import com.bellapelle.model.SessaoAtendimento;
import com.bellapelle.repository.AgendamentoRepository;
import com.bellapelle.repository.ProcedimentoRepository;
import com.bellapelle.repository.ProntuarioRepository;
import com.bellapelle.repository.SessaoAtendimentoRepository;
import com.bellapelle.service.regra.RegraDadosAtendimento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
public class AtendimentoService {

    private final AgendamentoRepository agendamentos;
    private final ProcedimentoRepository procedimentos;
    private final ProntuarioRepository prontuarios;
    private final SessaoAtendimentoRepository sessoes;
    private final AgendamentoService agendamentoService;
    private final RegraDadosAtendimento regraDados;

    public AtendimentoService(
            AgendamentoRepository agendamentos,
            ProcedimentoRepository procedimentos,
            ProntuarioRepository prontuarios,
            SessaoAtendimentoRepository sessoes,
            AgendamentoService agendamentoService,
            RegraDadosAtendimento regraDados) {
        this.agendamentos = agendamentos;
        this.procedimentos = procedimentos;
        this.prontuarios = prontuarios;
        this.sessoes = sessoes;
        this.agendamentoService = agendamentoService;
        this.regraDados = regraDados;
    }

    @Transactional(readOnly = true)
    public AtendimentoContextoResponse consultar(Long idAgendamento) {
        var agendamento = agendamentoService.buscar(idAgendamento);

        var procedimento = procedimentos
                .findById(agendamento.idProcedimento())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Procedimento não encontrado."));

        var historico = sessoes
                .historicoPaciente(agendamento.idPaciente())
                .stream()
                .map(SessaoAtendimentoResponse::de)
                .toList();

        return new AtendimentoContextoResponse(
                agendamento,
                Boolean.TRUE.equals(procedimento.getInjetavel()),
                historico);
    }

    @Transactional
    public void realizar(Long idAgendamento, AtendimentoRequest req) {
        Agendamento a = agendamentos.findById(idAgendamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Agendamento não encontrado."));

        if (!Set.of("Agendado", "Confirmado").contains(a.getStatus())) {
            throw new RegraNegocioException(
                    "Só é possível realizar atendimentos Agendado ou Confirmado.");
        }

        if (sessoes.existsByIdAgendamento(idAgendamento)) {
            throw new RegraNegocioException(
                    "Este agendamento já possui uma sessão registrada.");
        }

        String statusSala = agendamentos.statusSala(a.getIdSala())
                .orElseThrow(() -> new RegraNegocioException(
                        "Sala não encontrada."));

        if (!"Ativa".equals(statusSala)) {
            throw new RegraNegocioException(
                    "Sala não está ativa. Não é possível realizar atendimento nela.");
        }

        var procedimento = procedimentos.findById(a.getIdProcedimento())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Procedimento não encontrado."));

        boolean injetavel = Boolean.TRUE.equals(
                procedimento.getInjetavel());

        regraDados.validar(req, injetavel);

        // Serializa a criação do prontuário para o mesmo paciente.
        prontuarios.bloquearPaciente(a.getIdPaciente())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente não encontrado."));

        LocalDate hoje = LocalDate.now();

        Prontuario prontuario = prontuarios
                .buscarParaAtualizacao(a.getIdPaciente())
                .orElseGet(() -> {
                    Prontuario novo = new Prontuario();
                    novo.setIdPaciente(a.getIdPaciente());
                    novo.setDataCriacao(hoje);
                    return prontuarios.saveAndFlush(novo);
                });

        SessaoAtendimento sessao = new SessaoAtendimento();
        sessao.setIdAgendamento(a.getId());
        sessao.setIdProntuario(prontuario.getId());
        sessao.setData(hoje);
        sessao.setIdProcedimento(a.getIdProcedimento());
        sessao.setIdProfissional(a.getIdProfissional());
        sessao.setIdEquipamento(a.getIdEquipamento());
        sessao.setDescricaoEvolucao(req.descricaoEvolucao().trim());

        if (injetavel) {
            sessao.setNumeroLote(req.numeroLote().trim());
            sessao.setFabricante(req.fabricante().trim());
            sessao.setValidadeLote(req.validadeLote());
        }

        sessoes.saveAndFlush(sessao);

        a.setTipoCancelamento(null);
        a.setStatus("Realizado");
        agendamentos.saveAndFlush(a);
    }
}