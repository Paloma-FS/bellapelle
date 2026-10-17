package com.bellapelle.service;

import com.bellapelle.dto.EquipamentoRequest;
import com.bellapelle.dto.EquipamentoResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.Equipamento;
import com.bellapelle.repository.EquipamentoRepository;
import com.bellapelle.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class EquipamentoService {

    private static final Set<String> STATUS_VALIDOS =
            Set.of("Disponível", "Em manutenção", "Inativo");

    private final EquipamentoRepository equipamentoRepository;
    private final SalaRepository salaRepository;

    public EquipamentoService(
            EquipamentoRepository equipamentoRepository,
            SalaRepository salaRepository
    ) {
        this.equipamentoRepository = equipamentoRepository;
        this.salaRepository = salaRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listar(String busca, String status) {
        String b = vazio(busca) ? null : busca.trim();
        String s = vazio(status) ? null : status.trim();
        return equipamentoRepository.pesquisar(b, s)
                .stream()
                .map(EquipamentoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipamentoResponse buscar(Long id) {
        return EquipamentoResponse.de(obter(id));
    }

    @Transactional
    public EquipamentoResponse criar(EquipamentoRequest req) {
        Equipamento equipamento = new Equipamento();
        preencher(equipamento, req);
        return EquipamentoResponse.de(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoResponse atualizar(Long id, EquipamentoRequest req) {
        Equipamento equipamento = obter(id);
        preencher(equipamento, req);
        return EquipamentoResponse.de(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoResponse alterarStatus(Long id, String status) {
        Equipamento equipamento = obter(id);
        equipamento.setStatus(validarStatus(status));
        return EquipamentoResponse.de(equipamentoRepository.save(equipamento));
    }

    private void preencher(Equipamento equipamento, EquipamentoRequest req) {
        equipamento.setNomeModelo(req.nomeModelo().trim());
        equipamento.setTipo(req.tipo().trim());
        equipamento.setNumeroSerie(vazio(req.numeroSerie()) ? null : req.numeroSerie().trim());

        if (req.idSala() == null) {
            equipamento.setIdSala(null);
        } else {
            if (!salaRepository.existsById(req.idSala())) {
                throw new RegraNegocioException("A sala informada não existe.");
            }
            equipamento.setIdSala(req.idSala());
        }

        String status = vazio(req.status()) ? equipamento.getStatus() : req.status().trim();
        equipamento.setStatus(validarStatus(status));
        equipamento.setUltimaManutencao(req.ultimaManutencao());
        equipamento.setProximaManutencao(req.proximaManutencao());
    }

    private Equipamento obter(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento não encontrado."));
    }

    private String validarStatus(String status) {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new RegraNegocioException(
                    "Status de equipamento inválido. Use Disponível, Em manutenção ou Inativo."
            );
        }
        return status;
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}