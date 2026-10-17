package com.bellapelle.controller;

import com.bellapelle.dto.ItemPacoteRequest;
import com.bellapelle.dto.ItemPacoteResponse;
import com.bellapelle.service.ItemPacoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacotes/{idPacote}/itens")
public class ItemPacoteController {

    private final ItemPacoteService service;

    public ItemPacoteController(ItemPacoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemPacoteResponse> listar(@PathVariable Long idPacote) {
        return service.listar(idPacote);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemPacoteResponse adicionar(@PathVariable Long idPacote, @Valid @RequestBody ItemPacoteRequest req) {
        return service.adicionar(idPacote, req);
    }

    @PutMapping("/{idItem}")
    public ItemPacoteResponse atualizar(@PathVariable Long idPacote, @PathVariable Long idItem,
                                        @Valid @RequestBody ItemPacoteRequest req) {
        return service.atualizar(idPacote, idItem, req);
    }

    @DeleteMapping("/{idItem}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long idPacote, @PathVariable Long idItem) {
        service.remover(idPacote, idItem);
    }
}