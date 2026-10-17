package com.bellapelle.service;

import com.bellapelle.dto.ProcedimentoRequest;
import com.bellapelle.dto.ProcedimentoResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.Procedimento;
import com.bellapelle.repository.ProcedimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcedimentoService {

    private final ProcedimentoRepository repository;

    public ProcedimentoService(ProcedimentoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoResponse> listar(String busca, String status) {
        String b = vazio(busca) ? null : busca.trim();
        String s = vazio(status) ? null : status.trim();
        return repository.pesquisar(b, s).stream().map(ProcedimentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoResponse buscar(Long id) {
        return ProcedimentoResponse.de(obter(id));
    }

    @Transactional
    public ProcedimentoResponse criar(ProcedimentoRequest req) {
        Procedimento p = new Procedimento();
        preencher(p, req);
        p.setStatus("Ativo");
        return ProcedimentoResponse.de(repository.save(p));
    }

    @Transactional
    public ProcedimentoResponse atualizar(Long id, ProcedimentoRequest req) {
        Procedimento p = obter(id);
        preencher(p, req);
        return ProcedimentoResponse.de(repository.save(p));
    }

    @Transactional
    public ProcedimentoResponse alterarStatus(Long id, String status) {
        if (!"Ativo".equals(status) && !"Inativo".equals(status)) {
            throw new RegraNegocioException("Status inválido. Use Ativo ou Inativo.");
        }
        Procedimento p = obter(id);
        p.setStatus(status);
        return ProcedimentoResponse.de(repository.save(p));
    }

    private Procedimento obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado."));
    }

    private void preencher(Procedimento p, ProcedimentoRequest r) {
        boolean requer = Boolean.TRUE.equals(r.requerEquipamento());
        if (requer && vazio(r.tipoEquipamentoRequerido())) {
            throw new RegraNegocioException("Informe o tipo de equipamento requerido.");
        }
        p.setNome(r.nome().trim());
        p.setDescricao(vazio(r.descricao()) ? null : r.descricao().trim());
        p.setCategoria(vazio(r.categoria()) ? null : r.categoria().trim());
        p.setValor(r.valor());
        p.setPercentualComissao(r.percentualComissao());
        p.setDuracaoMinutos(r.duracaoMinutos());
        p.setRequerEquipamento(requer);
        p.setTipoEquipamentoRequerido(requer ? r.tipoEquipamentoRequerido().trim() : null);
        p.setContraindicacoes(vazio(r.contraindicacoes()) ? null : r.contraindicacoes().trim());
        p.setInjetavel(Boolean.TRUE.equals(r.injetavel()));
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}