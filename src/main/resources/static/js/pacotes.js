const API = "/api/pacientes";

const campos = [
    "nome",
    "cpf",
    "telefone",
    "email",
    "dataNascimento",
    "genero",
    "peso",
    "altura",
    "preferenciaBebida",
    "preferenciaProfissional",
    "canalOrigem",
    "endereco",
    "contatoEmergenciaNome",
    "contatoEmergenciaTelefone",
    "alergias",
    "restricoesSaude",
    "observacoes"
];

const $ = (id) => document.getElementById(id);
const modal = $("modal");
const modalProntuario = $("modalProntuario");

const ROTULO_PREF = {
    F: "Feminino",
    M: "Masculino",
    I: "Indiferente"
};

function esc(valor) {
    const entidades = {
        "&": "&amp;",
        "<​": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    };

    return String(valor ?? "").replace(/[&<>"']/g, (caractere) => entidades[caractere]);
}

function formatarData(data) {
    if (!data) return "-";

    const partes = String(data).split("-");
    if (partes.length !== 3) return esc(data);

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

async function respostaJson(resp, mensagemPadrao) {
    if (!resp.ok) {
        const erro = await resp.json().catch(() => ({}));
        throw new Error(erro.mensagem || mensagemPadrao);
    }

    return resp.json();
}

async function carregar() {
    const params = new URLSearchParams();

    if ($("busca").value.trim()) {
        params.set("busca", $("busca").value.trim());
    }

    if ($("filtroStatus").value) {
        params.set("status", $("filtroStatus").value);
    }

    try {
        const resp = await fetch(`${API}?${params}`);
        const lista = await respostaJson(resp, "Não foi possível carregar os pacientes.");

        $("tabela").innerHTML = lista.map(linha).join("") ||
            `<tr><td colspan="7">Nenhum paciente encontrado.</td></tr>`;
    } catch (erro) {
        $("tabela").innerHTML =
            `<tr><td colspan="7">${esc(erro.message)}</td></tr>`;
    }
}

function linha(p) {
    const acaoStatus = p.status === "Ativo"
        ? `<button class="perigo" onclick="alterarStatus(${p.id}, 'desabilitar')">Desabilitar</button>`
        : `<button onclick="alterarStatus(${p.id}, 'reativar')">Reativar</button>`;

    return `<tr>
        <td>${esc(p.nome)}</td>
        <td>${esc(p.cpf)}</td>
        <td>${esc(p.telefone)}</td>
        <td>${esc(p.preferenciaBebida)}</td>
        <td>${esc(ROTULO_PREF[p.preferenciaProfissional] ?? p.preferenciaProfissional)}</td>
        <td><span class="badge ${esc(p.status)}">${esc(p.status)}</span></td>
        <td>
            <button onclick="editar(${p.id})">Editar</button>
            <button onclick="verProntuario(${p.id})">Prontuário</button>
            ${acaoStatus}
        </td>
    </tr>`;
}

function abrirNovo() {
    $("form").reset();
    $("id").value = "";
    $("erro").textContent = "";
    $("tituloForm").textContent = "Novo paciente";
    modal.showModal();
}

async function editar(id) {
    try {
        const resp = await fetch(`${API}/${id}`);
        const p = await respostaJson(resp, "Não foi possível carregar o paciente.");

        $("form").reset();
        $("id").value = p.id;
        campos.forEach((campo) => {
            $(campo).value = p[campo] ?? "";
        });

        $("erro").textContent = "";
        $("tituloForm").textContent = "Editar paciente";
        modal.showModal();
    } catch (erro) {
        alert(erro.message);
    }
}

async function alterarStatus(id, acao) {
    if (acao === "desabilitar" && !confirm("Desabilitar este paciente?")) {
        return;
    }

    try {
        const resp = await fetch(`${API}/${id}/${acao}`, { method: "PATCH" });
        await respostaJson(resp, "Não foi possível alterar o status do paciente.");
        await carregar();
    } catch (erro) {
        alert(erro.message);
    }
}

/* ---- consulta somente leitura do prontuário ---- */

function resumoPaciente(paciente) {
    const dados = [
        ["Nome", paciente.nome],
        ["CPF", paciente.cpf],
        ["Telefone", paciente.telefone],
        ["Data de nascimento", formatarData(paciente.dataNascimento)],
        ["Alergias", paciente.alergias],
        ["Restrições de saúde", paciente.restricoesSaude]
    ];

    return dados
        .filter(([, valor]) => valor)
        .map(([rotulo, valor]) =>
            `<p><span class="rotulo">${rotulo}:</span> ${esc(valor)}</p>`
        )
        .join("");
}

function cartaoSessao(sessao) {
    const detalhes = [
        ["Profissional", sessao.profissional],
        ["Equipamento", sessao.equipamento],
        ["Região tratada", sessao.regiaoTratada],
        ["Técnica utilizada", sessao.tecnicaUtilizada]
    ]
        .filter(([, valor]) => valor)
        .map(([rotulo, valor]) =>
            `<p><span class="rotulo">${rotulo}:</span> ${esc(valor)}</p>`
        )
        .join("");

    const dadosInjetavel =
        sessao.fabricante || sessao.numeroLote || sessao.validadeLote
            ? `<p><span class="rotulo">Injetável:</span>
                    Fabricante ${esc(sessao.fabricante || "-")} ·
                    Lote ${esc(sessao.numeroLote || "-")} ·
                    Validade ${formatarData(sessao.validadeLote)}
               </p>`
            : "";

    const anotacoes = [
        ["Evolução", sessao.descricaoEvolucao],
        ["Intercorrências", sessao.intercorrencias]
    ]
        .filter(([, valor]) => valor)
        .map(([rotulo, valor]) =>
            `<p><span class="rotulo">${rotulo}:</span> ${esc(valor)}</p>`
        )
        .join("");

    return `<article class="sessao">
        <h3>${formatarData(sessao.data)} — ${esc(sessao.procedimento)}</h3>
        ${detalhes}
        ${dadosInjetavel}
        ${anotacoes}
    </article>`;
}

async function verProntuario(id) {
    $("prontuarioTitulo").textContent = "Prontuário";
    $("prontuarioResumo").innerHTML = "";
    $("prontuarioConteudo").innerHTML =
        '<p class="prontuario-vazio">Carregando prontuário...</p>';
    $("prontuarioErro").textContent = "";
    $("prontuarioErro").hidden = true;

    modalProntuario.showModal();

    try {
        const [respPaciente, respProntuario] = await Promise.all([
            fetch(`${API}/${id}`),
            fetch(`${API}/${id}/prontuario`)
        ]);

        const [paciente, prontuario] = await Promise.all([
            respostaJson(respPaciente, "Não foi possível carregar os dados do paciente."),
            respostaJson(respProntuario, "Não foi possível carregar o prontuário.")
        ]);

        $("prontuarioTitulo").textContent = `Prontuário — ${paciente.nome}`;
        $("prontuarioResumo").innerHTML = resumoPaciente(paciente);

        if (!prontuario.sessoes || prontuario.sessoes.length === 0) {
            $("prontuarioConteudo").innerHTML =
                '<p class="prontuario-vazio">Paciente ainda não possui atendimentos registrados.</p>';
            return;
        }

        const dataCriacao = prontuario.dataCriacao
            ? `<p><span class="rotulo">Prontuário aberto em:</span> ${formatarData(prontuario.dataCriacao)}</p>`
            : "";

        $("prontuarioConteudo").innerHTML =
            dataCriacao + prontuario.sessoes.map(cartaoSessao).join("");
    } catch (erro) {
        $("prontuarioConteudo").innerHTML = "";
        $("prontuarioErro").textContent = erro.message;
        $("prontuarioErro").hidden = false;
    }
}

$("form").addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const dados = {};
    campos.forEach((campo) => {
        const valor = $(campo).value.trim();
        dados[campo] = valor === ""
            ? null
            : (campo === "peso" || campo === "altura" ? Number(valor) : valor);
    });

    const id = $("id").value;

    try {
        const resp = await fetch(id ? `${API}/${id}` : API, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        });

        if (resp.ok) {
            modal.close();
            await carregar();
        } else {
            const erro = await resp.json().catch(() => ({}));
            $("erro").textContent = erro.mensagem || "Erro ao salvar.";
        }
    } catch {
        $("erro").textContent = "Não foi possível conectar ao servidor.";
    }
});

$("btnNovo").addEventListener("click", abrirNovo);
$("btnCancelar").addEventListener("click", () => modal.close());
$("btnFecharProntuario").addEventListener("click", () => modalProntuario.close());
$("busca").addEventListener("input", carregar);
$("filtroStatus").addEventListener("change", carregar);

carregar();