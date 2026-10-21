package com.bellapelle.repository;

import com.bellapelle.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Integer> {

    List<Venda> findAllByOrderByDataVendaDescIdDesc();
}