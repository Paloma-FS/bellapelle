package com.bellapelle.controller;

import com.bellapelle.dto.PacoteRequest;
import com.bellapelle.dto.PacoteResponse;
import com.bellapelle.service.PacoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacotes")
public class PacoteController {

    private final PacoteService service;

    public PacoteController(PacoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<PacoteResponse> listar(@RequestParam(required = false) String busca,
                                       @RequestParam(required = false) String status) {
        return service.listar(busca, status);
    }

    @GetMapping("/{id}")
    public PacoteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacoteResponse criar(@Valid @RequestBody PacoteRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public PacoteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacoteRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    public PacoteResponse alterarStatus(@PathVariable Long id, @RequestParam String status) {
        return service.alterarStatus(id, status);
    }
}