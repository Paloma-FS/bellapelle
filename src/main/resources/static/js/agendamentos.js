const API = "/api/agendamentos";
const $ = (id) => document.getElementById(id);
const modal = $("modal");
const modalCancel = $("modalCancel");

let procs = [];
let idCancelando = null;
let tokenHorarios = 0;
let versaoLista = 0;
let consultandoHorarios = false;
let salvando = false;
let alterandoStatus = false;

function esc(t) {
    const entidades = {
        "&": "&amp;",
        "<​": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    };

    return String(t ?? "").replace(
        /[&<>"']/g,
        (c) => entidades[c]
    );
}

const pad = (n) => String(n).padStart(2, "0");

function hojeLocal() {
    const d = new Date();
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

const fmt = (d) => new Date(d).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });

// Lança erro se a consulta falhar (nunca devolve lista vazia "de mentira")
async function lista(url) {
    const r = await fetch(url);
    if (!r.ok) {
        const e = await r.json().catch(() => ({}));
        throw new Error(e.mensagem || `Falha ao consultar o servidor (${r.status}).`);
    }
    return r.json();
}

/* ---- avisos ---- */
function toast(msg, tipo) {
    const t = $("toast");
    t.textContent = msg;
    t.className = "toast " + (tipo === "erro" ? "toast-erro" : "toast-ok");
    t.hidden = false;
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => (t.hidden = true), 4000);
}

function mostrarAviso(el, msg) {
    el.textContent = msg || "";
    el.hidden = !msg;
}

/* ---- selects ---- */
function preencher(sel, itens, vazio) {
    const atual = $(sel).value;
    $(sel).innerHTML = (vazio ? `<option value="">${vazio}</option>` : "") +
        itens.map(x => `<option value="${x.id}">${esc(x.nome ?? x.nomeModelo ?? x.id)}</option>`).join("");
    if (atual && [...$(sel).options].some(o => o.value === atual)) $(sel).value = atual;
}

async function carregarOpcoes() {
    const [pac, prof, salas, equips, pr] = await Promise.all([
        lista("/api/pacientes"), lista("/api/profissionais"), lista("/api/salas"),
        lista("/api/equipamentos"), lista("/api/procedimentos?status=Ativo")
    ]);
    procs = pr;
    preencher("idPaciente", pac);
    preencher("idProfissional", prof.filter(p => !p.status || p.status === "Ativo"));
    preencher("idSala", salas.filter(s => !s.status || s.status === "Ativa"));
    preencher("idEquipamento", equips.filter(e => !e.status || e.status === "Disponível"), "(nenhum)");
    preencher("idProcedimento", pr);
}

/* ---- tabela ---- */
async function carregar() {
    const minhaVersao = ++versaoLista;
    const p = new URLSearchParams();
    if ($("filtroData").value) p.set("data", $("filtroData").value);
    if ($("filtroStatus").value) p.set("status", $("filtroStatus").value);
    try {
        const ags = await lista(`${API}?${p}`);
        if (minhaVersao !== versaoLista) return; // resposta antiga
        $("tabela").innerHTML = ags.map(linha).join("") || `<tr><td colspan="9">Nenhum agendamento encontrado.</td></tr>`;
    } catch (e) {
        if (minhaVersao !== versaoLista) return;
        $("tabela").innerHTML = `<tr><td colspan="9">Não foi possível carregar os agendamentos: ${esc(e.message)}</td></tr>`;
    }
}

function linha(a) {
    const aberto = a.status === "Agendado" || a.status === "Confirmado";
    const acoes = aberto ? `
        <div class="acoes-linha">
            <button class="btn-sm" onclick="editar(${a.id})">Editar</button>
            <details class="menu">
                <summary class="btn-sm">Mais ▾</summary>
                <div class="menu-lista">
                    ${a.status === "Agendado" ? `<button onclick="mudar(${a.id}, 'Confirmado')">Confirmar</button>` : ""}
                    <button onclick="mudar(${a.id}, 'Realizado')">Marcar realizado</button>
                    <button onclick="mudar(${a.id}, 'Faltou')">Marcar falta</button>
                    <button class="menu-perigo" onclick="abrirCancelar(${a.id})">Cancelar…</button>
                </div>
            </details>
        </div>` : (
            a.status === "Realizado"
                ? `<button
                       class="btn-sm"
                       onclick="abrirAtendimento(${a.id})">
                       Prontuário
                   </button>`
                : ""
        );
    return `<tr><td>${fmt(a.dataHora)}</td><td>${esc(a.nomePaciente)}</td><td>${esc(a.nomeProcedimento)}</td>
        <td>${esc(a.nomeProfissional)}</td><td>${esc(a.nomeSala)}</td><td>${esc(a.nomeEquipamento || "-")}</td>
        <td>${a.duracaoMinutos} min</td>
        <td>${esc(a.status)}${a.tipoCancelamento ? `<br><small>${esc(a.tipoCancelamento)}</small>` : ""}</td>
        <td>${acoes}</td></tr>`;
}

