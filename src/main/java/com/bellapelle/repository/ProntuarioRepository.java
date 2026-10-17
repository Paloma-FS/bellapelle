package com.bellapelle.repository;

import com.bellapelle.model.Prontuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {

    @Query(value = """
            SELECT id_paciente
            FROM paciente
            WHERE id_paciente = :idPaciente
            FOR UPDATE
            """, nativeQuery = true)
    Optional<Long> bloquearPaciente(@Param("idPaciente") Long idPaciente);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from Prontuario p
            where p.idPaciente = :idPaciente
            """)
    Optional<Prontuario> buscarParaAtualizacao(
            @Param("idPaciente") Long idPaciente);
}