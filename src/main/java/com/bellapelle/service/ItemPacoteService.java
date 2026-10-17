package com.bellapelle.service;

import com.bellapelle.dto.ItemPacoteRequest;
import com.bellapelle.dto.ItemPacoteResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.ItemPacote;
import com.bellapelle.model.Pacote;
import com.bellapelle.model.Procedimento;
import com.bellapelle.repository.ItemPacoteRepository;
import com.bellapelle.repository.PacoteRepository;
import com.bellapelle.repository.ProcedimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemPacoteService {

    private final ItemPacoteRepository repository;
    private final PacoteRepository pacoteRepository;
    private final ProcedimentoRepository procedimentoRepository;

    public ItemPacoteService(ItemPacoteRepository repository, PacoteRepository pacoteRepository,
                    ProcedimentoRepository procedimentoRepository) {
        this.repository = repository;
        this.pacoteRepository = pacoteRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemPacoteResponse> listar(Long idPacote) {
        return repository.listarPorPacote(idPacote).stream().map(ItemPacoteResponse::de).toList();
    }

    @Transactional
    public ItemPacoteResponse adicionar(Long idPacote, ItemPacoteRequest req) {
        Pacote pacote = pacoteRepository.findById(idPacote)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pacote não encontrado."));
        Procedimento proc = procedimentoRepository.findById(req.idProcedimento())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado."));
        if (!"Ativo".equals(proc.getStatus())) {
            throw new RegraNegocioException("Procedimento inativo não pode ser adicionado ao pacote.");
        }
        if (repository.existsByPacoteIdAndProcedimentoId(idPacote, proc.getId())) {
            throw new RegraNegocioException("Este procedimento já está no pacote.");
        }
        ItemPacote item = new ItemPacote();
        item.setPacote(pacote);
        item.setProcedimento(proc);
        item.setNumeroSessoes(req.numeroSessoes());
        item.setPeriodicidadeDias(req.periodicidadeDias());
        return ItemPacoteResponse.de(repository.save(item));
    }

    @Transactional
    public ItemPacoteResponse atualizar(Long idPacote, Long idItem, ItemPacoteRequest req) {
        ItemPacote item = obter(idPacote, idItem);
        if (!item.getProcedimento().getId().equals(req.idProcedimento())) {
            throw new RegraNegocioException("Para trocar o procedimento, remova o item e adicione outro.");
        }
        item.setNumeroSessoes(req.numeroSessoes());
        item.setPeriodicidadeDias(req.periodicidadeDias());
        return ItemPacoteResponse.de(repository.save(item));
    }

    @Transactional
    public void remover(Long idPacote, Long idItem) {
        ItemPacote item = obter(idPacote, idItem);
        if (repository.countByPacoteId(idPacote) <= 1) {
            throw new RegraNegocioException("O pacote deve manter ao menos um procedimento.");
        }
        repository.delete(item);
    }

    private ItemPacote obter(Long idPacote, Long idItem) {
        ItemPacote item = repository.findById(idItem)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado."));
        if (!item.getPacote().getId().equals(idPacote)) {
            throw new RecursoNaoEncontradoException("Item não pertence a este pacote.");
        }
        return item;
    }
}