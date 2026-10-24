const $ = (id) => document.getElementById(id);
const API_SALAS = "/api/salas";
const API_EQUIPAMENTOS = "/api/equipamentos";

let ocupado = false;
let versaoPesquisaSalas = 0;
let versaoPesquisaEquipamentos = 0;

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

/* -------------------- ABAS -------------------- */
function alternarTab(tab) {
    if (ocupado) return;

    const mostrarSalas = tab === "salas";

    $("tabSalas").hidden = !mostrarSalas;
    $("tabEquipamentos").hidden = mostrarSalas;

    $("tabSalas").classList.toggle("ativa", mostrarSalas);
    $("tabEquipamentos").classList.toggle("ativa", !mostrarSalas);

    $("tabBtnSalas").classList.toggle("ativa", mostrarSalas);
    $("tabBtnEquipamentos").classList.toggle("ativa", !mostrarSalas);

    if (mostrarSalas) {
        carregarSalas();
    } else {
        carregarEquipamentos();
    }
}

/* -------------------- SALAS: LISTAGEM -------------------- */

async function carregarSalas() {
    const minhaVersao = ++versaoPesquisaSalas;
    const params = new URLSearchParams();

    const busca = $("buscaSala").value.trim();
    const status = $("filtroStatusSala").value;

    if (busca) params.set("busca", busca);
    if (status) params.set("status", status);

    try {
        const salas = await consultarLista(`${API_SALAS}?${params}`);

        if (minhaVersao !== versaoPesquisaSalas) return;

        $("tabelaSalas").innerHTML =
            salas.map(linhaSala).join("") ||
            `<tr><td colspan="4">Nenhuma sala encontrada.</td></tr>`;
    } catch (erro) {
        if (minhaVersao !== versaoPesquisaSalas) return;

        $("tabelaSalas").innerHTML = `
            <tr>
                <td colspan="4">
                    Não foi possível carregar as salas:
                    ${esc(mensagemErro(erro))}
                </td>
            </tr>`;
    }
}

function linhaSala(sala) {
    const acaoStatus = sala.status !== "Inativa"
        ? `<button
                class="perigo"
                onclick="mudarStatusSala(${sala.id}, 'Inativa')">
                Inativar
           </button>`
        : `<button onclick="mudarStatusSala(${sala.id}, 'Ativa')">
                Ativar
           </button>`;

    return `
        <tr>
            <td><strong>${esc(sala.nome)}</strong></td>
            <td>${esc(sala.descricao || "-")}</td>
            <td>
                <span class="badge ${esc(sala.status)}">
                    ${esc(sala.status)}
                </span>
            </td>
            <td>
                <button onclick="editarSala(${sala.id})">
                    Editar
                </button>
                ${acaoStatus}
            </td>
        </tr>`;
}

/* -------------------- SALAS: FORMULÁRIO -------------------- */

function abrirNovaSala() {
    if (ocupado) return;

    $("formSala").reset();
    $("salaId").value = "";
    $("erroSala").textContent = "";
    $("tituloModalSala").textContent = "Nova Sala";

    if (!$("modalSala").open) {
        $("modalSala").showModal();
    }
}

async function editarSala(id) {
    await executarOperacao(async () => {
        const sala = await consultarRegistro(`${API_SALAS}/${id}`);

        $("formSala").reset();
        $("salaId").value = sala.id;
        $("salaNome").value = sala.nome;
        $("salaDescricao").value = sala.descricao || "";
        $("salaStatus").value = sala.status;
        $("erroSala").textContent = "";
        $("tituloModalSala").textContent = "Editar Sala";

        if (!$("modalSala").open) {
            $("modalSala").showModal();
        }
    });
}

async function mudarStatusSala(id, status) {
    if (ocupado) return;

    if (
        status === "Inativa" &&
        !confirm("Deseja realmente inativar esta sala?")
    ) {
        return;
    }

    await executarOperacao(async () => {
        await requisicao(
            `${API_SALAS}/${id}/status?status=${encodeURIComponent(status)}`,
            { method: "PATCH" },
            false
        );

        await carregarSalas();
    });
}

$("formSala").addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (ocupado) return;

    $("erroSala").textContent = "";

    const id = $("salaId").value;

    const payload = {
        nome: $("salaNome").value.trim(),
        descricao: $("salaDescricao").value.trim() || null,
        status: $("salaStatus").value
    };

    await executarOperacao(async () => {
        await requisicao(
            id ? `${API_SALAS}/${id}` : API_SALAS,
            {
                method: id ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            },
            false
        );

        $("modalSala").close();
        await carregarSalas();
    }, "erroSala");
});

/* -------------------- EQUIPAMENTOS: SALAS DISPONÍVEIS -------------------- */

async function atualizarSelectSalas(selecionado) {
    const salas = await consultarLista(API_SALAS);

    const mesmoId = (id) =>
        selecionado != null &&
        String(id) === String(selecionado);

    $("eqIdSala").innerHTML =
        `<option value="">Nenhuma / Portátil</option>` +
        salas
            .filter(
                (sala) =>
                    sala.status !== "Inativa" ||
                    mesmoId(sala.id)
            )
            .map((sala) => `
                <option
                    value="${esc(sala.id)}"
                    ${mesmoId(sala.id) ? "selected" : ""}>
                    ${esc(sala.nome)}
                </option>
            `)
            .join("");
}

/* -------------------- EQUIPAMENTOS: LISTAGEM -------------------- */

