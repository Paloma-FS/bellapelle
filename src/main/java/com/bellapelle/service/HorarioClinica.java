package com.bellapelle.service;

import java.time.LocalTime;

public final class HorarioClinica {
    public static final LocalTime ABERTURA = LocalTime.of(9, 0);
    public static final LocalTime ULTIMO_INICIO = LocalTime.of(18, 30);
    public static final LocalTime FECHAMENTO = LocalTime.of(19, 0); // limite para o TÉRMINO
    public static final int PASSO_MINUTOS = 30;

    private HorarioClinica() {}
}