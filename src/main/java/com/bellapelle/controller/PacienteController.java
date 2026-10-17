package com.bellapelle.controller;

import com.bellapelle.dto.PacienteRequest;
import com.bellapelle.dto.PacienteResponse;
import com.bellapelle.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<PacienteResponse> listar(@RequestParam(required = false) String busca,
                                         @RequestParam(required = false) String status) {
        return service.listar(busca, status);
    }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponse criar(@Valid @RequestBody PacienteRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable Integer id, @Valid @RequestBody PacienteRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/desabilitar")
    public PacienteResponse desabilitar(@PathVariable Integer id) {
        return service.desabilitar(id);
    }

    @PatchMapping("/{id}/reativar")
    public PacienteResponse reativar(@PathVariable Integer id) {
        return service.reativar(id);
    }
}