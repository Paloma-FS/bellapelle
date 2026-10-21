package com.bellapelle.controller;

import com.bellapelle.dto.VendaResponse;
import com.bellapelle.service.VendaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parcelas")
public class ParcelaController {

    private final VendaService service;

    public ParcelaController(VendaService service) {
        this.service = service;
    }

    @PatchMapping("/{id}/pagar")
    public VendaResponse pagar(@PathVariable Integer id) {
        return service.pagarParcela(id);
    }
}