package com.bellapelle.controller;

import com.bellapelle.dto.SalaRequest;
import com.bellapelle.dto.SalaResponse;
import com.bellapelle.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final SalaService service;

    public SalaController(SalaService service) {
        this.service = service;
    }

    @GetMapping
    public List<SalaResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status
    ) {
        return service.listar(busca, status);
    }

    @GetMapping("/{id}")
    public SalaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaResponse criar(@Valid @RequestBody SalaRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public SalaResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SalaRequest req
    ) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    public SalaResponse alterarStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return service.alterarStatus(id, status);
    }
}