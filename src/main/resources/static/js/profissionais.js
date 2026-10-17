const API = "/api/profissionais";
const $ = (id) => document.getElementById(id);
const modal = $("modal");

const campos = [
    "nome",
    "cpf",
    "dataNascimento",
    "genero",
    "telefone",
    "email",
    "funcao",
    "tipoVinculo",
    "tipoConselho",
    "numeroConselho",
    "ufConselho",
    "percentualComissao",
    "dataAdmissao",
    "diasHorarioTrabalho"
];

let ocupado = false;
let versaoPesquisa = 0;
let procedimentosCarregados = false;

/* -------------------- UTILITÁRIOS -------------------- */

function esc(texto) {
    const entidades = {
        "&": "&amp;",
        "<​": "&lt;",
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
                ? "A conexão foi interrompida. Confira os dados no sistema "
                    + "antes de tentar salvar novamente."
                : "Não foi possível conectar ao servidor."
        );
    }

    // Verifica o status antes de processar a resposta.
    if (!resposta.ok) {
        let mensagem =
            `Não foi possível concluir a operação (HTTP ${resposta.status}).`;

        try {
            const texto = await resposta.text();

            if (texto) {
                try {
                    const erro = JSON.parse(texto);

                    if (
                        erro &&
                        typeof erro.mensagem === "string" &&
                        erro.mensagem.trim()
                    ) {
                        mensagem = erro.mensagem;
                    }
                } catch {
                    // Mantém a mensagem HTTP se o corpo não for JSON.
                }
            }
        } catch {
            // Mantém a mensagem HTTP se não for possível ler o corpo.
        }

        throw new Error(mensagem);
    }

    if (!esperaJson || resposta.status === 204) {
        return null;
    }

    try {
        return await resposta.json();
    } catch {
        throw new Error(
            "O servidor retornou uma resposta inválida."
        );
    }
}

async function consultarLista(url) {
    const dados = await requisicao(url);

    if (!Array.isArray(dados)) {
        throw new Error(
            "O servidor não retornou uma lista válida."
        );
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
        throw new Error(
            "O servidor não retornou um registro válido."
        );
    }

    return dados;
}

async function executarOperacao(acao, idErro = "erroPagina") {
    if (ocupado) return;

    ocupado = true;

    const estados = [
        ...document.querySelectorAll(
            "button, dialog input, dialog select, dialog textarea"
        )
    ].map((elemento) => ({
        elemento,
        desabilitado: elemento.disabled
    }));

    estados.forEach(({ elemento }) => {
        elemento.disabled = true;
    });

    const destinoErro = $(idErro);

    if (destinoErro) {
        destinoErro.textContent = "";
    }

    try {
        await acao();
    } catch (erro) {
        if (destinoErro) {
            destinoErro.textContent = mensagemErro(erro);
        }
    } finally {
        estados.forEach(({ elemento, desabilitado }) => {
            if (elemento.isConnected) {
                elemento.disabled = desabilitado;
            }
        });

        ocupado = false;
    }
}

/* -------------------- HABILITAÇÕES -------------------- */

function renderizarProcedimentos(procedimentos, selecionados) {
    const container = $("listaProcedimentos");
    const marcados = new Set(
        selecionados.map((id) => String(id))
    );

    const idsCatalogo = new Set(
        procedimentos.map((procedimento) =>
            String(procedimento.id)
        )
    );

    const faltantes = [...marcados].filter(
        (id) => !idsCatalogo.has(id)
    );

    /*
     * Não permite salvar uma lista incompleta e remover vínculos
     * silenciosamente caso o catálogo não contenha um item vinculado.
     */
    if (faltantes.length > 0) {
        throw new Error(
            "Não foi possível carregar todos os procedimentos já "
            + "vinculados a este profissional. Nenhuma habilitação "
            + "foi alterada."
        );
    }

    const fragmento = document.createDocumentFragment();

    if (procedimentos.length === 0) {
        const aviso = document.createElement("p");
        aviso.textContent = "Nenhum procedimento cadastrado.";
        fragmento.appendChild(aviso);
    } else {
        procedimentos.forEach((procedimento) => {
            const id = Number(procedimento.id);

            if (!Number.isSafeInteger(id) || id <= 0) {
                throw new Error(
                    "O servidor retornou um procedimento inválido."
                );
            }

            const label = document.createElement("label");
            const checkbox = document.createElement("input");
            const texto = document.createElement("span");

            checkbox.type = "checkbox";
            checkbox.name = "procedimentosIds";
            checkbox.value = String(id);
            checkbox.checked = marcados.has(String(id));

            /*
             * O conteúdo é inserido com textContent, não com innerHTML.
             * Procedimentos inativos continuam visíveis para que um
             * vínculo existente não seja removido sem o usuário decidir.
             */
            const status = procedimento.status;
            const complemento =
                status && status !== "Ativo"
                    ? ` (${status})`
                    : "";

            texto.textContent =
                `${procedimento.nome ?? `Procedimento ${id}`}${complemento}`;

            label.appendChild(checkbox);
            label.appendChild(texto);
            fragmento.appendChild(label);
        });
    }

    container.replaceChildren(fragmento);
    procedimentosCarregados = true;
}