function fecharMenus() {
    document.querySelectorAll("details.menu").forEach(d => (d.open = false));
}

async function mudar(id, status, tipo) {
    fecharMenus();

    if (status === "Realizado") {
        await abrirAtendimento(id);
        return false;
    }

    if (alterandoStatus) return false;
    alterandoStatus = true;
    const q = new URLSearchParams({ status });
    if (tipo) q.set("tipoCancelamento", tipo);
    try {
        const r = await fetch(`${API}/${id}/status?${q}`, { method: "PATCH" });
        if (!r.ok) {
            const e = await r.json().catch(() => ({}));
            toast(e.mensagem || "Erro ao alterar status.", "erro");
            return false;
        }
    } catch (e) {
        toast("Não foi possível conectar ao servidor.", "erro");
        return false;
    } finally {
        alterandoStatus = false;
    }
    toast(`Agendamento marcado como ${status}.`);
    carregar();
    return true;
}

/* ---- cancelamento ---- */
async function abrirCancelar(id) {
    fecharMenus();
    idCancelando = id;
    mostrarAviso($("erroCancel"), "");
    try {
        const a = await lista(`${API}/${id}`);
        $("cancelResumo").textContent = `${a.nomePaciente} · ${a.nomeProcedimento} · ${fmt(a.dataHora)}`;
    } catch (e) {
        toast(e.message, "erro");
        return;
    }
    document.querySelector('input[name="tipoCancel"][value="Sem custo"]').checked = true;
    modalCancel.showModal();
}

$("formCancel").addEventListener("submit", async (e) => {
    e.preventDefault();
    const tipo = document.querySelector('input[name="tipoCancel"]:checked').value;
    const ok = await mudar(idCancelando, "Cancelado", tipo);
    if (ok) modalCancel.close();
});
$("btnCancelFechar").addEventListener("click", () => modalCancel.close());

/* ---- horários livres (calculados pelo servidor) ---- */
function selecionarHorario(valor) {
    $("dataHora").value = valor;
    $("horarioEscolhido").textContent = valor ? "· escolhido: " + valor.substring(11, 16) : "";
    document.querySelectorAll("#horarios button").forEach(b =>
        b.classList.toggle("sel", b.dataset.valor === valor));
}

