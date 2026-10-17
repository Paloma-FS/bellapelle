package com.bellapelle.service;

import com.bellapelle.dto.AgendamentoRequest;
import com.bellapelle.dto.AgendamentoResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.Agendamento;
import com.bellapelle.repository.AgendamentoRepository;
import com.bellapelle.service.regra.RegraAgendamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AgendamentoService {

    private static final Set<String> STATUS = Set.of("Agendado", "Confirmado", "Realizado", "Cancelado", "Faltou");
    private static final Set<String> CANCELAMENTO = Set.of("Sem custo", "Com débito", "Falta justificada");

    /** Transições permitidas: status atual -> próximos status. Estados finais não têm saída. */
    private static final Map<String, Set<String>> TRANSICOES = Map.of(
            "Agendado", Set.of("Confirmado", "Realizado", "Cancelado", "Faltou"),
            "Confirmado", Set.of("Realizado", "Cancelado", "Faltou"),
            "Realizado", Set.of(),
            "Cancelado", Set.of(),
            "Faltou", Set.of());

    private final AgendamentoRepository repository;
    private final List<RegraAgendamento> regras;

    public AgendamentoService(AgendamentoRepository repository, List<RegraAgendamento> regras) {
        this.repository = repository;
        this.regras = regras;
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listar(LocalDate data, String status) {
        LocalDateTime ini = data == null ? null : data.atStartOfDay();
        LocalDateTime fim = data == null ? null : data.plusDays(1).atStartOfDay();
        return repository.listar(ini, fim, status == null || status.isBlank() ? null : status)
                .stream().map(AgendamentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse buscar(Long id) {
        return repository.buscarView(id).map(AgendamentoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado."));
    }

    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest req) {
        Agendamento a = new Agendamento();
        aplicar(a, req, 0L);
        return buscar(repository.save(a).getId());
    }

    @Transactional
    public AgendamentoResponse atualizar(Long id, AgendamentoRequest req) {
        Agendamento a = obter(id);
        if (!Set.of("Agendado", "Confirmado").contains(a.getStatus())) {
            throw new RegraNegocioException("Só é possível alterar agendamentos Agendado ou Confirmado.");
        }
        aplicar(a, req, id);
        repository.save(a);
        return buscar(id);
    }

    @Transactional
    public AgendamentoResponse alterarStatus(Long id, String status, String tipoCancelamento) {
        if (!STATUS.contains(status)) throw new RegraNegocioException("Status inválido.");
        if ("Realizado".equals(status)) {
            throw new RegraNegocioException(
                    "Para marcar como realizado, registre o atendimento no prontuário.");
        }
        Agendamento a = obter(id);

        Set<String> permitidos = TRANSICOES.getOrDefault(a.getStatus(), Set.of());
        if (!permitidos.contains(status)) {
            throw new RegraNegocioException(
                    "Não é possível alterar o agendamento de " + a.getStatus() + " para " + status + ".");
        }

        if (Set.of("Agendado", "Confirmado", "Realizado").contains(status)) {
            String statusSala = repository.statusSala(a.getIdSala())
                    .orElseThrow(() -> new RegraNegocioException("Sala não encontrada."));
            if (!"Ativa".equals(statusSala)) {
                throw new RegraNegocioException(
                        "Sala não está ativa. Não é possível agendar, confirmar ou realizar atendimento nela.");
            }
        }
        if ("Cancelado".equals(status)) {
            if (tipoCancelamento == null || !CANCELAMENTO.contains(tipoCancelamento)) {
                throw new RegraNegocioException("Informe o tipo de cancelamento.");
            }
            a.setTipoCancelamento(tipoCancelamento);
        } else {
            a.setTipoCancelamento(null);
        }
        a.setStatus(status);
        repository.save(a);
        return buscar(id);
    }

    public record HorarioLivre(String hora, boolean livre, String motivo) {}

    @Transactional(readOnly = true)
    public List<HorarioLivre> horariosLivres(LocalDate data, Long idPaciente, Long idProcedimento,
                    Long idProfissional, Long idSala, Long idEquipamento,
                    Integer duracaoMinutos, Long ignorarId) {
        var proc = repository.infoProcedimento(idProcedimento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado."));
        if (!"Ativo".equals(proc.getStatus())) {
            throw new RegraNegocioException("Procedimento inativo.");
        }
        int duracao = duracaoMinutos != null ? duracaoMinutos : proc.getDuracao();
        validarDuracao(duracao);
        long ignorar = ignorarId == null ? 0L : ignorarId;
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = data.atTime(HorarioClinica.FECHAMENTO);

        List<HorarioLivre> saida = new ArrayList<>();
        for (LocalDateTime ini = data.atTime(HorarioClinica.ABERTURA);
             !ini.plusMinutes(duracao).isAfter(limite);
             ini = ini.plusMinutes(HorarioClinica.PASSO_MINUTOS)) {

            String hora = ini.toLocalTime().toString();
            if (ignorar == 0L && ini.isBefore(agora)) {
                saida.add(new HorarioLivre(hora, false, "Horário já passou"));
                continue;
            }
            LocalDateTime fim = ini.plusMinutes(duracao);
            var req = new AgendamentoRequest(idPaciente, idProcedimento, idProfissional,
                    idSala, idEquipamento, ini, duracao);
            var ctx = new ContextoAgendamento(req, proc, ini, fim, ignorar);

            String motivo = null;
            for (RegraAgendamento r : regras) {
                try {
                    r.validar(ctx);
                } catch (RegraNegocioException e) {
                    motivo = e.getMessage();
                    break;
                }
            }
            saida.add(new HorarioLivre(hora, motivo == null, motivo));
        }
        return saida;
    }

    private void aplicar(Agendamento a, AgendamentoRequest req, Long ignorarId) {
        if (repository.contarPaciente(req.idPaciente()) == 0) {
            throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        }
        var proc = repository.infoProcedimento(req.idProcedimento())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado."));
        if (!"Ativo".equals(proc.getStatus())) {
            throw new RegraNegocioException("Procedimento inativo.");
        }
        if (req.dataHora().isBefore(LocalDateTime.now()) && ignorarId == 0L) {
            throw new RegraNegocioException("Data e hora devem estar no futuro.");
        }
        int duracao = req.duracaoMinutos() != null ? req.duracaoMinutos() : proc.getDuracao();
        validarDuracao(duracao);
        LocalDateTime fim = req.dataHora().plusMinutes(duracao);

        ContextoAgendamento ctx = new ContextoAgendamento(req, proc, req.dataHora(), fim, ignorarId);
        regras.forEach(r -> r.validar(ctx));

        a.setIdPaciente(req.idPaciente());
        a.setIdProcedimento(req.idProcedimento());
        a.setIdProfissional(req.idProfissional());
        a.setIdSala(req.idSala());
        a.setIdEquipamento(req.idEquipamento());
        a.setDataHora(req.dataHora());
        a.setDuracaoMinutos(duracao);
    }

    private void validarDuracao(int duracao) {
        if (duracao < 1) {
            throw new RegraNegocioException("A duração deve ser de pelo menos 1 minuto.");
        }
    }

    private Agendamento obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado."));
    }
}