package com.bellapelle.repository;

import com.bellapelle.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    @Query("""
            select e from Equipamento e
            where (:status is null or e.status = :status)
              and (:busca is null
                   or lower(e.nomeModelo) like lower(concat('%', :busca, '%'))
                   or lower(e.tipo) like lower(concat('%', :busca, '%'))
                   or lower(e.numeroSerie) like lower(concat('%', :busca, '%')))
            order by e.nomeModelo
            """)
    List<Equipamento> pesquisar(@Param("busca") String busca, @Param("status") String status);
}