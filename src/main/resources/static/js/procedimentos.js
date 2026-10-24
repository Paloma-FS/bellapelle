const API = "/api/procedimentos";
const $ = (id) => document.getElementById(id);
const modal = $("modal");

const textos = [
    "nome",
    "categoria",
    "descricao",
    "contraindicacoes",
    "tipoEquipamentoRequerido"
];

const numeros = [
    "valor",
    "duracaoMinutos",
    "percentualComissao"
];

const booleanos = [
    "requerEquipamento",
    "injetavel"
];

let ocupado = false;
let versaoPesquisa = 0;

function esc(texto) {
    const entidades = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    };

    return String(texto ?? "").replace(
        /[&<>"']/g,
        (caractere) => entidades[caractere]
    );
}

function mensagemErro(erro) {
    return erro instanceof Error
        ? erro.message
        : "Não foi possível concluir a operação.";
}

const moeda = (valor) =>
    Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });

async function requisicao(url, opcoes = {}, esperaJson = true) {
    let resposta;

    try {
        resposta = await fetch(url, opcoes);
    } catch {
        const escrita =
            opcoes.method &&
            opcoes.method.toUpperCase() !== "GET";

        throw new Error(
            escrita
                ? "A conexão falhou. Não foi possível confirmar se a alteração foi gravada. Confira a listagem antes de tentar novamente."
                : "Não foi possível conectar ao servidor. Verifique a conexão e tente novamente."
        );
    }

    if (!resposta.ok) {
        const dados = await resposta.json().catch(() => null);

        const mensagem =
            typeof dados?.mensagem === "string" &&
            dados.mensagem.trim()
                ? dados.mensagem
                : `Não foi possível concluir a operação (HTTP ${resposta.status}).`;

        throw new Error(mensagem);
    }

    if (!esperaJson) return null;

    try {
        return await resposta.json();
    } catch {
        throw new Error("O servidor retornou uma resposta inválida.");
    }
}

async function consultarLista(url) {
    const dados = await requisicao(url);

    if (!Array.isArray(dados)) {
        throw new Error("O servidor não retornou uma lista válida.");
    }

    return dados;
}

async function consultarRegistro(url) {
    const dados = await requisicao(url);

    if (
        dados === null ||
        typeof dados !== "object" ||
        Array.isArray(dados) ||
        dados.id == null
    ) {
        throw new Error("O servidor não retornou um registro válido.");
    }

    return dados;
}

/*
 * Serializa abertura de edição, salvamento e alteração de status.
 * A pesquisa continua disponível e tem seu próprio controle de ordem.
 */
async function executarOperacao(acao, idErro = null) {
    if (ocupado) return;

    ocupado = true;

    const estados = [
        ...document.querySelectorAll(
            "button, dialog input, dialog select, dialog textarea"
        )
    ].map((controle) => {
        const estado = {
            controle,
            disabled: controle.disabled
        };

        controle.disabled = true;
        return estado;
    });

    try {
        await acao();
    } catch (erro) {
        if (idErro) {
            $(idErro).textContent = mensagemErro(erro);
        } else {
            alert(mensagemErro(erro));
        }
    } finally {
        estados.forEach(({ controle, disabled }) => {
            controle.disabled = disabled;
        });

        ocupado = false;
    }
}

/* -------------------- LISTAGEM E PESQUISA -------------------- */

async function carregar() {
    const minhaVersao = ++versaoPesquisa;
    const params = new URLSearchParams();

    const busca = $("busca").value.trim();
    const status = $("filtroStatus").value;

    if (busca) params.set("busca", busca);
    if (status) params.set("status", status);

    try {
        const lista = await consultarLista(`${API}?${params}`);

        if (minhaVersao !== versaoPesquisa) return;

        $("tabela").innerHTML =
            lista.map(linha).join("") ||
            `<tr><td colspan="7">Nenhum procedimento encontrado.</td></tr>`;
    } catch (erro) {
        if (minhaVersao !== versaoPesquisa) return;

        $("tabela").innerHTML = `
            <tr>
                <td colspan="7">
                    Não foi possível carregar os procedimentos:
                    ${esc(mensagemErro(erro))}
                </td>
            </tr>`;
    }
}

function linha(p) {
    const acao = p.status === "Ativo"
        ? `<button
                class="perigo"
                onclick="alterarStatus(${p.id}, 'Inativo')">
                Inativar
           </button>`
        : `<button onclick="alterarStatus(${p.id}, 'Ativo')">
                Reativar
           </button>`;

    return `
        <tr>
            <td>${esc(p.nome)}</td>
            <td>${esc(p.categoria || "-")}</td>
            <td>${moeda(p.valor)}</td>
            <td>${esc(p.duracaoMinutos)} min</td>
            <td>
                ${p.requerEquipamento
                    ? esc(p.tipoEquipamentoRequerido)
                    : "-"}
            </td>
            <td>
                <span class="badge ${esc(p.status)}">
                    ${esc(p.status)}
                </span>
            </td>
            <td>
                <button onclick="editar(${p.id})">Editar</button>
                ${acao}
            </td>
        </tr>`;
}

/* -------------------- NOVO E EDIÇÃO -------------------- */

function abrirNovo() {
    if (ocupado) return;

    $("form").reset();
    $("id").value = "";
    $("erro").textContent = "";
    $("tituloForm").textContent = "Novo procedimento";

    if (!modal.open) modal.showModal();
}

async function editar(id) {
    await executarOperacao(async () => {
        const p = await consultarRegistro(`${API}/${id}`);

        $("form").reset();
        $("id").value = p.id;

        textos.concat(numeros).forEach((campo) => {
            $(campo).value = p[campo] ?? "";
        });

        booleanos.forEach((campo) => {
            $(campo).value = String(Boolean(p[campo]));
        });

        $("erro").textContent = "";
        $("tituloForm").textContent = "Editar procedimento";

        if (!modal.open) modal.showModal();
    });
}

/* -------------------- STATUS -------------------- */

async function alterarStatus(id, status) {
    if (ocupado) return;

    if (
        status === "Inativo" &&
        !confirm("Inativar este procedimento?")
    ) {
        return;
    }

    await executarOperacao(async () => {
        await requisicao(
            `${API}/${id}/status?status=${encodeURIComponent(status)}`,
            { method: "PATCH" },
            false
        );

        await carregar();
    });
}

/* -------------------- SALVAMENTO -------------------- */

$("form").addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (ocupado) return;

    $("erro").textContent = "";

    const dados = {};

    textos.forEach((campo) => {
        const valor = $(campo).value.trim();
        dados[campo] = valor === "" ? null : valor;
    });

    numeros.forEach((campo) => {
        const valor = $(campo).value.trim();
        dados[campo] = valor === "" ? null : Number(valor);
    });

    booleanos.forEach((campo) => {
        dados[campo] = $(campo).value === "true";
    });

    const id = $("id").value;

    await executarOperacao(async () => {
        await requisicao(
            id ? `${API}/${id}` : API,
            {
                method: id ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(dados)
            },
            false
        );

        // Só fecha depois de uma resposta HTTP de sucesso.
        modal.close();
        await carregar();
    }, "erro");
});

/* -------------------- EVENTOS -------------------- */

$("btnNovo").addEventListener("click", abrirNovo);

$("btnCancelar").addEventListener("click", () => {
    if (!ocupado) modal.close();
});

modal.addEventListener("cancel", (evento) => {
    if (ocupado) evento.preventDefault();
});

$("busca").addEventListener("input", carregar);
$("filtroStatus").addEventListener("change", carregar);

carregar();
