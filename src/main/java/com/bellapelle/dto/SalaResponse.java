package com.bellapelle.dto;

import com.bellapelle.model.Sala;

public record SalaResponse(Long id, String nome, String descricao, String status) {

    public static SalaResponse de(Sala sala) {
        return new SalaResponse(
                sala.getId(),
                sala.getNome(),
                sala.getDescricao(),
                sala.getStatus()
        );
    }
}