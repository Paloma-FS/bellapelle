package com.bellapelle.repository;

import com.bellapelle.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RelatorioRepository extends JpaRepository<Agendamento, Long> {

    interface ComissaoProjection {
        String getNomeProfissional();

        BigDecimal getValorComissao();

        String getNomeProcedimento();

        LocalDate getData();
    }

    interface FaltaProjection {
        String getNomePaciente();

        LocalDate getDataFalta();

        Long getFaltasAnteriores();
    }

    interface VendaProjection {
        String getTipo();

        String getNomeItem();

        Long getQuantidadeVendida();

        BigDecimal getValorTotalVendas();

        LocalDate getDataUltimaVenda();
    }

    @Query(value = """
            SELECT
                pf.nome AS nomeProfissional,
                COALESCE(
                    pr.valor * COALESCE(pr.percentual_comissao, 0) / 100,
                    0
                ) AS valorComissao,
                pr.nome AS nomeProcedimento,
                s.data AS data
            FROM sessao_atendimento s
            JOIN procedimento pr
                ON pr.id_procedimento = s.id_procedimento
            JOIN profissional pf
                ON pf.id_profissional = s.id_profissional
            WHERE s.data >= :inicio
              AND s.data <= :fim
            ORDER BY s.data DESC, pf.nome, pr.nome
            """, nativeQuery = true)
    List<ComissaoProjection> listarComissoes(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query(value = """
            SELECT
                p.nome AS nomePaciente,
                CAST(a.data_hora AS DATE) AS dataFalta,
                (
                    SELECT COUNT(*)
                    FROM agendamento anterior
                    WHERE anterior.id_paciente = a.id_paciente
                      AND anterior.status = 'Faltou'
                      AND (
                          anterior.data_hora < a.data_hora
                          OR (
                              anterior.data_hora = a.data_hora
                              AND anterior.id_agendamento < a.id_agendamento
                          )
                      )
                ) AS faltasAnteriores
            FROM agendamento a
            JOIN paciente p
                ON p.id_paciente = a.id_paciente
            WHERE a.status = 'Faltou'
              AND a.data_hora >= :inicio
              AND a.data_hora < :fimExclusivo
            ORDER BY a.data_hora DESC, p.nome
            """, nativeQuery = true)
    List<FaltaProjection> listarFaltas(
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fimExclusivo") java.time.LocalDateTime fimExclusivo
    );

    @Query(value = """
            SELECT
                CASE
                    WHEN v.id_procedimento IS NOT NULL THEN 'PROCEDIMENTO'
                    ELSE 'PACOTE'
                END AS tipo,
                COALESCE(pr.nome, pa.nome) AS nomeItem,
                COUNT(v.id_venda) AS quantidadeVendida,
                SUM(v.valor_total) AS valorTotalVendas,
                MAX(v.data_venda) AS dataUltimaVenda
            FROM venda v
            LEFT JOIN procedimento pr
                ON pr.id_procedimento = v.id_procedimento
            LEFT JOIN pacote pa
                ON pa.id_pacote = v.id_pacote
            WHERE v.data_venda >= :inicio
              AND v.data_venda <= :fim
              AND (
                  :tipo = 'TODOS'
                  OR (
                      :tipo = 'PROCEDIMENTO'
                      AND v.id_procedimento IS NOT NULL
                  )
                  OR (
                      :tipo = 'PACOTE'
                      AND v.id_pacote IS NOT NULL
                  )
              )
            GROUP BY
                v.id_procedimento,
                v.id_pacote,
                pr.nome,
                pa.nome
            ORDER BY quantidadeVendida DESC, nomeItem
            """, nativeQuery = true)
    List<VendaProjection> listarVendas(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("tipo") String tipo
    );
}