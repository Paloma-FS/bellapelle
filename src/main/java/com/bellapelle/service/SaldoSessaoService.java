package com.bellapelle.service;

import com.bellapelle.dto.SaldoSessaoResponse;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.SaldoSessao;
import com.bellapelle.repository.SaldoSessaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SaldoSessaoService {

    private final SaldoSessaoRepository repository;

    public SaldoSessaoService(SaldoSessaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SaldoSessaoResponse> listarPorPaciente(Integer idPaciente) {
        return repository.listarPorPaciente(idPaciente).stream()
                .map(SaldoSessaoResponse::de).toList();
    }

    @Transactional
    public void debitar(Integer idPaciente, Long idProcedimento, LocalDate data) {
        List<SaldoSessao> saldos = repository.buscarComSaldoDisponivel(idPaciente, idProcedimento);
        if (saldos.isEmpty()) {
            throw new RegraNegocioException(
                    "A paciente não possui sessões disponíveis deste procedimento. Registre uma venda antes do atendimento.");
        }
        SaldoSessao saldo = saldos.get(0);
        saldo.setQuantidadeUtilizada(saldo.getQuantidadeUtilizada() + 1);
        saldo.setDataUltimaSessao(data);
    }
}