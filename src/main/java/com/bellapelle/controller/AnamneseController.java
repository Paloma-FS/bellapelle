package com.bellapelle.controller;

import com.bellapelle.dto.AnamneseRequest;
import com.bellapelle.dto.AnamneseResponse;
import com.bellapelle.service.AnamneseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
public class AnamneseController {

    private final AnamneseService service;

    public AnamneseController(AnamneseService service) {
        this.service = service;
    }

    @GetMapping("/{idPaciente}/anamnese")
    public AnamneseResponse buscar(@PathVariable Integer idPaciente) {
        return service.buscar(idPaciente);
    }

    @PutMapping("/{idPaciente}/anamnese")
    public AnamneseResponse salvar(@PathVariable Integer idPaciente, @Valid @RequestBody AnamneseRequest req) {
        return service.salvar(idPaciente, req);
    }
}