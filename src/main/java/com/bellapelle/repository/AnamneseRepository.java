package com.bellapelle.repository;

import com.bellapelle.dto.AnamneseRequest;
import com.bellapelle.dto.AnamneseResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AnamneseRepository {

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

    public Optional<Integer> buscarIdPorPaciente(Integer idPaciente) {
        List<?> resultados = entityManager.createNativeQuery("""
                SELECT id_anamnese
                FROM anamnese
                WHERE id_paciente = :idPaciente
                """)
                .setParameter("idPaciente", idPaciente)
                .getResultList();
        if (resultados.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(((Number) resultados.get(0)).intValue());
    }

    public Optional<AnamneseResponse> buscarPorPaciente(Integer idPaciente) {
        List<?> resultados = entityManager.createNativeQuery("""
                SELECT data_preenchimento,
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
        Object[] l = (Object[]) resultados.get(0);
        return Optional.of(new AnamneseResponse(
                paraData(l[0]),
                (String) l[1],
                (String) l[2],
                (String) l[3],
                paraBoolean(l[4]),
                paraBoolean(l[5]),
                paraBoolean(l[6]),
                (String) l[7],
                (String) l[8]
        ));
    }

    public void inserir(Integer idPaciente, AnamneseRequest req, LocalDate dataPreenchimento) {
        entityManager.createNativeQuery("""
                INSERT INTO anamnese
                    (id_paciente, data_preenchimento, alergias, medicacoes_continuas, cirurgias_previas,
                     gestante_ou_lactante, uso_isotretinoina, historico_queloide, doencas_de_pele, contraindicacoes_identificadas)
                VALUES
                    (:idPaciente, :data, :alergias, :medicacoes, :cirurgias,
                     :gestante, :isotretinoina, :queloide, :doencas, :contraindicacoes)
                """)
                .setParameter("idPaciente", idPaciente)
                .setParameter("data", dataPreenchimento)
                .setParameter("alergias", req.alergias())
                .setParameter("medicacoes", req.medicacoesContinuas())
                .setParameter("cirurgias", req.cirurgiasPrevias())
                .setParameter("gestante", req.gestanteOuLactante())
                .setParameter("isotretinoina", req.usoIsotretinoina())
                .setParameter("queloide", req.historicoQueloide())
                .setParameter("doencas", req.doencasDePele())
                .setParameter("contraindicacoes", req.contraindicacoesIdentificadas())
                .executeUpdate();
    }

    public void atualizar(Integer idPaciente, AnamneseRequest req, LocalDate dataPreenchimento) {
        entityManager.createNativeQuery("""
                UPDATE anamnese
                SET data_preenchimento = :data,
                    alergias = :alergias,
                    medicacoes_continuas = :medicacoes,
                    cirurgias_previas = :cirurgias,
                    gestante_ou_lactante = :gestante,
                    uso_isotretinoina = :isotretinoina,
                    historico_queloide = :queloide,
                    doencas_de_pele = :doencas,
                    contraindicacoes_identificadas = :contraindicacoes
                WHERE id_paciente = :idPaciente
                """)
                .setParameter("idPaciente", idPaciente)
                .setParameter("data", dataPreenchimento)
                .setParameter("alergias", req.alergias())
                .setParameter("medicacoes", req.medicacoesContinuas())
                .setParameter("cirurgias", req.cirurgiasPrevias())
                .setParameter("gestante", req.gestanteOuLactante())
                .setParameter("isotretinoina", req.usoIsotretinoina())
                .setParameter("queloide", req.historicoQueloide())
                .setParameter("doencas", req.doencasDePele())
                .setParameter("contraindicacoes", req.contraindicacoesIdentificadas())
                .executeUpdate();
    }

    private static LocalDate paraData(Object valor) {
        if (valor == null) return null;
        if (valor instanceof LocalDate d) return d;
        if (valor instanceof Date d) return d.toLocalDate();
        if (valor instanceof Timestamp t) return t.toLocalDateTime().toLocalDate();
        return LocalDate.parse(valor.toString());
    }

    private static Boolean paraBoolean(Object valor) {
        if (valor == null) return null;
        if (valor instanceof Boolean b) return b;
        if (valor instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(valor.toString());
    }
}