package com.bellapelle.controller;

import com.bellapelle.dto.ProcedimentoRequest;
import com.bellapelle.dto.ProcedimentoResponse;
import com.bellapelle.service.ProcedimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procedimentos")
public class ProcedimentoController {

    private final ProcedimentoService service;

    public ProcedimentoController(ProcedimentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProcedimentoResponse> listar(@RequestParam(required = false) String busca,
                                             @RequestParam(required = false) String status) {
        return service.listar(busca, status);
    }

    @GetMapping("/{id}")
    public ProcedimentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcedimentoResponse criar(@Valid @RequestBody ProcedimentoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public ProcedimentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProcedimentoRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    public ProcedimentoResponse alterarStatus(@PathVariable Long id, @RequestParam String status) {
        return service.alterarStatus(id, status);
    }
}