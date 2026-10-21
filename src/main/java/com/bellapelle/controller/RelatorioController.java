package com.bellapelle.controller;

import com.bellapelle.dto.RelatorioComissaoResponse;
import com.bellapelle.dto.RelatorioFaltaResponse;
import com.bellapelle.dto.RelatorioVendaResponse;
import com.bellapelle.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/comissoes")
    public List<RelatorioComissaoResponse> comissoes(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim
    ) {
        return relatorioService.listarComissoes(inicio, fim);
    }

    @GetMapping("/faltas")
    public List<RelatorioFaltaResponse> faltas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim
    ) {
        return relatorioService.listarFaltas(inicio, fim);
    }

    @GetMapping("/vendas")
    public List<RelatorioVendaResponse> vendas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim,

            @RequestParam(required = false, defaultValue = "TODOS")
            String tipo
    ) {
        return relatorioService.listarVendas(inicio, fim, tipo);
    }
}