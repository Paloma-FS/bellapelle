const API = "/api/pacientes";

const campos = [
    "nome", "cpf", "telefone", "email", "dataNascimento", "genero",
    "peso", "altura", "preferenciaBebida", "preferenciaProfissional",
    "canalOrigem", "endereco", "contatoEmergenciaNome",
    "contatoEmergenciaTelefone", "alergias", "restricoesSaude", "observacoes"
];

const ROTULO_PREF = {
    F: "Feminino",
    M: "Masculino",
    I: "Indiferente"
};

const $ = (id) => document.getElementById(id);

const modal = $("modal");
const modalProntuario = $("modalProntuario");
const modalAnamnese = $("modalAnamnese");

let idPacienteAnamnese = null;
let versaoBusca = 0;

// Liga um evento sem quebrar o script caso o elemento não exista no HTML.
function ouvir(id, evento, funcao) {
    const elemento = $(id);

    if (elemento) {
        elemento.addEventListener(evento, funcao);
    } else {
        console.warn("[pacientes.js] elemento não encontrado no index.html:", id);
    }
}

function esc(valor) {
    return String(valor ?? "").replace(/[&<>"']/g, (caractere) => ({
        "&": "&amp;",
        "<​": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[caractere]);
}

function formatarData(data) {
    if (!data) return "";

    const [ano, mes, dia] = String(data).split("-");
    if (!ano || !mes || !dia) return esc(data);

    return `${dia}/${mes}/${ano}`;
}

async function buscarJson(url, mensagemErro) {
    const resposta = await fetch(url);

    if (!resposta.ok) {
        const erro = await resposta.json().catch(() => ({}));
        throw new Error(erro.mensagem || mensagemErro);
    }

    return resposta.json();
}

async function carregar() {
    const versaoAtual = ++versaoBusca;
    const parametros = new URLSearchParams();

    if ($("busca") && $("busca").value.trim()) {
        parametros.set("busca", $("busca").value.trim());
    }

    if ($("filtroStatus") && $("filtroStatus").value) {
        parametros.set("status", $("filtroStatus").value);
    }

    try {
        const resposta = await fetch(`${API}?${parametros}`);

        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({}));
            throw new Error(erro.mensagem || "Não foi possível carregar os pacientes.");
        }

        const lista = await resposta.json();

        // Evita que uma busca mais lenta sobrescreva uma busca mais recente.
        if (versaoAtual !== versaoBusca) return;

        $("tabela").innerHTML = lista.map(linha).join("") ||
            `<tr><td colspan="7">Nenhum paciente encontrado.</td></tr>`;
    } catch (erro) {
        if (versaoAtual !== versaoBusca) return;

        $("tabela").innerHTML =
            `<tr><td colspan="7">${esc(erro.message || "Erro ao carregar pacientes.")}</td></tr>`;
    }
}

