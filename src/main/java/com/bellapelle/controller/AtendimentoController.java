package com.bellapelle.controller;

import com.bellapelle.dto.AtendimentoContextoResponse;
import com.bellapelle.dto.AtendimentoRequest;
import com.bellapelle.service.AtendimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agendamentos")
public class AtendimentoController {

    private final AtendimentoService service;

    public AtendimentoController(AtendimentoService service) {
        this.service = service;
    }

    @GetMapping("/{id}/atendimento")
    public AtendimentoContextoResponse consultar(@PathVariable Long id) {
        return service.consultar(id);
    }

    @PostMapping("/{id}/atendimento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void realizar(
            @PathVariable Long id,
            @Valid @RequestBody AtendimentoRequest req) {
        service.realizar(id, req);
    }
}