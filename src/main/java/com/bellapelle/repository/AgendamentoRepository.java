package com.bellapelle.repository;

import com.bellapelle.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository
        extends JpaRepository<Agendamento, Long> {

    @Override
@org.springframework.data.jpa.repository.Lock(
        jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
Optional<Agendamento> findById(Long id);
    
            String SELECT_VIEW = """
            SELECT
                a.id_agendamento AS id,
                a.id_paciente AS idPaciente,
                p.nome AS nomePaciente,
                a.id_procedimento AS idProcedimento,
                pr.nome AS nomeProcedimento,
                a.id_profissional AS idProfissional,
                pf.nome AS nomeProfissional,
                a.id_sala AS idSala,
                s.nome AS nomeSala,
                a.id_equipamento AS idEquipamento,
                e.nome_modelo AS nomeEquipamento,
                a.data_hora AS dataHora,
                a.duracao_minutos AS duracaoMinutos,
                a.status AS status,
                a.tipo_cancelamento AS tipoCancelamento
            FROM agendamento a
            JOIN paciente p
                ON p.id_paciente = a.id_paciente
            JOIN procedimento pr
                ON pr.id_procedimento = a.id_procedimento
            JOIN profissional pf
                ON pf.id_profissional = a.id_profissional
            JOIN sala s
                ON s.id_sala = a.id_sala
            LEFT JOIN equipamento e
                ON e.id_equipamento = a.id_equipamento
            """;

    interface AgendamentoView {
        Long getId();

        Long getIdPaciente();

        String getNomePaciente();

        Long getIdProcedimento();

        String getNomeProcedimento();

        Long getIdProfissional();

        String getNomeProfissional();

        Long getIdSala();

        String getNomeSala();

        Long getIdEquipamento();

        String getNomeEquipamento();

        LocalDateTime getDataHora();

        Integer getDuracaoMinutos();

        String getStatus();

        String getTipoCancelamento();
    }

    interface ProcedimentoInfo {
        String getStatus();

        Integer getDuracao();

        Integer getRequer();

        String getTipoEquipamento();
    }

    interface EquipamentoInfo {
        String getStatus();

        String getTipo();
    }

    interface PreferenciaGeneroInfo {
        String getPreferencia();

        String getGenero();
    }

    @Query(value = SELECT_VIEW + """
            WHERE (:inicio IS NULL OR a.data_hora >= :inicio)
              AND (:fim IS NULL OR a.data_hora < :fim)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.data_hora, a.id_agendamento
            """, nativeQuery = true)
    List<AgendamentoView> listar(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("status") String status
    );

    @Query(value = SELECT_VIEW + """
            WHERE a.id_agendamento = :id
            """, nativeQuery = true)
    Optional<AgendamentoView> buscarView(@Param("id") Long id);

    @Query(value = """
            SELECT COUNT(*)
            FROM paciente
            WHERE id_paciente = :id
            """, nativeQuery = true)
    long contarPaciente(@Param("id") Long id);

    @Query(value = """
            SELECT
                status AS status,
                duracao_minutos AS duracao,
                CAST(requer_equipamento AS SIGNED) AS requer,
                tipo_equipamento_requerido AS tipoEquipamento
            FROM procedimento
            WHERE id_procedimento = :id
            """, nativeQuery = true)
    Optional<ProcedimentoInfo> infoProcedimento(@Param("id") Long id);

    @Query(value = """
            SELECT COUNT(*)
            FROM profissional
            WHERE id_profissional = :id
              AND status = 'Ativo'
            """, nativeQuery = true)
    long contarProfissionalAtivo(@Param("id") Long id);

    @Query(value = """
            SELECT COUNT(*)
            FROM profissional_procedimento
            WHERE id_profissional = :idProfissional
              AND id_procedimento = :idProcedimento
            """, nativeQuery = true)
    long contarHabilitacao(
            @Param("idProfissional") Long idProfissional,
            @Param("idProcedimento") Long idProcedimento
    );

    @Query(value = """
            SELECT status
            FROM sala
            WHERE id_sala = :id
            """, nativeQuery = true)
    Optional<String> statusSala(@Param("id") Long id);

    @Query(value = """
            SELECT
                status AS status,
                tipo AS tipo
            FROM equipamento
            WHERE id_equipamento = :id
            """, nativeQuery = true)
    Optional<EquipamentoInfo> infoEquipamento(@Param("id") Long id);

    @Query(value = """
            SELECT
                p.preferencia_profissional AS preferencia,
                pf.genero AS genero
            FROM paciente p
            CROSS JOIN profissional pf
            WHERE p.id_paciente = :idPaciente
              AND pf.id_profissional = :idProfissional
            """, nativeQuery = true)
    Optional<PreferenciaGeneroInfo> infoPreferenciaGenero(
            @Param("idPaciente") Long idPaciente,
            @Param("idProfissional") Long idProfissional
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM agendamento a
            WHERE a.id_sala = :idSala
              AND a.status IN (
                  :#{T(com.bellapelle.model.StatusAgendamento).Agendado.name()},
                  :#{T(com.bellapelle.model.StatusAgendamento).Confirmado.name()},
                  :#{T(com.bellapelle.model.StatusAgendamento).Realizado.name()}
              )
              AND (:ignorarId IS NULL OR a.id_agendamento <> :ignorarId)
              AND a.data_hora < :fim
              AND DATEADD('MINUTE', a.duracao_minutos, a.data_hora) > :inicio
            """, nativeQuery = true)
    long conflitosSala(
            @Param("idSala") Long idSala,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("ignorarId") Long ignorarId
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM agendamento a
            WHERE a.id_profissional = :idProfissional
              AND a.status IN (
                  :#{T(com.bellapelle.model.StatusAgendamento).Agendado.name()},
                  :#{T(com.bellapelle.model.StatusAgendamento).Confirmado.name()},
                  :#{T(com.bellapelle.model.StatusAgendamento).Realizado.name()}
              )
              AND (:ignorarId IS NULL OR a.id_agendamento <> :ignorarId)
              AND a.data_hora < :fim
              AND DATEADD('MINUTE', a.duracao_minutos, a.data_hora) > :inicio
            """, nativeQuery = true)
    long conflitosProfissional(
            @Param("idProfissional") Long idProfissional,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("ignorarId") Long ignorarId
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM agendamento a
            WHERE a.id_equipamento = :idEquipamento
              AND a.status IN (
                  :#{T(com.bellapelle.model.StatusAgendamento).Agendado.name()},
                  :#{T(com.bellapelle.model.StatusAgendamento).Confirmado.name()},
                  :#{T(com.bellapelle.model.StatusAgendamento).Realizado.name()}
              )
              AND (:ignorarId IS NULL OR a.id_agendamento <> :ignorarId)
              AND a.data_hora < :fim
              AND DATEADD('MINUTE', a.duracao_minutos, a.data_hora) > :inicio
            """, nativeQuery = true)
    long conflitosEquipamento(
            @Param("idEquipamento") Long idEquipamento,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("ignorarId") Long ignorarId
    );

    @Query(value = """
            SELECT a.*
            FROM agendamento a
            WHERE a.status IN (
                  :#{T(com.bellapelle.model.StatusAgendamento).Agendado.name()},
                  :#{T(com.bellapelle.model.StatusAgendamento).Confirmado.name()},
                  :#{T(com.bellapelle.model.StatusAgendamento).Realizado.name()}
              )
              AND a.data_hora < :fim
              AND DATEADD('MINUTE', a.duracao_minutos, a.data_hora) > :inicio
            ORDER BY a.data_hora, a.id_agendamento
            """, nativeQuery = true)
    List<Agendamento> findAtivosNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}