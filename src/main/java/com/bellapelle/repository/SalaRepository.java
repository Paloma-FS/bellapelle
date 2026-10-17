package com.bellapelle.repository;

import com.bellapelle.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    @Query("""
            select s from Sala s
            where (:status is null or s.status = :status)
              and (:busca is null or lower(s.nome) like lower(concat('%', :busca, '%')))
            order by s.nome
            """)
    List<Sala> pesquisar(@Param("busca") String busca, @Param("status") String status);
}