// "preservar": horário que deve continuar selecionado, mas somente se ainda estiver livre.
async function atualizarHorarios(preservar = $("dataHora").value) {
    // Invalida qualquer consulta anterior e limpa a seleção antes de qualquer verificação.
    const meuToken = ++tokenHorarios;
    consultandoHorarios = false;
    selecionarHorario("");

    const grid = $("horarios");
    const data = $("data").value;
    const pac = $("idPaciente").value;
    const proc = $("idProcedimento").value;
    const prof = $("idProfissional").value;
    const sala = $("idSala").value;
    const equip = $("idEquipamento").value;
    const dur = $("duracao").value;
    const editando = $("id").value;

    if (!data || !pac || !proc || !prof || !sala) {
        grid.innerHTML = `<p class="dica">Escolha paciente, procedimento, profissional, sala e data para ver os horários do dia.</p>`;
        return;
    }

    const q = new URLSearchParams({ data, idPaciente: pac, idProcedimento: proc, idProfissional: prof, idSala: sala });
    if (equip) q.set("idEquipamento", equip);
    if (dur) q.set("duracaoMinutos", dur);
    if (editando) q.set("ignorarId", editando);

    consultandoHorarios = true;
    grid.innerHTML = `<p class="dica">Consultando horários…</p>`;

    let slots;
    try {
        slots = await lista(`${API}/horarios-livres?${q}`);
    } catch (e) {
        if (meuToken !== tokenHorarios) return;
        consultandoHorarios = false;
        grid.innerHTML = `<p class="dica">Não foi possível carregar os horários: ${esc(e.message)}</p>`;
        return;
    }
    if (meuToken !== tokenHorarios) return; // resposta antiga
    consultandoHorarios = false;

    if (!slots.length) {
        grid.innerHTML = `<p class="dica">A duração do procedimento não cabe no expediente (início das 09:00 às 18:30, término até as 19:00).</p>`;
        return;
    }

    grid.innerHTML = slots.map(s => {
        const valor = `${data}T${s.hora}`;
        return `<button type="button" class="slot ${s.livre ? "livre" : "ocupado"}" data-valor="${valor}"
                ${s.livre ? "" : `disabled title="${esc(s.motivo)}"`}>${s.hora}</button>`;
    }).join("");

    if (!slots.some(s => s.livre)) {
        const motivos = [...new Set(slots.map(s => s.motivo).filter(m => m && m !== "Horário já passou"))].slice(0, 3);
        grid.innerHTML += `<p class="dica">Nenhum horário livre.${motivos.length ? " Motivo: " + esc(motivos.join(" | ")) : ""}</p>`;
    }

    grid.querySelectorAll("button.livre").forEach(b =>
        b.addEventListener("click", () => selecionarHorario(b.dataset.valor)));

    // Só mantém o horário anterior se ele estiver entre os livres retornados.
    const ainda = preservar ? grid.querySelector(`button.livre[data-valor="${preservar}"]`) : null;
    selecionarHorario(ainda ? preservar : "");
}

["idPaciente", "idProfissional", "idSala", "idEquipamento", "data"].forEach(id =>
    $(id).addEventListener("change", () => atualizarHorarios()));
$("duracao").addEventListener("input", () => atualizarHorarios());

$("idProcedimento").addEventListener("change", () => {
    const p = procs.find(x => String(x.id) === $("idProcedimento").value);
    if (p && !$("id").value) $("duracao").value = p.duracaoMinutos;
    atualizarHorarios();
});

/* ---- novo / editar ---- */
async function abrirNovo() {
    $("form").reset();
    $("id").value = "";
    $("dataHora").value = "";
    mostrarAviso($("erro"), "");
    $("tituloForm").textContent = "Novo agendamento";
    try {
        await carregarOpcoes();
    } catch (e) {
        toast("Não foi possível carregar as opções: " + e.message, "erro");
        return;
    }
    const p = procs.find(x => String(x.id) === $("idProcedimento").value);
    if (p) $("duracao").value = p.duracaoMinutos;
    $("data").value = $("filtroData").value || hojeLocal();
    await atualizarHorarios("");
    modal.showModal();
}

async function editar(id) {
    fecharMenus();
    let a;
    try {
        a = await lista(`${API}/${id}`);
        await carregarOpcoes();
    } catch (e) {
        toast("Não foi possível abrir o agendamento: " + e.message, "erro");
        return;
    }
    $("id").value = a.id;
    $("idPaciente").value = a.idPaciente;
    $("idProcedimento").value = a.idProcedimento;
    $("idProfissional").value = a.idProfissional;
    $("idSala").value = a.idSala;
    $("idEquipamento").value = a.idEquipamento ?? "";
    $("duracao").value = a.duracaoMinutos;
    const dh = String(a.dataHora).substring(0, 16);
    $("data").value = dh.substring(0, 10);
    mostrarAviso($("erro"), "");
    $("tituloForm").textContent = "Editar agendamento";
    // O horário original só é recolocado se o servidor o devolver como livre.
    await atualizarHorarios(dh);
    modal.showModal();
}

