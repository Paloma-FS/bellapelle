package com.bellapelle.controller;

import com.bellapelle.dto.EquipamentoRequest;
import com.bellapelle.dto.EquipamentoResponse;
import com.bellapelle.service.EquipamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipamentos")
public class EquipamentoController {

    private final EquipamentoService service;

    public EquipamentoController(EquipamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EquipamentoResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status
    ) {
        return service.listar(busca, status);
    }

    @GetMapping("/{id}")
    public EquipamentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipamentoResponse criar(@Valid @RequestBody EquipamentoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public EquipamentoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EquipamentoRequest req
    ) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    public EquipamentoResponse alterarStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return service.alterarStatus(id, status);
    }
}