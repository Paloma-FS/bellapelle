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
import com.bellapelle.repository.SaldoSessaoRepository;
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
    private final SaldoSessaoRepository saldos;
    private final AgendamentoService agendamentoService;
    private final RegraDadosAtendimento regraDados;
    private final SaldoSessaoService saldoSessaoService;

    public AtendimentoService(
            AgendamentoRepository agendamentos,
            ProcedimentoRepository procedimentos,
            ProntuarioRepository prontuarios,
            SessaoAtendimentoRepository sessoes,
            SaldoSessaoRepository saldos,
            AgendamentoService agendamentoService,
            RegraDadosAtendimento regraDados,
            SaldoSessaoService saldoSessaoService) {
        this.agendamentos = agendamentos;
        this.procedimentos = procedimentos;
        this.prontuarios = prontuarios;
        this.sessoes = sessoes;
        this.saldos = saldos;
        this.agendamentoService = agendamentoService;
        this.regraDados = regraDados;
        this.saldoSessaoService = saldoSessaoService;
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

        // Procedimentos pagos pela paciente que ainda têm sessões disponíveis.
        // listarPorPaciente recebe Integer; o id do agendamento vem como Long.
        var disponiveis = saldos.listarPorPaciente(agendamento.idPaciente().intValue())
                .stream()
                .filter(s -> s.getQuantidadeDisponivel() != null
                        && s.getQuantidadeDisponivel() > 0)
                .map(s -> new AtendimentoContextoResponse.ProcedimentoDisponivel(
                        s.getProcedimento().getId(),
                        s.getProcedimento().getNome(),
                        s.getQuantidadeDisponivel(),
                        s.getDataUltimaSessao(),
                        Boolean.TRUE.equals(s.getProcedimento().getInjetavel())))
                .toList();

        return new AtendimentoContextoResponse(
                agendamento,
                Boolean.TRUE.equals(procedimento.getInjetavel()),
                historico,
                disponiveis);
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

        // Procedimento executado: o escolhido na tela ou, se não informado, o agendado.
        Long idProcExecutado = req.idProcedimento() != null
                ? req.idProcedimento()
                : a.getIdProcedimento();

        var procedimento = procedimentos.findById(idProcExecutado)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Procedimento não encontrado."));

        boolean injetavel = Boolean.TRUE.equals(procedimento.getInjetavel());

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
        sessao.setIdProcedimento(idProcExecutado);
        sessao.setIdProfissional(a.getIdProfissional());
        sessao.setIdEquipamento(a.getIdEquipamento());
        sessao.setDescricaoEvolucao(req.descricaoEvolucao().trim());

        if (injetavel) {
            sessao.setNumeroLote(req.numeroLote().trim());
            sessao.setFabricante(req.fabricante().trim());
            sessao.setValidadeLote(req.validadeLote());
        }

        sessoes.saveAndFlush(sessao);

        // Debita uma sessão do saldo da paciente para o procedimento executado.
        // Se não houver saldo, a exceção desfaz também a sessão criada acima.
        saldoSessaoService.debitar(
                a.getIdPaciente().intValue(),
                idProcExecutado,
                hoje);

        // O agendamento passa a refletir o procedimento efetivamente executado.
        a.setIdProcedimento(idProcExecutado);
        a.setTipoCancelamento(null);
        a.setStatus("Realizado");
        agendamentos.saveAndFlush(a);
    }
}