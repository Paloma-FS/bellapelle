package com.bellapelle.repository;

import com.bellapelle.model.SaldoSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaldoSessaoRepository extends JpaRepository<SaldoSessao, Integer> {

    @Query("""
            SELECT s FROM SaldoSessao s
            JOIN FETCH s.procedimento
            WHERE s.paciente.id = :idPaciente
            ORDER BY s.id
            """)
    List<SaldoSessao> listarPorPaciente(@Param("idPaciente") Integer idPaciente);

    @Query("""
            SELECT s FROM SaldoSessao s
            WHERE s.paciente.id = :idPaciente
              AND s.procedimento.id = :idProcedimento
              AND s.quantidadeUtilizada < s.quantidadeTotal
            ORDER BY s.id
            """)
    List<SaldoSessao> buscarComSaldoDisponivel(@Param("idPaciente") Integer idPaciente,
                                               @Param("idProcedimento") Long idProcedimento);
}