$("form").addEventListener("submit", async (e) => {
    e.preventDefault();
    if (salvando) return;
    mostrarAviso($("erro"), "");
    if (consultandoHorarios) {
        mostrarAviso($("erro"), "Aguarde a consulta dos horários terminar.");
        return;
    }
    if (!$("dataHora").value) {
        mostrarAviso($("erro"), "Escolha um horário livre na grade.");
        return;
    }
    const id = $("id").value;
    const dados = {
        idPaciente: Number($("idPaciente").value),
        idProcedimento: Number($("idProcedimento").value),
        idProfissional: Number($("idProfissional").value),
        idSala: Number($("idSala").value),
        idEquipamento: $("idEquipamento").value ? Number($("idEquipamento").value) : null,
        dataHora: $("dataHora").value + ":00",
        duracaoMinutos: $("duracao").value ? Number($("duracao").value) : null
    };

    const botao = $("form").querySelector('button[type="submit"]');
    salvando = true;
    botao.disabled = true;
    try {
        const r = await fetch(id ? `${API}/${id}` : API, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        });
        if (r.ok) {
            modal.close();
            toast(id ? "Agendamento atualizado." : "Agendamento criado.");
            carregar();
        } else {
            const err = await r.json().catch(() => ({}));
            mostrarAviso($("erro"), err.mensagem || "Erro ao salvar.");
            atualizarHorarios();
        }
    } catch (e2) {
        mostrarAviso($("erro"), "Não foi possível conectar ao servidor.");
    } finally {
        salvando = false;
        botao.disabled = false;
    }
});

$("btnNovo").addEventListener("click", abrirNovo);
$("btnCancelar").addEventListener("click", () => modal.close());
$("filtroData").addEventListener("change", carregar);
$("filtroStatus").addEventListener("change", carregar);
document.addEventListener("click", (e) => { if (!e.target.closest("details.menu")) fecharMenus(); });
carregar();

/* ---- prontuário / realização do atendimento ---- */

const modalAtendimento = $("modalAtendimento");

let idAtendimentoAtual = null;
let atendimentoInjetavel = false;
let atendimentoSomenteLeitura = false;
let salvandoAtendimento = false;
let abrindoAtendimento = false;

function formatarDataAtendimento(valor) {
    if (!valor) return "-";

    const partes = String(valor).substring(0, 10).split("-");

    return partes.length === 3
        ? `${partes[2]}/${partes[1]}/${partes[0]}`
        : String(valor);
}

function montarHistoricoAtendimento(historico) {
    const container = $("historicoAtendimento");
    container.replaceChildren();

    if (!historico.length) {
        const aviso = document.createElement("p");
        aviso.className = "dica";
        aviso.textContent = "Nenhuma sessão registrada neste prontuário.";
        container.appendChild(aviso);
        return;
    }

    historico.forEach((sessao) => {
        const item = document.createElement("details");

        const titulo = document.createElement("summary");
        titulo.textContent =
            `${formatarDataAtendimento(sessao.data)} · ` +
            `${sessao.nomeProcedimento} · ${sessao.nomeProfissional}`;

        item.appendChild(titulo);

        function adicionarTexto(rotulo, valor) {
            if (valor == null || String(valor).trim() === "") return;

            const paragrafo = document.createElement("p");
            paragrafo.style.whiteSpace = "pre-wrap";

            const destaque = document.createElement("strong");
            destaque.textContent = `${rotulo}: `;

            paragrafo.appendChild(destaque);
            paragrafo.appendChild(
                document.createTextNode(String(valor))
            );

            item.appendChild(paragrafo);
        }

        adicionarTexto("Anotações", sessao.descricaoEvolucao);
        adicionarTexto("Intercorrências", sessao.intercorrencias);
        adicionarTexto("Fabricante", sessao.fabricante);
        adicionarTexto("Lote", sessao.numeroLote);

        if (sessao.validadeLote) {
            adicionarTexto(
                "Validade",
                formatarDataAtendimento(sessao.validadeLote)
            );
        }

        container.appendChild(item);
    });
}

