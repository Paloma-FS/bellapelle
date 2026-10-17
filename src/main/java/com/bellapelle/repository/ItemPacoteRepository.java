package com.bellapelle.repository;

import com.bellapelle.model.ItemPacote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemPacoteRepository extends JpaRepository<ItemPacote, Long> {

    @Query("select i from ItemPacote i join fetch i.procedimento where i.pacote.id = :idPacote order by i.procedimento.nome")
    List<ItemPacote> listarPorPacote(@Param("idPacote") Long idPacote);

    boolean existsByPacoteIdAndProcedimentoId(Long idPacote, Long idProcedimento);

    boolean existsByPacoteIdAndProcedimentoIdAndIdNot(Long idPacote, Long idProcedimento, Long id);

    long countByPacoteId(Long idPacote);
}