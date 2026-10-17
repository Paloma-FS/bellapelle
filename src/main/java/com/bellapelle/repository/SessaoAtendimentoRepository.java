package com.bellapelle.repository;

import com.bellapelle.model.SessaoAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SessaoAtendimentoRepository
        extends JpaRepository<SessaoAtendimento, Long> {

    boolean existsByIdAgendamento(Long idAgendamento);

    interface HistoricoView {
        Long getId();

        LocalDate getData();

        String getNomeProcedimento();

        String getNomeProfissional();

        String getDescricaoEvolucao();

        String getIntercorrencias();

        String getNumeroLote();

        String getFabricante();

        LocalDate getValidadeLote();
    }

    @Query(value = """
            SELECT
                s.id_sessao AS id,
                s.data AS data,
                pr.nome AS nomeProcedimento,
                pf.nome AS nomeProfissional,
                s.descricao_evolucao AS descricaoEvolucao,
                s.intercorrencias AS intercorrencias,
                s.numero_lote AS numeroLote,
                s.fabricante AS fabricante,
                s.validade_lote AS validadeLote
            FROM sessao_atendimento s
            JOIN prontuario pt
                ON pt.id_prontuario = s.id_prontuario
            JOIN procedimento pr
                ON pr.id_procedimento = s.id_procedimento
            JOIN profissional pf
                ON pf.id_profissional = s.id_profissional
            WHERE pt.id_paciente = :idPaciente
            ORDER BY s.data DESC, s.id_sessao DESC
            """, nativeQuery = true)
    List<HistoricoView> historicoPaciente(
            @Param("idPaciente") Long idPaciente);
}