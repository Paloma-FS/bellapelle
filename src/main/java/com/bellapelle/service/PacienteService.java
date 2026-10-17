package com.bellapelle.service;

import com.bellapelle.dto.PacienteRequest;
import com.bellapelle.dto.PacienteResponse;
import com.bellapelle.exception.RecursoNaoEncontradoException;
import com.bellapelle.exception.RegraNegocioException;
import com.bellapelle.model.Paciente;
import com.bellapelle.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class PacienteService {

    private static final Set<String> BEBIDAS = Set.of("Chá", "Café", "Sem preferência");
    private static final Set<String> PROFISSIONAIS = Set.of("F", "M", "I");

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar(String busca, String status) {
        String b = (busca == null || busca.isBlank()) ? null : busca.trim();
        String s = (status == null || status.isBlank()) ? null : status.trim();
        return repository.pesquisar(b, s).stream().map(PacienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscar(Integer id) {
        return PacienteResponse.de(obter(id));
    }

    @Transactional
    public PacienteResponse criar(PacienteRequest req) {
        String cpf = normalizarCpf(req.cpf());
        if (repository.existsByCpf(cpf)) {
            throw new RegraNegocioException("Já existe paciente cadastrado com este CPF.");
        }
        Paciente p = new Paciente();
        preencher(p, req, cpf);
        p.setStatus("Ativo");
        return PacienteResponse.de(repository.save(p));
    }

    @Transactional
    public PacienteResponse atualizar(Integer id, PacienteRequest req) {
        Paciente p = obter(id);
        String cpf = normalizarCpf(req.cpf());
        if (repository.existsByCpfAndIdNot(cpf, id)) {
            throw new RegraNegocioException("Já existe outro paciente com este CPF.");
        }
        preencher(p, req, cpf);
        return PacienteResponse.de(repository.save(p));
    }

    @Transactional
    public PacienteResponse desabilitar(Integer id) {
        Paciente p = obter(id);
        p.setStatus("Inativo");
        return PacienteResponse.de(repository.save(p));
    }

    @Transactional
    public PacienteResponse reativar(Integer id) {
        Paciente p = obter(id);
        p.setStatus("Ativo");
        return PacienteResponse.de(repository.save(p));
    }

    private Paciente obter(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    private void preencher(Paciente p, PacienteRequest r, String cpf) {
        String bebida = vazio(r.preferenciaBebida()) ? "Sem preferência" : r.preferenciaBebida();
        String prof = r.preferenciaProfissional();

        if (!BEBIDAS.contains(bebida)) {
            throw new RegraNegocioException("Preferência de bebida inválida.");
        }

        if (vazio(prof) || !PROFISSIONAIS.contains(prof)) {
            throw new RegraNegocioException(
                    "Preferência de profissional inválida. Informe F (Feminino), M (Masculino) ou I (Indiferente)."
            );
        }

        p.setNome(r.nome().trim());
        p.setTelefone(r.telefone().trim());
        p.setEmail(vazio(r.email()) ? null : r.email().trim());
        p.setDataNascimento(r.dataNascimento());
        p.setCpf(cpf);
        p.setEndereco(r.endereco());
        p.setGenero(r.genero());
        p.setPeso(r.peso());
        p.setAltura(r.altura());
        p.setAlergias(r.alergias());
        p.setRestricoesSaude(r.restricoesSaude());
        p.setPreferenciaBebida(bebida);
        p.setPreferenciaProfissional(prof);
        p.setContatoEmergenciaNome(r.contatoEmergenciaNome());
        p.setContatoEmergenciaTelefone(r.contatoEmergenciaTelefone());
        p.setCanalOrigem(vazio(r.canalOrigem()) ? null : r.canalOrigem());
        p.setObservacoes(r.observacoes());
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private String normalizarCpf(String cpf) {
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11) {
            throw new RegraNegocioException("CPF deve ter 11 dígitos.");
        }
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }
}