async function abrirAtendimento(id) {
    fecharMenus();

    if (
        abrindoAtendimento ||
        salvandoAtendimento ||
        salvando ||
        alterandoStatus ||
        modalAtendimento.open
    ) {
        return;
    }

    abrindoAtendimento = true;

    try {
        const contexto = await lista(`${API}/${id}/atendimento`);

        if (
            !contexto ||
            !contexto.agendamento ||
            !Array.isArray(contexto.historico) ||
            typeof contexto.injetavel !== "boolean"
        ) {
            throw new Error(
                "O servidor retornou dados inválidos do prontuário."
            );
        }

        const a = contexto.agendamento;
        const podeRealizar =
            a.status === "Agendado" || a.status === "Confirmado";

        $("formAtendimento").reset();

        idAtendimentoAtual = a.id;
        atendimentoInjetavel = contexto.injetavel;
        atendimentoSomenteLeitura = !podeRealizar;

        $("tituloAtendimento").textContent = podeRealizar
            ? "Prontuário — realizar atendimento"
            : "Prontuário — consulta";

        $("atendimentoResumo").textContent =
            `${a.nomePaciente} · ${a.nomeProcedimento} · ` +
            `${a.nomeProfissional} · ${fmt(a.dataHora)}`;

        montarHistoricoAtendimento(contexto.historico);

        $("camposNovoAtendimento").hidden =
            atendimentoSomenteLeitura;

        $("descricaoEvolucaoAtendimento").disabled =
            atendimentoSomenteLeitura;

        $("descricaoEvolucaoAtendimento").required =
            !atendimentoSomenteLeitura;

        const pedirInjetavel =
            atendimentoInjetavel && !atendimentoSomenteLeitura;

        const grupo = $("dadosInjetavelAtendimento");
        grupo.hidden = !pedirInjetavel;
        grupo.disabled = !pedirInjetavel;

        [
            "fabricanteAtendimento",
            "numeroLoteAtendimento",
            "validadeLoteAtendimento"
        ].forEach((campo) => {
            $(campo).required = pedirInjetavel;
        });

        $("btnSalvarAtendimento").hidden =
            atendimentoSomenteLeitura;

        mostrarAviso($("erroAtendimento"), "");

        modalAtendimento.showModal();
    } catch (erro) {
        toast(
            erro.message || "Não foi possível abrir o prontuário.",
            "erro"
        );
    } finally {
        abrindoAtendimento = false;
    }
}

$("formAtendimento").addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (
        salvandoAtendimento ||
        atendimentoSomenteLeitura ||
        idAtendimentoAtual == null
    ) {
        return;
    }

    mostrarAviso($("erroAtendimento"), "");

    const descricaoEvolucao =
        $("descricaoEvolucaoAtendimento").value.trim();

    const fabricante =
        $("fabricanteAtendimento").value.trim();

    const numeroLote =
        $("numeroLoteAtendimento").value.trim();

    const validadeLote =
        $("validadeLoteAtendimento").value;

    if (!descricaoEvolucao) {
        mostrarAviso(
            $("erroAtendimento"),
            "Informe as anotações da sessão."
        );
        return;
    }

    if (
        atendimentoInjetavel &&
        (!fabricante || !numeroLote || !validadeLote)
    ) {
        mostrarAviso(
            $("erroAtendimento"),
            "Informe fabricante, lote e validade do injetável."
        );
        return;
    }

    const dados = {
        descricaoEvolucao,
        fabricante: atendimentoInjetavel ? fabricante : null,
        numeroLote: atendimentoInjetavel ? numeroLote : null,
        validadeLote: atendimentoInjetavel ? validadeLote : null
    };

    const id = idAtendimentoAtual;

    salvandoAtendimento = true;

    const controles = [
        ...$("formAtendimento").querySelectorAll(
            "input, textarea, button"
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
        let resposta;

        try {
            resposta = await fetch(`${API}/${id}/atendimento`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(dados)
            });
        } catch {
            throw new Error(
                "A conexão falhou. Não foi possível confirmar se o " +
                "atendimento foi gravado. Feche esta janela e confira " +
                "o status e o prontuário antes de tentar novamente."
            );
        }

        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => null);

            throw new Error(
                typeof erro?.mensagem === "string"
                    ? erro.mensagem
                    : `Não foi possível salvar o atendimento ` +
                      `(HTTP ${resposta.status}).`
            );
        }

        // O endpoint retorna HTTP 204, sem corpo JSON.
        modalAtendimento.close();
        idAtendimentoAtual = null;

        toast("Atendimento salvo no prontuário e marcado como realizado.");
        await carregar();
    } catch (erro) {
        mostrarAviso(
            $("erroAtendimento"),
            erro.message || "Não foi possível salvar o atendimento."
        );
    } finally {
        controles.forEach(({ controle, disabled }) => {
            controle.disabled = disabled;
        });

        salvandoAtendimento = false;
    }
});

$("btnAtendimentoFechar").addEventListener("click", () => {
    if (!salvandoAtendimento) {
        modalAtendimento.close();
    }
});

modalAtendimento.addEventListener("cancel", (evento) => {
    if (salvandoAtendimento) {
        evento.preventDefault();
    }
});

modalAtendimento.addEventListener("close", () => {
    idAtendimentoAtual = null;
});