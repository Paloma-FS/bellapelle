package com.bellapelle.dto;

import com.bellapelle.model.Profissional;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProfissionalResponse(
        Integer id, String nome, String cpf, LocalDate dataNascimento, String genero,
        String telefone, String email, String numeroConselho, String tipoConselho,
        String ufConselho, String funcao, String tipoVinculo, BigDecimal percentualComissao,
        String diasHorarioTrabalho, String status, LocalDate dataAdmissao, LocalDate dataDesligamento
) {
    public static ProfissionalResponse de(Profissional p) {
        return new ProfissionalResponse(
                p.getId(), p.getNome(), p.getCpf(), p.getDataNascimento(), p.getGenero(),
                p.getTelefone(), p.getEmail(), p.getNumeroConselho(), p.getTipoConselho(),
                p.getUfConselho(), p.getFuncao(), p.getTipoVinculo(), p.getPercentualComissao(),
                p.getDiasHorarioTrabalho(), p.getStatus(), p.getDataAdmissao(), p.getDataDesligamento());
    }
}