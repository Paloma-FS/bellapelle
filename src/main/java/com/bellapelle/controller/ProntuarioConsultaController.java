package com.bellapelle.controller;

import com.bellapelle.dto.ProntuarioConsultaResponse;
import com.bellapelle.service.ProntuarioConsultaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
public class ProntuarioConsultaController {

    private final ProntuarioConsultaService service;

    public ProntuarioConsultaController(ProntuarioConsultaService service) {
        this.service = service;
    }

    @GetMapping("/{id}/prontuario")
    public ProntuarioConsultaResponse consultar(@PathVariable Integer id) {
        return service.consultar(id);
    }
}