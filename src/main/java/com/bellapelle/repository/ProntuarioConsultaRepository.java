package com.bellapelle.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ProntuarioConsultaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public boolean existePaciente(Integer idPaciente) {
        Number total = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM paciente
                WHERE id_paciente = :idPaciente
                """)
                .setParameter("idPaciente", idPaciente)
                .getSingleResult();

        return total.longValue() > 0;
    }

    public Optional<LocalDate> buscarDataCriacao(Integer idPaciente) {
        List<?> resultados = entityManager.createNativeQuery("""
                SELECT data_criacao
                FROM prontuario
                WHERE id_paciente = :idPaciente
                """)
                .setParameter("idPaciente", idPaciente)
                .getResultList();

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(paraData(resultados.get(0)));
    }

    public Optional<Object[]> buscarAnamnese(Integer idPaciente) {
        List<?> resultados = entityManager.createNativeQuery("""
                SELECT
                    data_preenchimento,
                    alergias,
                    medicacoes_continuas,
                    cirurgias_previas,
                    gestante_ou_lactante,
                    uso_isotretinoina,
                    historico_queloide,
                    doencas_de_pele,
                    contraindicacoes_identificadas
                FROM anamnese
                WHERE id_paciente = :idPaciente
                """)
                .setParameter("idPaciente", idPaciente)
                .getResultList();

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of((Object[]) resultados.get(0));
    }

    public List<Object[]> listarSessoes(Integer idPaciente) {
        List<?> resultados = entityManager.createNativeQuery("""
                SELECT
                    s.data,
                    p.nome,
                    pf.nome,
                    e.nome_modelo,
                    s.regiao_tratada,
                    s.tecnica_utilizada,
                    s.numero_lote,
                    s.validade_lote,
                    s.descricao_evolucao,
                    s.intercorrencias
                FROM prontuario pt
                JOIN sessao_atendimento s
                    ON s.id_prontuario = pt.id_prontuario
                JOIN procedimento p
                    ON p.id_procedimento = s.id_procedimento
                JOIN profissional pf
                    ON pf.id_profissional = s.id_profissional
                LEFT JOIN equipamento e
                    ON e.id_equipamento = s.id_equipamento
                WHERE pt.id_paciente = :idPaciente
                ORDER BY s.data DESC, s.id_sessao DESC
                """)
                .setParameter("idPaciente", idPaciente)
                .getResultList();

        return resultados.stream()
                .map(resultado -> (Object[]) resultado)
                .toList();
    }

    public static LocalDate paraData(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof LocalDate data) {
            return data;
        }
        if (valor instanceof Date data) {
            return data.toLocalDate();
        }

        throw new IllegalStateException(
                "Tipo de data inesperado na consulta do prontuário: "
                        + valor.getClass().getName()
        );
    }

    public static Boolean paraBooleano(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Boolean booleano) {
            return booleano;
        }
        if (valor instanceof Number numero) {
            return numero.intValue() != 0;
        }

        return Boolean.valueOf(valor.toString());
    }

    public static String paraTexto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}