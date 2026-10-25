package com.bellapelle.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PainelRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public long contarAtendimentosHoje(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        Number total = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM agendamento a
                WHERE a.data_hora >= :inicio
                  AND a.data_hora < :fim
                  AND a.status IN ('Agendado', 'Confirmado', 'Realizado')
                """)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getSingleResult();

        return total.longValue();
    }

    public long contarFaltasNoMes(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        Number total = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM agendamento a
                WHERE a.data_hora >= :inicio
                  AND a.data_hora < :fim
                  AND a.status = 'Faltou'
                """)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getSingleResult();

        return total.longValue();
    }

    public long contarParcelasVencidas(LocalDate hoje) {
        Number total = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM parcela p
                WHERE p.status = 'Pendente'
                  AND p.data_vencimento < :hoje
                """)
                .setParameter("hoje", hoje)
                .getSingleResult();

        return total.longValue();
    }

    public long contarPacientesComSaldoBaixo() {
        Number total = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(DISTINCT s.id_paciente)
                FROM saldo_sessao s
                WHERE s.quantidade_disponivel BETWEEN 1 AND 2
                """)
                .getSingleResult();

        return total.longValue();
    }

    public List<Object[]> listarAgenda(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        return entityManager.createNativeQuery("""
                SELECT
                    a.id_agendamento,
                    p.nome,
                    pr.nome,
                    pf.nome,
                    sa.nome,
                    e.nome_modelo,
                    a.data_hora,
                    a.duracao_minutos,
                    a.status
                FROM agendamento a
                JOIN paciente p
                    ON p.id_paciente = a.id_paciente
                JOIN procedimento pr
                    ON pr.id_procedimento = a.id_procedimento
                JOIN profissional pf
                    ON pf.id_profissional = a.id_profissional
                JOIN sala sa
                    ON sa.id_sala = a.id_sala
                LEFT JOIN equipamento e
                    ON e.id_equipamento = a.id_equipamento
                WHERE a.data_hora < :fim
                  AND DATEADD('MINUTE', a.duracao_minutos, a.data_hora) > :inicio
                  AND a.status IN ('Agendado', 'Confirmado', 'Realizado')
                ORDER BY a.data_hora, a.id_agendamento
                """)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList();
    }

    public List<Object[]> listarAlertasAnamnese() {
        Query query = entityManager.createNativeQuery("""
                SELECT
                    p.nome,
                    an.contraindicacoes_identificadas,
                    an.uso_isotretinoina,
                    an.gestante_ou_lactante,
                    an.historico_queloide,
                    an.doencas_de_pele
                FROM anamnese an
                JOIN paciente p
                    ON p.id_paciente = an.id_paciente
                WHERE COALESCE(an.contraindicacoes_identificadas, '') <> ''
                   OR an.uso_isotretinoina = 1
                   OR an.gestante_ou_lactante = 1
                   OR an.historico_queloide = 1
                   OR COALESCE(an.doencas_de_pele, '') <> ''
                ORDER BY p.nome
                """);

        query.setMaxResults(5);
        return query.getResultList();
    }

    public List<Object[]> listarParcelasVencidas(LocalDate hoje) {
        Query query = entityManager.createNativeQuery("""
                SELECT
                    p.nome,
                    pa.numero_parcela,
                    pa.data_vencimento,
                    pa.valor_parcela
                FROM parcela pa
                JOIN venda v
                    ON v.id_venda = pa.id_venda
                JOIN paciente p
                    ON p.id_paciente = v.id_paciente
                WHERE pa.status = 'Pendente'
                  AND pa.data_vencimento < :hoje
                ORDER BY pa.data_vencimento, p.nome
                """);

        query.setParameter("hoje", hoje);
        query.setMaxResults(5);
        return query.getResultList();
    }

    public List<Object[]> listarSaldosSemSessaoNoMes(LocalDate inicioMes) {
        Query query = entityManager.createNativeQuery("""
                SELECT
                    p.nome,
                    pr.nome,
                    s.quantidade_disponivel,
                    s.data_ultima_sessao
                FROM saldo_sessao s
                JOIN paciente p
                    ON p.id_paciente = s.id_paciente
                JOIN procedimento pr
                    ON pr.id_procedimento = s.id_procedimento
                WHERE s.quantidade_disponivel > 0
                  AND (
                      s.data_ultima_sessao IS NULL
                      OR s.data_ultima_sessao < :inicioMes
                  )
                ORDER BY p.nome, pr.nome
                """);

        query.setParameter("inicioMes", inicioMes);
        query.setMaxResults(5);
        return query.getResultList();
    }

    public List<Object[]> listarEquipamentosEmManutencao() {
        Query query = entityManager.createNativeQuery("""
                SELECT
                    e.nome_modelo,
                    e.proxima_manutencao
                FROM equipamento e
                WHERE e.status = 'Em manutenção'
                ORDER BY e.nome_modelo
                """);

        query.setMaxResults(5);
        return query.getResultList();
    }
}