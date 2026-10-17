package com.bellapelle.repository;

import com.bellapelle.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Integer id);

    @Query("""
            SELECT p FROM Paciente p
            WHERE (:status IS NULL OR p.status = :status)
              AND (:busca IS NULL
                   OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR p.cpf LIKE CONCAT('%', :busca, '%')
                   OR p.telefone LIKE CONCAT('%', :busca, '%'))
            ORDER BY p.nome
            """)
    List<Paciente> pesquisar(@Param("busca") String busca, @Param("status") String status);
}