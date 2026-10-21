package com.bellapelle.service;

import com.bellapelle.dto.PainelResponse;
import com.bellapelle.dto.PainelResponse.AgendaItem;
import com.bellapelle.dto.PainelResponse.Alerta;
import com.bellapelle.dto.PainelResponse.Resumo;
import com.bellapelle.repository.PainelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class PainelService {

    private static final int JANELA_PADRAO_HORAS = 2;
    private static final int JANELA_MAXIMA_HORAS = 24;
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PainelRepository repository;

    public PainelService(PainelRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PainelResponse carregar(Integer horasSolicitadas) {
        int horas = normalizarJanela(horasSolicitadas);

        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioDia = hoje.atStartOfDay();
        LocalDateTime fimDia = hoje.plusDays(1).atStartOfDay();
        LocalDate inicioMes = hoje.withDayOfMonth(1);

        Resumo resumo = new Resumo(
                repository.contarAtendimentosHoje(inicioDia, fimDia),
                repository.contarFaltasNoMes(
                        inicioMes.atStartOfDay(),
                        inicioMes.plusMonths(1).atStartOfDay()
                ),
                repository.contarParcelasVencidas(hoje),
                repository.contarPacientesComSaldoBaixo()
        );

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime fimJanela = agora.plusHours(horas);

        List<AgendaItem> agenda = repository
                .listarAgenda(agora, fimJanela)
                .stream()
                .map(linha -> converterAgenda(linha, agora))
                .toList();

        return new PainelResponse(
                resumo,
                agenda,
                montarAlertas(hoje, inicioMes),
                horas
        );
    }

    private int normalizarJanela(Integer horas) {
        if (horas == null || horas < 2) {
            return JANELA_PADRAO_HORAS;
        }

        return Math.min(horas, JANELA_MAXIMA_HORAS);
    }

    private AgendaItem converterAgenda(
            Object[] linha,
            LocalDateTime agora
    ) {
        LocalDateTime dataHora = paraDataHora(linha[6]);
        Integer duracao = linha[7] == null
                ? 0
                : ((Number) linha[7]).intValue();

        boolean emAndamento =
                !agora.isBefore(dataHora)
                        && agora.isBefore(dataHora.plusMinutes(duracao));

        return new AgendaItem(
                ((Number) linha[0]).longValue(),
                texto(linha[1]),
                texto(linha[2]),
                texto(linha[3]),
                texto(linha[4]),
                texto(linha[5]),
                dataHora,
                duracao,
                texto(linha[8]),
                emAndamento
        );
    }

    private List<Alerta> montarAlertas(
            LocalDate hoje,
            LocalDate inicioMes
    ) {
        List<Alerta> alertas = new ArrayList<>();

        for (Object[] linha : repository.listarAlertasAnamnese()) {
            List<String> detalhes = new ArrayList<>();

            adicionarSeVerdadeiro(
                    detalhes,
                    linha[2],
                    "Uso de isotretinoína"
            );
            adicionarSeVerdadeiro(
                    detalhes,
                    linha[3],
                    "Gestante ou lactante"
            );
            adicionarSeVerdadeiro(
                    detalhes,
                    linha[4],
                    "Histórico de queloide"
            );

            adicionarTexto(detalhes, linha[1]);
            adicionarTexto(detalhes, linha[5]);

            alertas.add(new Alerta(
                    "anamnese",
                    "vermelho",
                    texto(linha[0]) + " — revisar anamnese",
                    detalhes.isEmpty()
                            ? "Verifique a anamnese antes do atendimento."
                            : String.join(" · ", detalhes)
            ));
        }

        for (Object[] linha : repository.listarParcelasVencidas(hoje)) {
            String vencimento = paraDataTexto(linha[2]);
            String valor = formatarMoeda(linha[3]);

            alertas.add(new Alerta(
                    "parcela",
                    "ambar",
                    texto(linha[0]) + " — parcela vencida",
                    "Parcela " + texto(linha[1])
                            + " · vencimento " + vencimento
                            + " · R$ " + valor
            ));
        }

        for (Object[] linha
                : repository.listarSaldosSemSessaoNoMes(inicioMes)) {
            alertas.add(new Alerta(
                    "sem-sessao",
                    "ambar",
                    texto(linha[0]) + " — sem sessão no mês",
                    texto(linha[1]) + " · "
                            + texto(linha[2]) + " sessão(ões) disponível(is)"
            ));
        }

        for (Object[] linha
                : repository.listarEquipamentosEmManutencao()) {
            String detalhe = linha[1] == null
                    ? "Sem data prevista informada."
                    : "Previsão: " + paraDataTexto(linha[1]);

            alertas.add(new Alerta(
                    "equipamento",
                    "vermelho",
                    texto(linha[0]) + " — em manutenção",
                    detalhe
            ));
        }

        return alertas;
    }

    private void adicionarSeVerdadeiro(
            List<String> destino,
            Object valor,
            String rotulo
    ) {
        if (Boolean.TRUE.equals(paraBoolean(valor))) {
            destino.add(rotulo);
        }
    }

    private void adicionarTexto(List<String> destino, Object valor) {
        String texto = texto(valor);

        if (!texto.isBlank() && !"null".equalsIgnoreCase(texto)) {
            destino.add(texto);
        }
    }

    private Boolean paraBoolean(Object valor) {
        if (valor == null) {
            return false;
        }

        if (valor instanceof Boolean b) {
            return b;
        }

        if (valor instanceof Number n) {
            return n.intValue() != 0;
        }

        return Boolean.parseBoolean(valor.toString());
    }

    private LocalDateTime paraDataHora(Object valor) {
        if (valor instanceof LocalDateTime dataHora) {
            return dataHora;
        }

        if (valor instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        return LocalDateTime.parse(valor.toString().replace(" ", "T"));
    }

    private String paraDataTexto(Object valor) {
        if (valor == null) {
            return "sem data";
        }

        if (valor instanceof LocalDate data) {
            return data.format(FORMATO_DATA);
        }

        if (valor instanceof Date data) {
            return data.toLocalDate().format(FORMATO_DATA);
        }

        return LocalDate.parse(valor.toString()).format(FORMATO_DATA);
    }

    private String formatarMoeda(Object valor) {
        if (valor == null) {
            return "0,00";
        }

        BigDecimal decimal = valor instanceof BigDecimal b
                ? b
                : new BigDecimal(valor.toString());

        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString()
                .replace(".", ",");
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}