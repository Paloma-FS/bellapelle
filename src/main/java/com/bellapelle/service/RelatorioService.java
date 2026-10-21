package com.bellapelle.service;

import com.bellapelle.dto.RelatorioComissaoResponse;
import com.bellapelle.dto.RelatorioFaltaResponse;
import com.bellapelle.dto.RelatorioVendaResponse;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.repository.RelatorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RelatorioService {

    private static final Set<String> TIPOS_VENDA =
            Set.of("TODOS", "PROCEDIMENTO", "PACOTE");

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional(readOnly = true)
    public List<RelatorioComissaoResponse> listarComissoes(
            LocalDate inicio,
            LocalDate fim
    ) {
        validarPeriodo(inicio, fim);

        return relatorioRepository.listarComissoes(inicio, fim)
                .stream()
                .map(item -> new RelatorioComissaoResponse(
                        item.getNomeProfissional(),
                        item.getValorComissao(),
                        item.getNomeProcedimento(),
                        item.getData()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RelatorioFaltaResponse> listarFaltas(
            LocalDate inicio,
            LocalDate fim
    ) {
        validarPeriodo(inicio, fim);

        LocalDateTime inicioDoDia = inicio.atStartOfDay();
        LocalDateTime inicioDoDiaSeguinteAoFim = fim.plusDays(1).atStartOfDay();

        return relatorioRepository.listarFaltas(
                        inicioDoDia,
                        inicioDoDiaSeguinteAoFim
                )
                .stream()
                .map(item -> new RelatorioFaltaResponse(
                        item.getNomePaciente(),
                        item.getDataFalta(),
                        item.getFaltasAnteriores()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RelatorioVendaResponse> listarVendas(
            LocalDate inicio,
            LocalDate fim,
            String tipo
    ) {
        validarPeriodo(inicio, fim);

        String tipoNormalizado = tipo == null
                ? "TODOS"
                : tipo.trim().toUpperCase(Locale.ROOT);

        if (!TIPOS_VENDA.contains(tipoNormalizado)) {
            throw new RegraNegocioException(
                    "O filtro de vendas deve ser Todos, Procedimento ou Pacote."
            );
        }

        return relatorioRepository.listarVendas(
                        inicio,
                        fim,
                        tipoNormalizado
                )
                .stream()
                .map(item -> new RelatorioVendaResponse(
                        item.getTipo(),
                        item.getNomeItem(),
                        item.getQuantidadeVendida(),
                        item.getValorTotalVendas(),
                        item.getDataUltimaVenda()
                ))
                .toList();
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException(
                    "Informe a data inicial e a data final do relatório."
            );
        }

        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException(
                    "A data inicial não pode ser posterior à data final."
            );
        }
    }
}