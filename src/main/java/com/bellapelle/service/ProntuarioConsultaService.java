package com.bellapelle.service;

import com.bellapelle.dto.ProntuarioConsultaResponse;
import com.bellapelle.dto.ProntuarioConsultaResponse.AnamneseProntuario;
import com.bellapelle.dto.ProntuarioConsultaResponse.SessaoProntuario;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.repository.ProntuarioConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.bellapelle.repository.ProntuarioConsultaRepository.paraBooleano;
import static com.bellapelle.repository.ProntuarioConsultaRepository.paraData;
import static com.bellapelle.repository.ProntuarioConsultaRepository.paraTexto;

@Service
public class ProntuarioConsultaService {

    private final ProntuarioConsultaRepository repository;

    public ProntuarioConsultaService(ProntuarioConsultaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ProntuarioConsultaResponse consultar(Integer idPaciente) {
        if (!repository.existePaciente(idPaciente)) {
            throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        }

        LocalDate dataCriacao = repository.buscarDataCriacao(idPaciente).orElse(null);

        AnamneseProntuario anamnese = repository.buscarAnamnese(idPaciente)
                .map(ProntuarioConsultaService::converterAnamnese)
                .orElse(null);

        List<SessaoProntuario> sessoes = repository.listarSessoes(idPaciente)
                .stream()
                .map(ProntuarioConsultaService::converterSessao)
                .toList();

        return new ProntuarioConsultaResponse(dataCriacao, anamnese, sessoes);
    }

    private static AnamneseProntuario converterAnamnese(Object[] linha) {
        return new AnamneseProntuario(
                paraData(linha[0]),
                paraTexto(linha[1]),
                paraTexto(linha[2]),
                paraTexto(linha[3]),
                paraBooleano(linha[4]),
                paraBooleano(linha[5]),
                paraBooleano(linha[6]),
                paraTexto(linha[7]),
                paraTexto(linha[8])
        );
    }

    private static SessaoProntuario converterSessao(Object[] linha) {
        return new SessaoProntuario(
                paraData(linha[0]),
                paraTexto(linha[1]),
                paraTexto(linha[2]),
                paraTexto(linha[3]),
                paraTexto(linha[4]),
                paraTexto(linha[5]),
                paraTexto(linha[6]),
                paraData(linha[7]),
                paraTexto(linha[8]),
                paraTexto(linha[9])
        );
    }
}