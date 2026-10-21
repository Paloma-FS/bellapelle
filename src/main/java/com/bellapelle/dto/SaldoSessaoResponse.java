package com.bellapelle.dto;

import com.bellapelle.model.SaldoSessao;

import java.time.LocalDate;

public record SaldoSessaoResponse(
        Integer id, Integer idPaciente, String pacienteNome,
        Long idProcedimento, String procedimentoNome,
        Integer quantidadeTotal, Integer quantidadeUtilizada,
        Integer quantidadeDisponivel, LocalDate dataUltimaSessao,
        Integer idVendaOrigem, String pacoteNome
) {

    // Construtor antigo (sem pacote) mantido para nao quebrar quem ja usa o DTO.
    public SaldoSessaoResponse(Integer id, Integer idPaciente, String pacienteNome,
                    Long idProcedimento, String procedimentoNome,
                    Integer quantidadeTotal, Integer quantidadeUtilizada,
                    Integer quantidadeDisponivel, LocalDate dataUltimaSessao,
                    Integer idVendaOrigem) {
        this(id, idPaciente, pacienteNome, idProcedimento, procedimentoNome,
                quantidadeTotal, quantidadeUtilizada, quantidadeDisponivel,
                dataUltimaSessao, idVendaOrigem, null);
    }

    public static SaldoSessaoResponse de(SaldoSessao s) {
        var venda = s.getVendaOrigem();
        var pacote = venda.getPacote();

        return new SaldoSessaoResponse(
                s.getId(),
                s.getPaciente().getId(),
                s.getPaciente().getNome(),
                s.getProcedimento().getId(),
                s.getProcedimento().getNome(),
                s.getQuantidadeTotal(),
                s.getQuantidadeUtilizada(),
                s.getQuantidadeDisponivel(),
                s.getDataUltimaSessao(),
                venda.getId(),
                pacote == null ? null : pacote.getNome()
        );
    }
}