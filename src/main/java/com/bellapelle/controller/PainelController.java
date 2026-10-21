package com.bellapelle.controller;

import com.bellapelle.dto.PainelResponse;
import com.bellapelle.service.PainelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private final PainelService service;

    public PainelController(PainelService service) {
        this.service = service;
    }

    @GetMapping
    public PainelResponse carregar(
            @RequestParam(required = false) Integer horas
    ) {
        return service.carregar(horas);
    }
}