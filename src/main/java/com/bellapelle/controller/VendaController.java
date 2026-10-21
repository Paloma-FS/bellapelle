package com.bellapelle.controller;

import com.bellapelle.dto.VendaRequest;
import com.bellapelle.dto.VendaResponse;
import com.bellapelle.service.VendaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @GetMapping
    public List<VendaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public VendaResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaResponse criar(@RequestBody VendaRequest req) {
        return service.criar(req);
    }
}