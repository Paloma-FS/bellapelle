package com.bellapelle.dto;

import com.bellapelle.model.Paciente;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PacienteResponse(
        Integer id, String nome, String telefone, String email, LocalDate dataNascimento,
        String cpf, String endereco, String genero, BigDecimal peso, Integer altura,
        String alergias, String restricoesSaude, String preferenciaBebida,
        String preferenciaProfissional, String contatoEmergenciaNome,
        String contatoEmergenciaTelefone, String status, String canalOrigem, String observacoes
) {
    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome(), p.getTelefone(), p.getEmail(),
                p.getDataNascimento(), p.getCpf(), p.getEndereco(), p.getGenero(), p.getPeso(),
                p.getAltura(), p.getAlergias(), p.getRestricoesSaude(), p.getPreferenciaBebida(),
                p.getPreferenciaProfissional(), p.getContatoEmergenciaNome(),
                p.getContatoEmergenciaTelefone(), p.getStatus(), p.getCanalOrigem(), p.getObservacoes());
    }
}