package com.bellapelle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_pacote")
@Getter
@Setter
@NoArgsConstructor
public class ItemPacote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_pacote")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pacote", nullable = false)
    private Pacote pacote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_procedimento", nullable = false)
    private Procedimento procedimento;

    @Column(name = "numero_sessoes", nullable = false)
    private Integer numeroSessoes;

    @Column(name = "periodicidade_dias")
    private Integer periodicidadeDias;
}