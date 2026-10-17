package com.bellapelle.repository;

import com.bellapelle.model.Procedimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcedimentoRepository extends JpaRepository<Procedimento, Long> {

    @Query("""
            select p from Procedimento p
            where (:status is null or p.status = :status)
              and (:busca is null
                   or lower(p.nome) like lower(concat('%', :busca, '%'))
                   or lower(p.categoria) like lower(concat('%', :busca, '%')))
            order by p.nome
            """)
    List<Procedimento> pesquisar(@Param("busca") String busca, @Param("status") String status);
}