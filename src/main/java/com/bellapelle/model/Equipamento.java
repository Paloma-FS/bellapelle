package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "equipamento")
@Getter
@Setter
@NoArgsConstructor
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipamento")
    private Long id;

    @Column(name = "nome_modelo", nullable = false, length = 150)
    private String nomeModelo;

    @Column(nullable = false, length = 100)
    private String tipo;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    // Guarda o ID da sala; o banco mantém e verifica a chave estrangeira.
    @Column(name = "id_sala")
    private Long idSala;

    @Column(nullable = false, length = 30)
    private String status = "Disponível";

    @Column(name = "ultima_manutencao")
    private LocalDate ultimaManutencao;

    @Column(name = "proxima_manutencao")
    private LocalDate proximaManutencao;
}