function obterProcedimentosSelecionados() {
    if (!procedimentosCarregados) {
        throw new Error(
            "Os procedimentos não foram carregados. Reabra o formulário."
        );
    }

    return [
        ...document.querySelectorAll(
            '#listaProcedimentos input[name="procedimentosIds"]:checked'
        )
    ].map((checkbox) => {
        const id = Number(checkbox.value);

        if (!Number.isSafeInteger(id) || id <= 0) {
            throw new Error(
                "Existe um procedimento inválido na seleção."
            );
        }

        return id;
    });
}

/* -------------------- LISTAGEM -------------------- */

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
            '<tr><td colspan="7">Nenhum profissional encontrado.</td></tr>';
    } catch (erro) {
        if (minhaVersao !== versaoPesquisa) return;

        $("tabela").innerHTML = `
            <tr>
                <td colspan="7">
                    Não foi possível carregar os profissionais:
                    ${esc(mensagemErro(erro))}
                </td>
            </tr>`;
    }
}

function linha(p) {
    const conselho = p.numeroConselho
        ? `${esc(p.tipoConselho || "")} ${esc(p.numeroConselho)}`
            + `${p.ufConselho ? "/" + esc(p.ufConselho) : ""}`
        : "-";

    const id = Number(p.id);

    if (!Number.isSafeInteger(id) || id <= 0) {
        throw new Error(
            "O servidor retornou um profissional inválido."
        );
    }

    const acaoStatus = p.status === "Ativo"
        ? `<button
                class="perigo"
                onclick="alterarStatus(${id}, 'desabilitar')">
                Inativar
           </button>`
        : `<button onclick="alterarStatus(${id}, 'reativar')">
                Reativar
           </button>`;

    return `
        <tr>
            <td>${esc(p.nome)}</td>
            <td>${esc(p.cpf)}</td>
            <td>${esc(p.funcao)}</td>
            <td>${conselho}</td>
            <td>${esc(p.tipoVinculo)}</td>
            <td>
                <span class="badge ${esc(p.status)}">
                    ${esc(p.status)}
                </span>
            </td>
            <td>
                <button onclick="editar(${id})">Editar</button>
                ${acaoStatus}
            </td>
        </tr>`;
}

/* -------------------- NOVO E EDIÇÃO -------------------- */

async function abrirNovo() {
    await executarOperacao(async () => {
        procedimentosCarregados = false;

        /*
         * Consulta o catálogo sem restringir a Ativo.
         * Isso mantém a apresentação dos procedimentos consistente
         * com a edição e evita ocultar vínculos existentes.
         */
        const procedimentos =
            await consultarLista("/api/procedimentos");

        $("form").reset();
        $("id").value = "";
        $("erro").textContent = "";
        $("tituloForm").textContent = "Novo profissional";

        renderizarProcedimentos(procedimentos, []);

        if (!modal.open) {
            modal.showModal();
        }
    });
}

async function editar(id) {
    await executarOperacao(async () => {
        procedimentosCarregados = false;

        const [profissional, procedimentos, selecionados] =
            await Promise.all([
                consultarRegistro(`${API}/${id}`),
                consultarLista("/api/procedimentos"),
                consultarLista(`${API}/${id}/procedimentos`)
            ]);

        $("form").reset();
        $("id").value = profissional.id;

        campos.forEach((campo) => {
            $(campo).value = profissional[campo] ?? "";
        });

        $("erro").textContent = "";
        $("tituloForm").textContent = "Editar profissional";

        renderizarProcedimentos(
            procedimentos,
            selecionados
        );

        if (!modal.open) {
            modal.showModal();
        }
    });
}

/* -------------------- STATUS -------------------- */

async function alterarStatus(id, acao) {
    if (ocupado) return;

    if (!["desabilitar", "reativar"].includes(acao)) {
        $("erroPagina").textContent =
            "Ação de status inválida.";
        return;
    }

    if (
        acao === "desabilitar" &&
        !confirm("Inativar este profissional?")
    ) {
        return;
    }

    await executarOperacao(async () => {
        await requisicao(
            `${API}/${id}/${acao}`,
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

    campos.forEach((campo) => {
        const valor = $(campo).value.trim();

        dados[campo] = valor === ""
            ? null
            : campo === "percentualComissao"
                ? Number(valor)
                : valor;
    });

    try {
        dados.procedimentosIds =
            obterProcedimentosSelecionados();
    } catch (erro) {
        $("erro").textContent = mensagemErro(erro);
        return;
    }

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

        // Fecha somente depois da resposta HTTP de sucesso.
        modal.close();
        await carregar();
    }, "erro");
});

/* -------------------- EVENTOS -------------------- */

$("btnNovo").addEventListener("click", abrirNovo);

$("btnCancelar").addEventListener("click", () => {
    if (!ocupado) {
        modal.close();
    }
});

modal.addEventListener("cancel", (evento) => {
    if (ocupado) {
        evento.preventDefault();
    }
});

modal.addEventListener("close", () => {
    procedimentosCarregados = false;
});

$("busca").addEventListener("input", carregar);
$("filtroStatus").addEventListener("change", carregar);

carregar();