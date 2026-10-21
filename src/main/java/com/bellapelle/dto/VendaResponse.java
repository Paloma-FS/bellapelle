package com.bellapelle.dto;

import com.bellapelle.model.Venda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record VendaResponse(
        Integer id, Integer idPaciente, String pacienteNome,
        Long idPacote, String pacoteNome,
        Long idProcedimento, String procedimentoNome,
        BigDecimal valorTotal, LocalDate dataVenda,
        String formaPagamento, BigDecimal valorEntrada,
        List<ParcelaResponse> parcelas
) {
    public static VendaResponse de(Venda v) {
        return new VendaResponse(
                v.getId(),
                v.getPaciente().getId(),
                v.getPaciente().getNome(),
                v.getPacote() != null ? v.getPacote().getId() : null,
                v.getPacote() != null ? v.getPacote().getNome() : null,
                v.getProcedimento() != null ? v.getProcedimento().getId() : null,
                v.getProcedimento() != null ? v.getProcedimento().getNome() : null,
                v.getValorTotal(),
                v.getDataVenda(),
                v.getFormaPagamento(),
                v.getValorEntrada(),
                v.getParcelas().stream()
                        .sorted(Comparator.comparing(p -> p.getNumeroParcela()))
                        .map(ParcelaResponse::de)
                        .toList()
        );
    }
}