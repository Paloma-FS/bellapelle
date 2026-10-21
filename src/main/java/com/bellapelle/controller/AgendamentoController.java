package com.bellapelle.controller;

import com.bellapelle.dto.AgendamentoRequest;
import com.bellapelle.dto.AgendamentoResponse;
import com.bellapelle.dto.AtendimentoRequest;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.service.AgendamentoService;
import com.bellapelle.service.AtendimentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;
    private final AtendimentoService atendimentoService;

    public AgendamentoController(AgendamentoService service, AtendimentoService atendimentoService) {
        this.service = service;
        this.atendimentoService = atendimentoService;
    }

    @GetMapping
    public List<AgendamentoResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) String status) {
        return service.listar(data, status);
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse criar(@Valid @RequestBody AgendamentoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public AgendamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AgendamentoRequest req) {
        return service.atualizar(id, req);
    }

    /**
     * Altera o status do agendamento.
     * Para "Realizado" é obrigatório enviar no corpo os dados da sessão
     * (evolução e, se o procedimento for injetável, lote/fabricante/validade),
     * pois o atendimento é registrado no prontuário na mesma transação.
     */
    @PatchMapping("/{id}/status")
    public AgendamentoResponse status(@PathVariable Long id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String tipoCancelamento,
                                      @Valid @RequestBody(required = false) AtendimentoRequest atendimento) {
        if ("Realizado".equals(status)) {
            if (atendimento == null) {
                throw new RegraNegocioException(
                        "Preencha as informações da sessão para marcá-la como realizada.");
            }
            atendimentoService.realizar(id, atendimento);
            return service.buscar(id);
        }

        return service.alterarStatus(id, status, tipoCancelamento);
    }

    @GetMapping("/horarios-livres")
    public List<AgendamentoService.HorarioLivre> horariosLivres(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam Long idPaciente,
            @RequestParam Long idProcedimento,
            @RequestParam Long idProfissional,
            @RequestParam Long idSala,
            @RequestParam(required = false) Long idEquipamento,
            @RequestParam(required = false) Integer duracaoMinutos,
            @RequestParam(required = false) Long ignorarId) {
        return service.horariosLivres(data, idPaciente, idProcedimento, idProfissional,
                idSala, idEquipamento, duracaoMinutos, ignorarId);
    }
}