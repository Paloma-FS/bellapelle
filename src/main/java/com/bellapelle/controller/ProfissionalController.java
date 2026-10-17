package com.bellapelle.controller;

import com.bellapelle.dto.ProfissionalRequest;
import com.bellapelle.dto.ProfissionalResponse;
import com.bellapelle.service.ProfissionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProfissionalResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status) {

        return service.listar(busca, status);
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/procedimentos")
    public List<Long> listarProcedimentos(@PathVariable Integer id) {
        return service.listarProcedimentos(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfissionalResponse criar(
            @Valid @RequestBody ProfissionalRequest req) {

        return service.criar(req);
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProfissionalRequest req) {

        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/desabilitar")
    public ProfissionalResponse desabilitar(@PathVariable Integer id) {
        return service.desabilitar(id);
    }

    @PatchMapping("/{id}/reativar")
    public ProfissionalResponse reativar(@PathVariable Integer id) {
        return service.reativar(id);
    }
}