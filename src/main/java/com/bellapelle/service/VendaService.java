package com.bellapelle.service;

import com.bellapelle.dto.VendaRequest;
import com.bellapelle.dto.VendaResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.*;
import com.bellapelle.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class VendaService {

    private static final Set<String> FORMAS_PAGAMENTO = Set.of("Pix", "Débito", "Crédito");
    private static final int MAX_PARCELAS = 12;

    private final VendaRepository vendaRepository;
    private final ParcelaRepository parcelaRepository;
    private final PacienteRepository pacienteRepository;
    private final PacoteRepository pacoteRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final ItemPacoteRepository itemPacoteRepository;
    private final SaldoSessaoRepository saldoSessaoRepository;

    public VendaService(VendaRepository vendaRepository, ParcelaRepository parcelaRepository,
                        PacienteRepository pacienteRepository, PacoteRepository pacoteRepository,
                        ProcedimentoRepository procedimentoRepository,
                        ItemPacoteRepository itemPacoteRepository,
                        SaldoSessaoRepository saldoSessaoRepository) {
        this.vendaRepository = vendaRepository;
        this.parcelaRepository = parcelaRepository;
        this.pacienteRepository = pacienteRepository;
        this.pacoteRepository = pacoteRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.itemPacoteRepository = itemPacoteRepository;
        this.saldoSessaoRepository = saldoSessaoRepository;
    }

    @Transactional
    public VendaResponse criar(VendaRequest req) {
        if (req.idPaciente() == null) {
            throw new RegraNegocioException("Informe a paciente da venda.");
        }
        Paciente paciente = pacienteRepository.findById(req.idPaciente())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrada."));

        boolean porPacote = req.idPacote() != null;
        boolean porProcedimento = req.idProcedimento() != null;
        if (porPacote == porProcedimento) {
            throw new RegraNegocioException("A venda deve ter um pacote ou um procedimento, mas não ambos.");
        }

        String forma = req.formaPagamento();
        if (forma == null || !FORMAS_PAGAMENTO.contains(forma)) {
            throw new RegraNegocioException("Forma de pagamento inválida.");
        }

        int numeroParcelas = req.numeroParcelas() == null ? 1 : req.numeroParcelas();
        if (numeroParcelas < 1 || numeroParcelas > MAX_PARCELAS) {
            throw new RegraNegocioException("O parcelamento deve ser entre 1 e " + MAX_PARCELAS + " vezes.");
        }
        if (!"Crédito".equals(forma) && numeroParcelas > 1) {
            throw new RegraNegocioException("Parcelamento é permitido apenas no cartão de crédito.");
        }

        Venda venda = new Venda();
        venda.setPaciente(paciente);
        venda.setFormaPagamento(forma);
        venda.setDataVenda(req.dataVenda() != null ? req.dataVenda() : LocalDate.now());

        if (porPacote) {
            Pacote pacote = pacoteRepository.findById(req.idPacote())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Pacote não encontrado."));
            if (!"Ativo".equals(pacote.getStatus())) {
                throw new RegraNegocioException("Este pacote está inativo.");
            }
            venda.setPacote(pacote);
            venda.setValorTotal(pacote.getValorTotal());
        } else {
            Procedimento procedimento = procedimentoRepository.findById(req.idProcedimento())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado."));
            if (!"Ativo".equals(procedimento.getStatus())) {
                throw new RegraNegocioException("Este procedimento está inativo.");
            }
            venda.setProcedimento(procedimento);
            venda.setValorTotal(procedimento.getValor());
        }

        processarPagamento(venda, req, numeroParcelas);
        vendaRepository.save(venda);

        // credita as sessões no saldo da paciente
        if (porPacote) {
            creditarSessoesDoPacote(paciente, venda.getPacote(), venda);
        } else {
            creditarSessoes(paciente, venda.getProcedimento(), venda, 1);
        }

        return VendaResponse.de(venda);
    }

    @Transactional
    public List<VendaResponse> listar() {
        vencerParcelasAtrasadas();
        return vendaRepository.findAllByOrderByDataVendaDescIdDesc().stream()
                .map(VendaResponse::de).toList();
    }

    @Transactional
    public VendaResponse buscar(Integer id) {
        vencerParcelasAtrasadas();
        return VendaResponse.de(obter(id));
    }

    @Transactional
    public VendaResponse pagarParcela(Integer idParcela) {
        Parcela parcela = parcelaRepository.findById(idParcela)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parcela não encontrada."));
        if ("Paga".equals(parcela.getStatus())) {
            throw new RegraNegocioException("Esta parcela já está paga.");
        }
        parcela.setStatus("Paga");
        parcela.setDataPagamento(LocalDate.now());
        return VendaResponse.de(parcela.getVenda());
    }

    private void processarPagamento(Venda venda, VendaRequest req, int numeroParcelas) {
        BigDecimal total = venda.getValorTotal();

        if (numeroParcelas == 1) {
            venda.setValorEntrada(total);
            return;
        }

        BigDecimal entrada = req.valorEntrada() == null ? BigDecimal.ZERO : req.valorEntrada();
        if (entrada.signum() < 0 || entrada.compareTo(total) >= 0) {
            throw new RegraNegocioException("O valor de entrada deve ser maior ou igual a zero e menor que o valor total.");
        }
        venda.setValorEntrada(entrada);

        BigDecimal restante = total.subtract(entrada);
        BigDecimal valorBase = restante.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.DOWN);
        BigDecimal primeira = restante.subtract(valorBase.multiply(BigDecimal.valueOf(numeroParcelas - 1)));

        LocalDate vencimento = venda.getDataVenda().plusMonths(1);
        for (int i = 1; i <= numeroParcelas; i++) {
            Parcela parcela = new Parcela();
            parcela.setVenda(venda);
            parcela.setNumeroParcela(i);
            parcela.setValorParcela(i == 1 ? primeira : valorBase);
            parcela.setDataVencimento(vencimento);
            parcela.setStatus("Pendente");
            venda.getParcelas().add(parcela);
            vencimento = vencimento.plusMonths(1);
        }
    }

    private void creditarSessoesDoPacote(Paciente paciente, Pacote pacote, Venda venda) {
        List<ItemPacote> itens = itemPacoteRepository.listarPorPacote(pacote.getId());
        if (itens.isEmpty()) {
            throw new RegraNegocioException("O pacote não possui procedimentos cadastrados.");
        }
        for (ItemPacote item : itens) {
            creditarSessoes(paciente, item.getProcedimento(), venda, item.getNumeroSessoes());
        }
    }

    private void creditarSessoes(Paciente paciente, Procedimento procedimento, Venda venda, int quantidade) {
        SaldoSessao saldo = new SaldoSessao();
        saldo.setPaciente(paciente);
        saldo.setProcedimento(procedimento);
        saldo.setVendaOrigem(venda);
        saldo.setQuantidadeTotal(quantidade);
        saldo.setQuantidadeUtilizada(0);
        saldoSessaoRepository.save(saldo);
    }

    private void vencerParcelasAtrasadas() {
        List<Parcela> atrasadas = parcelaRepository
                .findByStatusAndDataVencimentoBefore("Pendente", LocalDate.now());
        for (Parcela parcela : atrasadas) {
            parcela.setStatus("Vencida");
        }
    }

    private Venda obter(Integer id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada."));
    }
}