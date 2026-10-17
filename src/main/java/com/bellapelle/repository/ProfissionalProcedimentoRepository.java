package com.bellapelle.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProfissionalProcedimentoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Long> listarIds(Integer idProfissional) {
        List<?> resultados = entityManager.createNativeQuery("""
                SELECT id_procedimento
                FROM profissional_procedimento
                WHERE id_profissional = :idProfissional
                ORDER BY id_procedimento
                """)
                .setParameter("idProfissional", idProfissional)
                .getResultList();

        return resultados.stream()
                .map(valor -> ((Number) valor).longValue())
                .toList();
    }

    public List<Long> listarIdsExistentes(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<?> resultados = entityManager.createNativeQuery("""
                SELECT id_procedimento
                FROM procedimento
                WHERE id_procedimento IN (:ids)
                """)
                .setParameter("ids", ids)
                .getResultList();

        return resultados.stream()
                .map(valor -> ((Number) valor).longValue())
                .toList();
    }

    /**
     * Serializa as alterações de um mesmo profissional.
     * Deve ser chamado dentro de uma transação de escrita.
     */
    public boolean bloquearProfissional(Integer idProfissional) {
        List<?> resultados = entityManager.createNativeQuery("""
                SELECT id_profissional
                FROM profissional
                WHERE id_profissional = :idProfissional
                FOR UPDATE
                """)
                .setParameter("idProfissional", idProfissional)
                .getResultList();

        return !resultados.isEmpty();
    }

    /**
     * Substitui as habilitações.
     * A transação é controlada pelo service.
     */
    public void substituir(Integer idProfissional, List<Long> ids) {
        entityManager.createNativeQuery("""
                DELETE FROM profissional_procedimento
                WHERE id_profissional = :idProfissional
                """)
                .setParameter("idProfissional", idProfissional)
                .executeUpdate();

        for (Long idProcedimento : ids) {
            entityManager.createNativeQuery("""
                    INSERT INTO profissional_procedimento (
                        id_profissional,
                        id_procedimento
                    )
                    VALUES (
                        :idProfissional,
                        :idProcedimento
                    )
                    """)
                    .setParameter("idProfissional", idProfissional)
                    .setParameter("idProcedimento", idProcedimento)
                    .executeUpdate();
        }
    }
}