function linha(paciente) {
    const acaoStatus = paciente.status === "Ativo"
        ? `<button class="perigo" onclick="alterarStatus(${paciente.id}, 'desabilitar')">Desabilitar</button>`
        : `<button onclick="alterarStatus(${paciente.id}, 'reativar')">Reativar</button>`;

    return `<tr>
        <td>${esc(paciente.nome)}</td>
        <td>${esc(paciente.cpf)}</td>
        <td>${esc(paciente.telefone)}</td>
        <td>${esc(paciente.preferenciaBebida)}</td>
        <td>${esc(ROTULO_PREF[paciente.preferenciaProfissional] ?? paciente.preferenciaProfissional)}</td>
        <td><span class="badge ${esc(paciente.status)}">${esc(paciente.status)}</span></td>
        <td>
            <button onclick="editar(${paciente.id})">Editar</button>
            <button onclick="verProntuario(${paciente.id})">Prontuário</button>
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
        const paciente = await buscarJson(
            `${API}/${id}`,
            "Não foi possível carregar o paciente."
        );

        $("form").reset();
        $("id").value = paciente.id;

        campos.forEach((campo) => {
            $(campo).value = paciente[campo] ?? "";
        });

        $("erro").textContent = "";
        $("tituloForm").textContent = "Editar paciente";
        modal.showModal();
    } catch (erro) {
        alert(erro.message || "Não foi possível carregar o paciente.");
    }
}

async function alterarStatus(id, acao) {
    if (acao === "desabilitar" && !confirm("Desabilitar este paciente?")) {
        return;
    }

    try {
        const resposta = await fetch(`${API}/${id}/${acao}`, {
            method: "PATCH"
        });

        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({}));
            throw new Error(erro.mensagem || "Não foi possível alterar o status.");
        }

        await carregar();
    } catch (erro) {
        alert(erro.message || "Não foi possível conectar ao servidor.");
    }
}

function resumoPaciente(paciente) {
    const dados = [
        ["Nome", paciente.nome],
        ["CPF", paciente.cpf],
        ["Telefone", paciente.telefone],
        ["Data de nascimento", formatarData(paciente.dataNascimento)],
        ["Alergias do cadastro", paciente.alergias],
        ["Restrições de saúde", paciente.restricoesSaude]
    ];

    return dados
        .filter(([, valor]) => valor)
        .map(([rotulo, valor]) =>
            `<p><strong>${rotulo}:</strong> ${esc(valor)}</p>`
        )
        .join("");
}

function textoSimNao(valor) {
    if (valor === true) return "Sim";
    if (valor === false) return "Não";
    return "-";
}

function montarAnamnese(anamnese) {
    if (!anamnese) {
        return `<p class="prontuario-vazio">Não há anamnese cadastrada.</p>`;
    }

    const itens = [
        ["Preenchida em", formatarData(anamnese.dataPreenchimento)],
        ["Alergias registradas na anamnese", anamnese.alergias],
        ["Medicações contínuas", anamnese.medicacoesContinuas],
        ["Cirurgias prévias", anamnese.cirurgiasPrevias],
        ["Gestante ou lactante", textoSimNao(anamnese.gestanteOuLactante)],
        ["Uso de isotretinoína", textoSimNao(anamnese.usoIsotretinoina)],
        ["Histórico de queloide", textoSimNao(anamnese.historicoQueloide)],
        ["Doenças de pele", anamnese.doencasDePele],
        ["Contraindicações identificadas", anamnese.contraindicacoesIdentificadas]
    ];

    return itens
        .filter(([, valor]) => valor && valor !== "-")
        .map(([rotulo, valor]) =>
            `<p><strong>${rotulo}:</strong> ${esc(valor)}</p>`
        )
        .join("");
}

function cartaoSessao(sessao) {
    const camposSessao = [
        ["Profissional", sessao.profissional],
        ["Equipamento", sessao.equipamento],
        ["Região tratada", sessao.regiaoTratada],
        ["Técnica utilizada", sessao.tecnicaUtilizada],
        ["Número do lote", sessao.numeroLote],
        ["Validade do lote", formatarData(sessao.validadeLote)],
        ["Evolução", sessao.descricaoEvolucao],
        ["Intercorrências", sessao.intercorrencias]
    ];

    const detalhes = camposSessao
        .filter(([, valor]) => valor)
        .map(([rotulo, valor]) =>
            `<p><strong>${rotulo}:</strong> ${esc(valor)}</p>`
        )
        .join("");

    return `<article class="sessao">
        <h3>${formatarData(sessao.data)} — ${esc(sessao.procedimento || "Atendimento")}</h3>
        ${detalhes || `<p class="prontuario-vazio">Sem detalhes adicionais registrados.</p>`}
    </article>`;
}

async function atualizarConteudoProntuario(id) {
    const prontuario = await buscarJson(
        `${API}/${id}/prontuario`,
        "Não foi possível carregar o prontuário."
    );

    const sessoes = Array.isArray(prontuario.sessoes)
        ? prontuario.sessoes
        : [];

    const dataCriacao = prontuario.dataCriacao
        ? `<p><strong>Prontuário aberto em:</strong> ${formatarData(prontuario.dataCriacao)}</p>`
        : `<p class="prontuario-vazio">Ainda não há prontuário formal aberto para este paciente.</p>`;

    const botaoAnamnese = prontuario.anamnese
        ? "Editar anamnese"
        : "Preencher anamnese";

    const secaoAnamnese = `
        <section class="sessao">
            <h3>Anamnese</h3>
            <p>
                <button type="button" onclick="abrirAnamnese(${id})">
                    ${botaoAnamnese}
                </button>
            </p>
            ${montarAnamnese(prontuario.anamnese)}
        </section>`;

    const historico = sessoes.length
        ? sessoes.map(cartaoSessao).join("")
        : `<p class="prontuario-vazio">Nenhum atendimento registrado no prontuário.</p>`;

    $("prontuarioConteudo").innerHTML =
        `${dataCriacao}${secaoAnamnese}<h3>Histórico de atendimentos</h3>${historico}`;
}

async function verProntuario(id) {
    if (!modalProntuario) {
        console.warn("[pacientes.js] modal do prontuário não encontrado no index.html.");
        return;
    }

    $("prontuarioTitulo").textContent = "Prontuário";
    $("prontuarioResumo").innerHTML = "";
    $("prontuarioConteudo").innerHTML =
        `<p class="prontuario-vazio">Carregando prontuário...</p>`;
    $("prontuarioErro").textContent = "";
    $("prontuarioErro").hidden = true;

    modalProntuario.showModal();

    try {
        const paciente = await buscarJson(
            `${API}/${id}`,
            "Não foi possível carregar os dados do paciente."
        );

        $("prontuarioTitulo").textContent = `Prontuário — ${paciente.nome}`;
        $("prontuarioResumo").innerHTML = resumoPaciente(paciente);

        await atualizarConteudoProntuario(id);
    } catch (erro) {
        $("prontuarioConteudo").innerHTML = "";
        $("prontuarioErro").textContent =
            erro.message || "Não foi possível carregar o prontuário.";
        $("prontuarioErro").hidden = false;
    }
}

function preencherCampoBooleano(id, valor) {
    $(id).value = valor == null ? "" : String(valor);
}

async function abrirAnamnese(id) {
    if (!modalAnamnese) {
        console.warn("[pacientes.js] modal da anamnese não encontrado no index.html.");
        return;
    }

    idPacienteAnamnese = id;

    $("anaErro").textContent = "";
    $("anaOk").textContent = "";
    $("anaData").textContent = "—";
    $("anaForm").reset();

    try {
        const resposta = await fetch(`${API}/${id}/anamnese`);

        if (resposta.ok) {
            const anamnese = await resposta.json();

            $("anaAlergias").value = anamnese.alergias ?? "";
            $("anaMedicacoes").value = anamnese.medicacoesContinuas ?? "";
            $("anaCirurgias").value = anamnese.cirurgiasPrevias ?? "";
            preencherCampoBooleano("anaGestante", anamnese.gestanteOuLactante);
            preencherCampoBooleano("anaIsotretinoina", anamnese.usoIsotretinoina);
            preencherCampoBooleano("anaQueloide", anamnese.historicoQueloide);
            $("anaDoencas").value = anamnese.doencasDePele ?? "";
            $("anaContraindicacoes").value =
                anamnese.contraindicacoesIdentificadas ?? "";
            $("anaData").textContent = formatarData(anamnese.dataPreenchimento) || "—";
        } else if (resposta.status !== 404) {
            const erro = await resposta.json().catch(() => ({}));
            throw new Error(erro.mensagem || "Falha ao carregar a anamnese.");
        }
    } catch (erro) {
        $("anaErro").textContent =
            erro.message || "Não foi possível carregar a anamnese.";
    }

    modalAnamnese.showModal();
}

function valorBooleanoOuNulo(id) {
    const valor = $(id).value;

    if (valor === "") return null;
    return valor === "true";
}

async function salvarAnamnese(evento) {
    evento.preventDefault();

    if (idPacienteAnamnese == null) return;

    $("anaErro").textContent = "";
    $("anaOk").textContent = "";

    const dados = {
        alergias: $("anaAlergias").value.trim(),
        medicacoesContinuas: $("anaMedicacoes").value.trim(),
        cirurgiasPrevias: $("anaCirurgias").value.trim(),
        gestanteOuLactante: valorBooleanoOuNulo("anaGestante"),
        usoIsotretinoina: valorBooleanoOuNulo("anaIsotretinoina"),
        historicoQueloide: valorBooleanoOuNulo("anaQueloide"),
        doencasDePele: $("anaDoencas").value.trim(),
        contraindicacoesIdentificadas: $("anaContraindicacoes").value.trim()
    };

    try {
        const resposta = await fetch(
            `${API}/${idPacienteAnamnese}/anamnese`,
            {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dados)
            }
        );

        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({}));
            throw new Error(erro.mensagem || "Falha ao salvar a anamnese.");
        }

        const salva = await resposta.json();

        $("anaData").textContent =
            formatarData(salva.dataPreenchimento) || "—";
        $("anaOk").textContent = "Anamnese salva.";

        modalAnamnese.close();

        // Atualiza a seção da anamnese no prontuário que continua aberto.
        await atualizarConteudoProntuario(idPacienteAnamnese);
    } catch (erro) {
        $("anaErro").textContent =
            erro.message || "Não foi possível salvar a anamnese.";
    }
}

async function salvarPaciente(evento) {
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
        const resposta = await fetch(id ? `${API}/${id}` : API, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        });

        if (resposta.ok) {
            modal.close();
            await carregar();
        } else {
            const erro = await resposta.json().catch(() => ({}));
            $("erro").textContent = erro.mensagem || "Erro ao salvar.";
        }
    } catch {
        $("erro").textContent = "Não foi possível conectar ao servidor.";
    }
}

// Liga a função de fechar nos botões de cancelar que JÁ existem no modal da
// anamnese (independentemente do id) e só cria um botão novo se não houver
// nenhum — assim não aparecem dois botões "Cancelar".
function ligarBotoesCancelarAnamnese() {
    if (!modalAnamnese) return;

    const formulario = $("anaForm");
    const botaoSalvar = formulario
        ? formulario.querySelector('button[type="submit"]')
        : null;

    let encontrouCancelar = false;

    modalAnamnese.querySelectorAll("button").forEach((botao) => {
        const texto = (botao.textContent || "").trim().toLowerCase();

        // Não mexe no botão que salva a anamnese.
        if (botao === botaoSalvar || texto === "salvar" || texto === "gravar") {
            return;
        }

        encontrouCancelar = true;
        botao.type = "button"; // evita submit acidental do formulário
        botao.addEventListener("click", () => modalAnamnese.close());
    });

    if (encontrouCancelar) return;

    // Nenhum botão de cancelar existe no HTML: cria um antes do Salvar.
    const botao = document.createElement("button");
    botao.type = "button";
    botao.id = "btnCancelarAnamnese";
    botao.textContent = "Cancelar";
    botao.addEventListener("click", () => modalAnamnese.close());

    if (formulario) {
        if (botaoSalvar) {
            botaoSalvar.parentNode.insertBefore(botao, botaoSalvar);
        } else {
            formulario.appendChild(botao);
        }
    } else {
        modalAnamnese.appendChild(botao);
    }
}

ouvir("form", "submit", salvarPaciente);
ouvir("btnNovo", "click", abrirNovo);
ouvir("btnCancelar", "click", () => modal.close());
ouvir("btnFecharProntuario", "click", () => modalProntuario.close());
ouvir("anaForm", "submit", salvarAnamnese);
ouvir("btnFecharAnamnese", "click", () => modalAnamnese.close());
ouvir("busca", "input", carregar);
ouvir("filtroStatus", "change", carregar);

ligarBotoesCancelarAnamnese();

carregar();