async function carregarEquipamentos() {
    const minhaVersao = ++versaoPesquisaEquipamentos;
    const params = new URLSearchParams();

    const busca = $("buscaEquipamento").value.trim();
    const status = $("filtroStatusEquipamento").value;

    if (busca) params.set("busca", busca);
    if (status) params.set("status", status);

    try {
        const [salas, equipamentos] = await Promise.all([
            consultarLista(API_SALAS),
            consultarLista(`${API_EQUIPAMENTOS}?${params}`)
        ]);

        if (minhaVersao !== versaoPesquisaEquipamentos) return;

        const nomesSalas = new Map(
            salas.map((sala) => [String(sala.id), sala.nome])
        );

        $("tabelaEquipamentos").innerHTML =
            equipamentos
                .map((equipamento) =>
                    linhaEquipamento(equipamento, nomesSalas)
                )
                .join("") ||
            `<tr><td colspan="7">Nenhum equipamento encontrado.</td></tr>`;
    } catch (erro) {
        if (minhaVersao !== versaoPesquisaEquipamentos) return;

        $("tabelaEquipamentos").innerHTML = `
            <tr>
                <td colspan="7">
                    Não foi possível carregar os equipamentos:
                    ${esc(mensagemErro(erro))}
                </td>
            </tr>`;
    }
}

function linhaEquipamento(equipamento, nomesSalas) {
    const salaNome =
        nomesSalas.get(String(equipamento.idSala)) || "-";

    const acaoStatus = equipamento.status !== "Inativo"
        ? `<button
                class="perigo"
                onclick="mudarStatusEquipamento(${equipamento.id}, 'Inativo')">
                Inativar
           </button>`
        : `<button
                onclick="mudarStatusEquipamento(${equipamento.id}, 'Disponível')">
                Ativar
           </button>`;

    return `
        <tr>
            <td>
                <strong>${esc(equipamento.nomeModelo)}</strong>
            </td>
            <td>${esc(equipamento.tipo)}</td>
            <td>${esc(equipamento.numeroSerie || "-")}</td>
            <td>${esc(salaNome)}</td>
            <td>${esc(equipamento.proximaManutencao || "-")}</td>
            <td>
                <span class="badge ${esc(equipamento.status)}">
                    ${esc(equipamento.status)}
                </span>
            </td>
            <td>
                <button onclick="editarEquipamento(${equipamento.id})">
                    Editar
                </button>
                ${acaoStatus}
            </td>
        </tr>`;
}

/* -------------------- EQUIPAMENTOS: FORMULÁRIO -------------------- */

async function abrirNovoEquipamento() {
    await executarOperacao(async () => {
        $("formEquipamento").reset();
        $("equipamentoId").value = "";
        $("erroEquipamento").textContent = "";
        $("tituloModalEquipamento").textContent = "Novo Equipamento";

        await atualizarSelectSalas(null);

        if (!$("modalEquipamento").open) {
            $("modalEquipamento").showModal();
        }
    });
}

async function editarEquipamento(id) {
    await executarOperacao(async () => {
        const equipamento = await consultarRegistro(
            `${API_EQUIPAMENTOS}/${id}`
        );

        await atualizarSelectSalas(equipamento.idSala);

        $("equipamentoId").value = equipamento.id;
        $("eqNomeModelo").value = equipamento.nomeModelo;
        $("eqTipo").value = equipamento.tipo;
        $("eqNumeroSerie").value = equipamento.numeroSerie || "";
        $("eqStatus").value = equipamento.status;
        $("eqUltimaManutencao").value =
            equipamento.ultimaManutencao || "";
        $("eqProximaManutencao").value =
            equipamento.proximaManutencao || "";

        $("erroEquipamento").textContent = "";
        $("tituloModalEquipamento").textContent = "Editar Equipamento";

        if (!$("modalEquipamento").open) {
            $("modalEquipamento").showModal();
        }
    });
}

async function mudarStatusEquipamento(id, status) {
    if (ocupado) return;

    if (
        status === "Inativo" &&
        !confirm("Deseja realmente inativar este equipamento?")
    ) {
        return;
    }

    await executarOperacao(async () => {
        await requisicao(
            `${API_EQUIPAMENTOS}/${id}/status?status=${encodeURIComponent(status)}`,
            { method: "PATCH" },
            false
        );

        await carregarEquipamentos();
    });
}

$("formEquipamento").addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (ocupado) return;

    $("erroEquipamento").textContent = "";

    const id = $("equipamentoId").value;
    const salaSelecionada = $("eqIdSala").value;

    const payload = {
        nomeModelo: $("eqNomeModelo").value.trim(),
        tipo: $("eqTipo").value.trim(),
        numeroSerie: $("eqNumeroSerie").value.trim() || null,
        idSala: salaSelecionada ? Number(salaSelecionada) : null,
        status: $("eqStatus").value,
        ultimaManutencao: $("eqUltimaManutencao").value || null,
        proximaManutencao: $("eqProximaManutencao").value || null
    };

    await executarOperacao(async () => {
        await requisicao(
            id ? `${API_EQUIPAMENTOS}/${id}` : API_EQUIPAMENTOS,
            {
                method: id ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            },
            false
        );

        $("modalEquipamento").close();
        await carregarEquipamentos();
    }, "erroEquipamento");
});

/* -------------------- EVENTOS -------------------- */


["modalSala", "modalEquipamento"].forEach((id) => {
    $(id).addEventListener("cancel", (evento) => {
        if (ocupado) evento.preventDefault();
    });
});

$("buscaSala").addEventListener("input", carregarSalas);
$("filtroStatusSala").addEventListener("change", carregarSalas);

$("buscaEquipamento").addEventListener(
    "input",
    carregarEquipamentos
);

$("filtroStatusEquipamento").addEventListener(
    "change",
    carregarEquipamentos
);

carregarSalas();
