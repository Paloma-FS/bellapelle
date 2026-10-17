package com.bellapelle.repository;

import com.bellapelle.model.Pacote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PacoteRepository extends JpaRepository<Pacote, Long> {

    @Query("""
            select p from Pacote p
            where (:status is null or p.status = :status)
              and (:busca is null or lower(p.nome) like lower(concat('%', :busca, '%')))
            order by p.nome
            """)
    List<Pacote> pesquisar(@Param("busca") String busca, @Param("status") String status);
}