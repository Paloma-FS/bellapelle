package com.bellapelle.repository;

import com.bellapelle.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Integer> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Integer id);

    @Query("""
            select p from Profissional p
            where (:status is null or p.status = :status)
              and (:busca is null
                   or lower(p.nome) like lower(concat('%', :busca, '%'))
                   or p.cpf like concat('%', :busca, '%')
                   or lower(p.funcao) like lower(concat('%', :busca, '%')))
            order by p.nome
            """)
    List<Profissional> pesquisar(@Param("busca") String busca, @Param("status") String status);
}