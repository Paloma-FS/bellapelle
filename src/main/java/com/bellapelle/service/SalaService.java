package com.bellapelle.service;

import com.bellapelle.dto.SalaRequest;
import com.bellapelle.dto.SalaResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.Sala;
import com.bellapelle.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class SalaService {

    private static final Set<String> STATUS_VALIDOS =
            Set.of("Ativa", "Em manutenção", "Inativa");

    private final SalaRepository repository;

    public SalaService(SalaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SalaResponse> listar(String busca, String status) {
        String b = vazio(busca) ? null : busca.trim();
        String s = vazio(status) ? null : status.trim();
        return repository.pesquisar(b, s).stream().map(SalaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public SalaResponse buscar(Long id) {
        return SalaResponse.de(obter(id));
    }

    @Transactional
    public SalaResponse criar(SalaRequest req) {
        Sala sala = new Sala();
        preencher(sala, req);
        return SalaResponse.de(repository.save(sala));
    }

    @Transactional
    public SalaResponse atualizar(Long id, SalaRequest req) {
        Sala sala = obter(id);
        preencher(sala, req);
        return SalaResponse.de(repository.save(sala));
    }

    @Transactional
    public SalaResponse alterarStatus(Long id, String status) {
        Sala sala = obter(id);
        sala.setStatus(validarStatus(status));
        return SalaResponse.de(repository.save(sala));
    }

    private void preencher(Sala sala, SalaRequest req) {
        sala.setNome(req.nome().trim());
        sala.setDescricao(vazio(req.descricao()) ? null : req.descricao().trim());

        String status = vazio(req.status()) ? sala.getStatus() : req.status().trim();
        sala.setStatus(validarStatus(status));
    }

    private Sala obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sala não encontrada."));
    }

    private String validarStatus(String status) {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new RegraNegocioException(
                    "Status de sala inválido. Use Ativa, Em manutenção ou Inativa."
            );
        }
        return status;
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}