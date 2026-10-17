package com.bellapelle.service.regra;

import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.repository.ProfissionalProcedimentoRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class RegraProcedimentosProfissional {

    private final ProfissionalProcedimentoRepository repository;

    public RegraProcedimentosProfissional(
            ProfissionalProcedimentoRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista vazia é permitida: significa profissional sem habilitações.
     * Retorna uma cópia validada, sem aceitar identificadores repetidos.
     */
    public List<Long> validar(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        Set<Long> unicos = new LinkedHashSet<>();

        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new RegraNegocioException(
                        "Existe um procedimento inválido na seleção."
                );
            }

            if (!unicos.add(id)) {
                throw new RegraNegocioException(
                        "Um mesmo procedimento não pode ser informado mais de uma vez."
                );
            }
        }

        List<Long> selecionados = List.copyOf(unicos);

        Set<Long> existentes = new LinkedHashSet<>(
                repository.listarIdsExistentes(selecionados)
        );

        if (!existentes.containsAll(unicos)) {
            throw new RegraNegocioException(
                    "Um ou mais procedimentos selecionados não existem. "
                            + "Reabra o cadastro e confira a seleção."
            );
        }

        return selecionados;
    }
}