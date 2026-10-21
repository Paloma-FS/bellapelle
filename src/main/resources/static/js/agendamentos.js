const API = "/api/agendamentos";
const $ = (id) => document.getElementById(id);
const modal = $("modal");
const modalCancel = $("modalCancel");
const modalAtendimento = $("modalAtendimento");

let procs = [];
let agsAtuais = [];
let idCancelando = null;
let idAtendimento = null;
let atendimentoInjetavel = false;
let disponiveisAtendimento = [];
let procSelecionado = null;
let tokenHorarios = 0;

function esc(t) {
    return String(t ?? "").replace(/[&<>"']/g, (c) => ({
        "&": "&amp;",
        "<​": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    }[c]));
}

const fmt = (d) =>
    new Date(d).toLocaleString("pt-BR", {
        dateStyle: "short",
        timeStyle: "short"
    });

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
        itens.map(x =>
            `<option value="${x.id}">${esc(x.nome ?? x.nomeModelo ?? x.id)}</option>`
        ).join("");

    if (atual && [...$(sel).options].some(o => o.value === atual)) {
        $(sel).value = atual;
    }
}

async function carregarOpcoes() {
    const [pac, prof, salas, equips, pr] = await Promise.all([
        lista("/api/pacientes"),
        lista("/api/profissionais"),
        lista("/api/salas"),
        lista("/api/equipamentos"),
        lista("/api/procedimentos?status=Ativo")
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
    const p = new URLSearchParams();

    if ($("filtroData").value) {
        p.set("data", $("filtroData").value);
    }
    if ($("filtroStatus").value) {
        p.set("status", $("filtroStatus").value);
    }

    try {
        const ags = await lista(`${API}?${p}`);
        agsAtuais = ags;
        $("tabela").innerHTML = ags.map(linha).join("") ||
            `<tr><td colspan="9">Nenhum agendamento encontrado.</td></tr>`;
    } catch (e) {
        $("tabela").innerHTML =
            `<tr><td colspan="9">Não foi possível carregar os agendamentos: ${esc(e.message)}</td></tr>`;
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
                    ${a.status === "Agendado"
                        ? `<button onclick="mudar(${a.id}, 'Confirmado')">Confirmar</button>`
                        : ""}
                    <button onclick="abrirAtendimento(${a.id})">Marcar realizado</button>
                    <button onclick="mudar(${a.id}, 'Faltou')">Marcar falta</button>
                    <button class="menu-perigo" onclick="abrirCancelar(${a.id})">Cancelar…</button>
                </div>
            </details>
        </div>` : "";

    return `<tr>
        <td>${fmt(a.dataHora)}</td>
        <td>${esc(a.nomePaciente)}</td>
        <td>${esc(a.nomeProcedimento)}</td>
        <td>${esc(a.nomeProfissional)}</td>
        <td>${esc(a.nomeSala)}</td>
        <td>${esc(a.nomeEquipamento || "-")}</td>
        <td>${a.duracaoMinutos} min</td>
        <td>${esc(a.status)}${a.tipoCancelamento
            ? `<br><small>${esc(a.tipoCancelamento)}</small>`
            : ""}</td>
        <td>${acoes}</td>
    </tr>`;
}

function fecharMenus() {
    document.querySelectorAll("details.menu").forEach(d => (d.open = false));
}

async function mudar(id, status, tipo) {
    fecharMenus();

    const q = new URLSearchParams({ status });
    if (tipo) {
        q.set("tipoCancelamento", tipo);
    }

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
    }

    toast(`Agendamento marcado como ${status}.`);
    carregar();
    return true;
}

/* ---- registro da sessão realizada ---- */
function formatarDataHistorico(data) {
    if (!data) {
        return "";
    }

    const partes = String(data).split("-");
    if (partes.length !== 3) {
        return esc(data);
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function exibirHistorico(historico) {
    const secao = $("historicoAtendimento");
    const listaHistorico = $("listaHistoricoAtendimento");

    if (!Array.isArray(historico) || historico.length === 0) {
        secao.hidden = true;
        listaHistorico.innerHTML = "";
        return;
    }

    listaHistorico.innerHTML = historico.slice(0, 5).map(sessao => `
        <div class="saldo-linha">
            <strong>${esc(formatarDataHistorico(sessao.data))} · ${esc(sessao.nomeProcedimento)}</strong>
            <span>${esc(sessao.nomeProfissional)}</span>
            <span>${esc(sessao.descricaoEvolucao)}</span>
        </div>
    `).join("");

    secao.hidden = false;
}

function marcarInjetavelObrigatorio(obrigatorio) {
    $("atendimentoFabricante").required = obrigatorio;
    $("atendimentoNumeroLote").required = obrigatorio;
    $("atendimentoValidadeLote").required = obrigatorio;
}

function aplicarInjetavel(injetavel) {
    atendimentoInjetavel = Boolean(injetavel);
    $("dadosInjetavel").hidden = !atendimentoInjetavel;
    marcarInjetavelObrigatorio(atendimentoInjetavel);
}

function resumoAtendimento(a) {
    return a
        ? `${a.nomePaciente} · ${a.nomeProcedimento} · ${fmt(a.dataHora)} · ${a.nomeProfissional}`
        : "";
}

function aplicarProcedimentoSelecionado(id) {
    procSelecionado = id;

    const d = disponiveisAtendimento.find(
        x => Number(x.idProcedimento) === Number(id));

    // Fabricante/lote/validade somente se o procedimento escolhido for injetável.
    aplicarInjetavel(Boolean(d && d.injetavel));
}

function renderizarDisponiveis(disponiveis, idProcAgendado) {
    const secao = $("disponiveisAtendimento");
    const caixa = $("listaDisponiveisAtendimento");
    const aviso = $("avisoSaldo");

    disponiveisAtendimento = Array.isArray(disponiveis) ? disponiveis : [];
    procSelecionado = null;

    if (!disponiveisAtendimento.length) {
        caixa.innerHTML = "";
        aviso.hidden = false;
        secao.hidden = false;
        return;
    }

    aviso.hidden = true;

    caixa.innerHTML = disponiveisAtendimento.map(d => {
        const ultima = d.dataUltimaSessao
            ? ` · última sessão: ${formatarDataHistorico(d.dataUltimaSessao)}`
            : "";
        const agendado = Number(d.idProcedimento) === Number(idProcAgendado);
        return `
            <label class="opcao">
                <input type="radio" name="procDisponivel"
                    value="${d.idProcedimento}"${agendado ? " checked" : ""}>
                <span>
                    <strong>${esc(d.nomeProcedimento)}${agendado ? " (agendado)" : ""}</strong>
                    <small>${d.quantidadeDisponivel} sessão(ões) disponível(is)${ultima}</small>
                </span>
            </label>`;
    }).join("");

    caixa.querySelectorAll("input[name='procDisponivel']").forEach(r =>
        r.addEventListener("change", () => aplicarProcedimentoSelecionado(Number(r.value)))
    );

    secao.hidden = false;

    // O procedimento agendado já vem selecionado quando tem saldo disponível.
    if (idProcAgendado && disponiveisAtendimento.some(
            d => Number(d.idProcedimento) === Number(idProcAgendado))) {
        aplicarProcedimentoSelecionado(Number(idProcAgendado));
    }
}

async function abrirAtendimento(id) {
    fecharMenus();
    idAtendimento = id;

    mostrarAviso($("erroAtendimento"), "");
    $("formAtendimento").reset();
    $("historicoAtendimento").hidden = true;
    $("listaHistoricoAtendimento").innerHTML = "";
    $("disponiveisAtendimento").hidden = true;
    $("listaDisponiveisAtendimento").innerHTML = "";
    $("avisoSaldo").hidden = true;
    disponiveisAtendimento = [];
    procSelecionado = null;

    // Abre o modal na hora com os dados da linha da tabela;
    // o contexto (disponíveis, histórico, injetável) chega na consulta abaixo.
    $("atendimentoResumo").textContent =
        resumoAtendimento(agsAtuais.find(x => x.id === id));
    aplicarInjetavel(false);

    modalAtendimento.showModal();

    try {
        const contexto = await lista(`${API}/${id}/atendimento`);

        $("atendimentoResumo").textContent =
            resumoAtendimento(contexto.agendamento) || $("atendimentoResumo").textContent;

        exibirHistorico(contexto.historico);
        renderizarDisponiveis(
            contexto.procedimentosDisponiveis,
            contexto.agendamento.idProcedimento);
    } catch (e) {
        mostrarAviso($("erroAtendimento"),
            "Não foi possível carregar os dados da sessão: " + e.message +
            ". Confirme somente se tiver certeza dos dados do procedimento.");
    }
}

$("formAtendimento").addEventListener("submit", async (e) => {
    e.preventDefault();
    mostrarAviso($("erroAtendimento"), "");

    if (!idAtendimento) {
        mostrarAviso($("erroAtendimento"), "Não foi possível identificar o agendamento.");
        return;
    }

    if (disponiveisAtendimento.length && !procSelecionado) {
        mostrarAviso($("erroAtendimento"), "Selecione o procedimento realizado.");
        return;
    }

    const dados = {
        descricaoEvolucao: $("atendimentoDescricao").value.trim(),
        numeroLote: atendimentoInjetavel
            ? $("atendimentoNumeroLote").value.trim()
            : null,
        fabricante: atendimentoInjetavel
            ? $("atendimentoFabricante").value.trim()
            : null,
        validadeLote: atendimentoInjetavel
            ? ($("atendimentoValidadeLote").value || null)
            : null,
        idProcedimento: procSelecionado
    };

    const botao = $("btnConfirmarAtendimento");
    botao.disabled = true;

    try {
        // Confirma pela própria rota de status: marca como Realizado e
        // registra a sessão no prontuário na mesma transação.
        const r = await fetch(`${API}/${idAtendimento}/status?status=Realizado`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        });

        if (!r.ok) {
            const err = await r.json().catch(() => ({}));
            mostrarAviso(
                $("erroAtendimento"),
                err.mensagem || "Não foi possível registrar a sessão."
            );
            return;
        }

        modalAtendimento.close();
        toast("Sessão registrada no prontuário. Agendamento marcado como realizado.");
        carregar();
    } catch (e2) {
        mostrarAviso(
            $("erroAtendimento"),
            "Não foi possível conectar ao servidor."
        );
    } finally {
        botao.disabled = false;
    }
});

$("btnAtendimentoFechar").addEventListener("click", () => {
    modalAtendimento.close();
});

/* ---- cancelamento ---- */
async function abrirCancelar(id) {
    fecharMenus();
    idCancelando = id;
    mostrarAviso($("erroCancel"), "");

    try {
        const a = await lista(`${API}/${id}`);
        $("cancelResumo").textContent =
            `${a.nomePaciente} · ${a.nomeProcedimento} · ${fmt(a.dataHora)}`;
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

    if (ok) {
        modalCancel.close();
    }
});

$("btnCancelFechar").addEventListener("click", () => modalCancel.close());

/* ---- horários livres (calculados pelo servidor) ---- */
function selecionarHorario(valor) {
    $("dataHora").value = valor;
    $("horarioEscolhido").textContent =
        valor ? "· escolhido: " + valor.substring(11, 16) : "";

    document.querySelectorAll("#horarios button").forEach(b =>
        b.classList.toggle("sel", b.dataset.valor === valor)
    );
}

async function atualizarHorarios() {
    const grid = $("horarios");
    const data = $("data").value;
    const pac = $("idPaciente").value;
    const proc = $("idProcedimento").value;
    const prof = $("idProfissional").value;
    const sala = $("idSala").value;
    const equip = $("idEquipamento").value;
    const dur = $("duracao").value;
    const editando = $("id").value;
    const antes = $("dataHora").value;

    if (!data || !pac || !proc || !prof || !sala) {
        grid.innerHTML =
            `<p class="dica">Escolha paciente, procedimento, profissional, sala e data para ver os horários do dia.</p>`;
        selecionarHorario("");
        return;
    }

    const q = new URLSearchParams({
        data,
        idPaciente: pac,
        idProcedimento: proc,
        idProfissional: prof,
        idSala: sala
    });

    if (equip) q.set("idEquipamento", equip);
    if (dur) q.set("duracaoMinutos", dur);
    if (editando) q.set("ignorarId", editando);

    const meuToken = ++tokenHorarios;
    grid.innerHTML = `<p class="dica">Consultando horários…</p>`;

    let slots;

    try {
        slots = await lista(`${API}/horarios-livres?${q}`);
    } catch (e) {
        if (meuToken !== tokenHorarios) return;
        grid.innerHTML =
            `<p class="dica">Não foi possível carregar os horários: ${esc(e.message)}</p>`;
        selecionarHorario("");
        return;
    }

    if (meuToken !== tokenHorarios) return;

    if (!slots.length) {
        grid.innerHTML =
            `<p class="dica">A duração do procedimento não cabe no expediente (09:00 às 19:00).</p>`;
        selecionarHorario("");
        return;
    }

    grid.innerHTML = slots.map(s => {
        const valor = `${data}T${s.hora}`;
        return `<button type="button"
                    class="slot ${s.livre ? "livre" : "ocupado"}"
                    data-valor="${valor}"
                    ${s.livre ? "" : `disabled title="${esc(s.motivo)}"`}>
                    ${s.hora}
                </button>`;
    }).join("");

    if (!slots.some(s => s.livre)) {
        const motivos = [
            ...new Set(
                slots
                    .map(s => s.motivo)
                    .filter(m => m && m !== "Horário já passou")
            )
        ].slice(0, 3);

        grid.innerHTML +=
            `<p class="dica">Nenhum horário livre.${motivos.length
                ? " Motivo: " + esc(motivos.join(" | "))
                : ""}</p>`;
    }

    grid.querySelectorAll("button.livre").forEach(b =>
        b.addEventListener("click", () => selecionarHorario(b.dataset.valor))
    );

    const ainda = grid.querySelector(`button.livre[data-valor="${antes}"]`);
    selecionarHorario(ainda ? antes : "");
}

["idPaciente", "idProfissional", "idSala", "idEquipamento", "data"].forEach(id =>
    $(id).addEventListener("change", atualizarHorarios)
);

$("duracao").addEventListener("input", atualizarHorarios);

$("idProcedimento").addEventListener("change", () => {
    const p = procs.find(x => String(x.id) === $("idProcedimento").value);
    if (p && !$("id").value) {
        $("duracao").value = p.duracaoMinutos;
    }
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
    if (p) {
        $("duracao").value = p.duracaoMinutos;
    }

    $("data").value =
        $("filtroData").value || new Date().toISOString().substring(0, 10);

    await atualizarHorarios();
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
    $("dataHora").value = dh;

    mostrarAviso($("erro"), "");
    $("tituloForm").textContent = "Editar agendamento";

    await atualizarHorarios();
    selecionarHorario(dh);
    modal.showModal();
}

$("form").addEventListener("submit", async (e) => {
    e.preventDefault();
    mostrarAviso($("erro"), "");

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
        idEquipamento: $("idEquipamento").value
            ? Number($("idEquipamento").value)
            : null,
        dataHora: $("dataHora").value + ":00",
        duracaoMinutos: $("duracao").value
            ? Number($("duracao").value)
            : null
    };

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
    }
});

$("btnNovo").addEventListener("click", abrirNovo);
$("btnCancelar").addEventListener("click", () => modal.close());
$("filtroData").addEventListener("change", carregar);
$("filtroStatus").addEventListener("change", carregar);

document.addEventListener("click", (e) => {
    if (!e.target.closest("details.menu")) {
        fecharMenus();
    }
});

carregar();