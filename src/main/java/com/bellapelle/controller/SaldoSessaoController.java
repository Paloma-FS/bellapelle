package com.bellapelle.controller;

import com.bellapelle.dto.SaldoSessaoResponse;
import com.bellapelle.service.SaldoSessaoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saldos")
public class SaldoSessaoController {

    private final SaldoSessaoService service;

    public SaldoSessaoController(SaldoSessaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<SaldoSessaoResponse> listar(@RequestParam Integer idPaciente) {
        return service.listarPorPaciente(idPaciente);
    }
}