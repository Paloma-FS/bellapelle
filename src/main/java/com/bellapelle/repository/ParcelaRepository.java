package com.bellapelle.repository;

import com.bellapelle.model.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ParcelaRepository extends JpaRepository<Parcela, Integer> {

    List<Parcela> findByStatusAndDataVencimentoBefore(String status, LocalDate data);
}