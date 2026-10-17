package com.bellapelle.dto;

import com.bellapelle.model.Equipamento;

import java.time.LocalDate;

public record EquipamentoResponse(
        Long id,
        String nomeModelo,
        String tipo,
        String numeroSerie,
        Long idSala,
        String status,
        LocalDate ultimaManutencao,
        LocalDate proximaManutencao
) {
    public static EquipamentoResponse de(Equipamento equipamento) {
        return new EquipamentoResponse(
                equipamento.getId(),
                equipamento.getNomeModelo(),
                equipamento.getTipo(),
                equipamento.getNumeroSerie(),
                equipamento.getIdSala(),
                equipamento.getStatus(),
                equipamento.getUltimaManutencao(),
                equipamento.getProximaManutencao()
        );
    }
}