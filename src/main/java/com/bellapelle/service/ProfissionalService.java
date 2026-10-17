package com.bellapelle.service;

import com.bellapelle.dto.ProfissionalRequest;
import com.bellapelle.dto.ProfissionalResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.Profissional;
import com.bellapelle.repository.ProfissionalProcedimentoRepository;
import com.bellapelle.repository.ProfissionalRepository;
import com.bellapelle.service.regra.RegraProcedimentosProfissional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class ProfissionalService {

    private static final Set<String> VINCULOS =
            Set.of("CLT", "Prestador de serviço", "Sócio");

    private static final Set<String> GENEROS =
            Set.of("F", "M", "I");

    private final ProfissionalRepository repository;
    private final ProfissionalProcedimentoRepository procedimentosRepository;
    private final RegraProcedimentosProfissional regraProcedimentos;

    public ProfissionalService(
            ProfissionalRepository repository,
            ProfissionalProcedimentoRepository procedimentosRepository,
            RegraProcedimentosProfissional regraProcedimentos) {

        this.repository = repository;
        this.procedimentosRepository = procedimentosRepository;
        this.regraProcedimentos = regraProcedimentos;
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listar(String busca, String status) {
        String b = vazio(busca) ? null : busca.trim();
        String s = vazio(status) ? null : status.trim();

        return repository.pesquisar(b, s)
                .stream()
                .map(ProfissionalResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfissionalResponse buscar(Integer id) {
        return ProfissionalResponse.de(obter(id));
    }

    @Transactional(readOnly = true)
    public List<Long> listarProcedimentos(Integer id) {
        obter(id);
        return procedimentosRepository.listarIds(id);
    }

    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest req) {
        String cpf = normalizarCpf(req.cpf());

        if (repository.existsByCpf(cpf)) {
            throw new RegraNegocioException(
                    "Já existe profissional cadastrado com este CPF."
            );
        }

        List<Long> procedimentos =
                regraProcedimentos.validar(req.procedimentosIds());

        Profissional p = new Profissional();
        preencher(p, req, cpf);
        p.setStatus("Ativo");

        // Garante que o profissional exista no banco antes dos vínculos.
        p = repository.saveAndFlush(p);

        procedimentosRepository.substituir(
                p.getId(),
                procedimentos
        );

        return ProfissionalResponse.de(p);
    }

    @Transactional
    public ProfissionalResponse atualizar(
            Integer id,
            ProfissionalRequest req) {

        bloquearProfissional(id);

        Profissional p = obter(id);
        String cpf = normalizarCpf(req.cpf());

        if (repository.existsByCpfAndIdNot(cpf, id)) {
            throw new RegraNegocioException(
                    "Já existe outro profissional com este CPF."
            );
        }

        /*
         * Campo ausente/null: preserva os vínculos existentes.
         * Lista vazia: remove todas as habilitações.
         * Lista preenchida: substitui pelas habilitações selecionadas.
         */
        List<Long> procedimentos = req.procedimentosIds() == null
                ? null
                : regraProcedimentos.validar(req.procedimentosIds());

        preencher(p, req, cpf);
        p = repository.saveAndFlush(p);

        if (procedimentos != null) {
            procedimentosRepository.substituir(id, procedimentos);
        }

        return ProfissionalResponse.de(p);
    }

    @Transactional
    public ProfissionalResponse desabilitar(Integer id) {
        bloquearProfissional(id);

        Profissional p = obter(id);
        p.setStatus("Inativo");

        if (p.getDataDesligamento() == null) {
            p.setDataDesligamento(LocalDate.now());
        }

        return ProfissionalResponse.de(repository.save(p));
    }

    @Transactional
    public ProfissionalResponse reativar(Integer id) {
        bloquearProfissional(id);

        Profissional p = obter(id);
        p.setStatus("Ativo");
        p.setDataDesligamento(null);

        return ProfissionalResponse.de(repository.save(p));
    }

    private void bloquearProfissional(Integer id) {
        if (!procedimentosRepository.bloquearProfissional(id)) {
            throw new RecursoNaoEncontradoException(
                    "Profissional não encontrado."
            );
        }
    }

    private Profissional obter(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Profissional não encontrado."
                        )
                );
    }

    private void preencher(
            Profissional p,
            ProfissionalRequest r,
            String cpf) {

        if (!VINCULOS.contains(r.tipoVinculo())) {
            throw new RegraNegocioException(
                    "Tipo de vínculo inválido."
            );
        }

        String genero = normalizarGenero(r.genero());

        p.setNome(r.nome().trim());
        p.setCpf(cpf);
        p.setDataNascimento(r.dataNascimento());
        p.setGenero(genero);

        p.setTelefone(
                vazio(r.telefone()) ? null : r.telefone().trim()
        );

        p.setEmail(
                vazio(r.email()) ? null : r.email().trim()
        );

        p.setNumeroConselho(
                vazio(r.numeroConselho())
                        ? null
                        : r.numeroConselho().trim()
        );

        p.setTipoConselho(
                vazio(r.tipoConselho())
                        ? null
                        : r.tipoConselho().trim()
        );

        p.setUfConselho(
                vazio(r.ufConselho())
                        ? null
                        : r.ufConselho().trim().toUpperCase()
        );

        p.setFuncao(r.funcao().trim());
        p.setTipoVinculo(r.tipoVinculo());
        p.setPercentualComissao(r.percentualComissao());

        p.setDiasHorarioTrabalho(
                vazio(r.diasHorarioTrabalho())
                        ? null
                        : r.diasHorarioTrabalho().trim()
        );

        p.setDataAdmissao(r.dataAdmissao());
    }

    /**
     * Gênero é opcional. Se informado, deve ser F, M ou I.
     */
    private String normalizarGenero(String genero) {
        if (vazio(genero)) {
            return null;
        }

        String g = genero.trim().toUpperCase();

        if (!GENEROS.contains(g)) {
            throw new RegraNegocioException(
                    "Gênero inválido. Use F, M ou I."
            );
        }

        return g;
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private String normalizarCpf(String cpf) {
        String d = cpf.replaceAll("\\D", "");

        if (d.length() != 11) {
            throw new RegraNegocioException(
                    "CPF deve ter 11 dígitos."
            );
        }

        return d.substring(0, 3)
                + "."
                + d.substring(3, 6)
                + "."
                + d.substring(6, 9)
                + "-"
                + d.substring(9);
    }
}