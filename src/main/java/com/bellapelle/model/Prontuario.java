package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "prontuario")
@Getter
@Setter
@NoArgsConstructor
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prontuario")
    private Long id;

    @Column(name = "id_paciente", nullable = false, unique = true)
    private Long idPaciente;

    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;
}