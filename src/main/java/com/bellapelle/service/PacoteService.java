package com.bellapelle.service;

import com.bellapelle.dto.ItemPacoteRequest;
import com.bellapelle.dto.PacoteRequest;
import com.bellapelle.dto.PacoteResponse;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PacoteService {

    private final PacoteRepository repository;
    private final ItemPacoteRepository itemRepository;
    private final ProcedimentoRepository procedimentoRepository;

    public PacoteService(PacoteRepository repository,
                         ItemPacoteRepository itemRepository,
                         ProcedimentoRepository procedimentoRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<PacoteResponse> listar(String busca, String status) {
        String b = vazio(busca) ? null : busca.trim();
        String s = vazio(status) ? null : status.trim();
        return repository.pesquisar(b, s).stream().map(PacoteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PacoteResponse buscar(Long id) {
        return PacoteResponse.de(obter(id));
    }

    @Transactional
    public PacoteResponse criar(PacoteRequest req) {
        Map<Long, Procedimento> procedimentos = carregarProcedimentos(req.itens());

        Pacote p = new Pacote();
        preencher(p, req);
        p.setStatus("Ativo");
        Pacote salvo = repository.save(p);

        sincronizarItens(salvo, req.itens(), procedimentos, new LinkedHashMap<>());
        return PacoteResponse.de(salvo);
    }

    @Transactional
    public PacoteResponse atualizar(Long id, PacoteRequest req) {
        Pacote p = obter(id);
        Map<Long, Procedimento> procedimentos = carregarProcedimentos(req.itens());

        Map<Long, ItemPacote> existentes = new LinkedHashMap<>();
        for (ItemPacote item : itemRepository.listarPorPacote(id)) {
            existentes.put(item.getProcedimento().getId(), item);
        }

        preencher(p, req);
        Pacote salvo = repository.save(p);

        sincronizarItens(salvo, req.itens(), procedimentos, existentes);
        return PacoteResponse.de(salvo);
    }

    @Transactional
    public PacoteResponse alterarStatus(Long id, String status) {
        if (!"Ativo".equals(status) && !"Inativo".equals(status)) {
            throw new RegraNegocioException("Status inválido. Use Ativo ou Inativo.");
        }
        Pacote p = obter(id);
        p.setStatus(status);
        return PacoteResponse.de(repository.save(p));
    }

    private Pacote obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pacote não encontrado."));
    }

    /**
     * Valida a lista de itens e carrega os procedimentos.
     * Nada é gravado aqui.
     */
    private Map<Long, Procedimento> carregarProcedimentos(List<ItemPacoteRequest> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new RegraNegocioException("O pacote deve ter ao menos um procedimento.");
        }

        Map<Long, Procedimento> procedimentos = new LinkedHashMap<>();
        for (ItemPacoteRequest item : itens) {
            if (procedimentos.containsKey(item.idProcedimento())) {
                throw new RegraNegocioException("Há procedimento repetido na lista do pacote.");
            }
            Procedimento proc = procedimentoRepository.findById(item.idProcedimento())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado."));
            procedimentos.put(proc.getId(), proc);
        }
        return procedimentos;
    }

    /**
     * Deixa os itens do pacote exatamente como a lista recebida.
     * Roda dentro da transação do chamador: qualquer exceção desfaz tudo.
     */
    private void sincronizarItens(Pacote pacote,
                                  List<ItemPacoteRequest> itens,
                                  Map<Long, Procedimento> procedimentos,
                                  Map<Long, ItemPacote> existentes) {
        List<ItemPacote> salvar = new ArrayList<>();

        for (ItemPacoteRequest req : itens) {
            ItemPacote item = existentes.get(req.idProcedimento());

            if (item == null) {
                Procedimento proc = procedimentos.get(req.idProcedimento());
                if (!"Ativo".equals(proc.getStatus())) {
                    throw new RegraNegocioException(
                            "Procedimento inativo não pode ser adicionado ao pacote: " + proc.getNome() + ".");
                }
                item = new ItemPacote();
                item.setPacote(pacote);
                item.setProcedimento(proc);
            }

            item.setNumeroSessoes(req.numeroSessoes());
            item.setPeriodicidadeDias(req.periodicidadeDias());
            salvar.add(item);
        }

        List<ItemPacote> remover = existentes.entrySet().stream()
                .filter(e -> !procedimentos.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        itemRepository.deleteAll(remover);
        itemRepository.saveAll(salvar);
    }

    private void preencher(Pacote p, PacoteRequest r) {
        p.setNome(r.nome().trim());
        p.setDescricao(vazio(r.descricao()) ? null : r.descricao().trim());
        p.setValorTotal(r.valorTotal());
        p.setValidadeDias(r.validadeDias());
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}