package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sala")
@Getter
@Setter
@NoArgsConstructor
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sala")
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false, length = 30)
    private String status = "Ativa";
}