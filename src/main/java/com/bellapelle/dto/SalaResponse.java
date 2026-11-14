package com.bellapelle.dto;

import com.bellapelle.model.Sala;

public record SalaResponse(Long id, Integer numero, String nome, String descricao, String status) {

    public static SalaResponse de(Sala sala) {
        return new SalaResponse(
                sala.getId(),
                sala.getNumero(),
                sala.getNome(),
                sala.getDescricao(),
                sala.getStatus()
        );
    }
}