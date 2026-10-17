package com.bellapelle.service;

import com.bellapelle.dto.AnamneseRequest;
import com.bellapelle.dto.AnamneseResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.repository.AnamneseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AnamneseService {

    private final AnamneseRepository repository;

    public AnamneseService(AnamneseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AnamneseResponse buscar(Integer idPaciente) {
        return repository.buscarPorPaciente(idPaciente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anamnese ainda não preenchida."));
    }

    @Transactional
    public AnamneseResponse salvar(Integer idPaciente, AnamneseRequest req) {
        if (!repository.existePaciente(idPaciente)) {
            throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        }
        LocalDate hoje = LocalDate.now();
        if (repository.buscarIdPorPaciente(idPaciente).isPresent()) {
            repository.atualizar(idPaciente, req, hoje);
        } else {
            repository.inserir(idPaciente, req, hoje);
        }
        return repository.buscarPorPaciente(idPaciente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Falha ao recarregar a anamnese salva."));
    }
}