/* Mostra na tela qualquer erro que impeça a agenda de iniciar. */
window.addEventListener("error", (e) => {
    const titulo = document.getElementById("dataTitulo");
    if (titulo && titulo.textContent === "Carregando…") {
        titulo.textContent = "Erro ao iniciar: " + e.message;
    }
});

window.addEventListener("unhandledrejection", (e) => {
    const titulo = document.getElementById("dataTitulo");
    if (titulo && titulo.textContent === "Carregando…") {
        titulo.textContent = "Erro ao iniciar: " + (e.reason?.message || e.reason);
    }
});

const API = "/api/agendamentos";
const $ = (id) => document.getElementById(id);
const modal = $("modal");
const modalCancel = $("modalCancel");

const HORA_INICIO = 9;
const HORA_FIM = 19;

let dataAtual = new Date().toLocaleDateString("en-CA");
let procs = [];
let pacientes = [];
let candidatosPaciente = [];
let salasAgenda = [];
let equipamentosAgenda = [];
let idCancelando = null;
let tokenHorarios = 0;
let tokenSaldo = 0;

function esc(t) {
    return String(t ?? "").replace(/[&<>"']/g, (c) => ({
        "&": "&amp;",
        "<": "&lt;",
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

const dataBR = (d) => {
    if (!d) return "–";
    return new Date(`${d}T00:00:00`).toLocaleDateString("pt-BR");
};

/* ---- datas da agenda ---- */
function somarDias(dataISO, dias) {
    const d = new Date(`${dataISO}T12:00:00`);
    d.setDate(d.getDate() + dias);
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function rotuloData(dataISO) {
    const d = new Date(`${dataISO}T12:00:00`);
    const txt = d.toLocaleDateString("pt-BR", {
        weekday: "long",
        day: "numeric",
        month: "long"
    });
    return txt.charAt(0).toUpperCase() + txt.slice(1);
}

/* ---- consultas à API ---- */
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
    toast.timer = setTimeout(() => {
        t.hidden = true;
    }, 4000);
}

function mostrarAviso(el, msg) {
    el.textContent = msg || "";
    el.hidden = !msg;
}

/* ---- busca de paciente por nome ou CPF ---- */
function normalizarTexto(texto) {
    return String(texto ?? "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase();
}

function somenteDigitos(texto) {
    return String(texto ?? "").replace(/\D/g, "");
}

function limparEstadoPaciente() {
    $("pacienteAviso").hidden = true;
    $("pacienteBusca").classList.remove("pac-busca-invalida");
}

function definirIdPaciente(id) {
    const campo = $("idPaciente");

    if (String(campo.value ?? "") === String(id ?? "")) {
        return;
    }

    campo.value = id ?? "";
    campo.dispatchEvent(new Event("change"));
}

function buscarPacientes() {
    const campo = $("pacienteBusca");
    const listaPacientes = $("pacienteLista");
    const buscaOriginal = campo.value.trim();
    const buscaNome = normalizarTexto(buscaOriginal);
    const buscaCpf = somenteDigitos(buscaOriginal);

    if (!buscaOriginal) {
        candidatosPaciente = [];
        listaPacientes.innerHTML = "";
        listaPacientes.hidden = true;
        return;
    }

    candidatosPaciente = pacientes
        .filter((p) => {
            const nomeCombina = normalizarTexto(p.nome).includes(buscaNome);
            const cpfCombina = buscaCpf && somenteDigitos(p.cpf).includes(buscaCpf);
            return nomeCombina || cpfCombina;
        })
        .slice(0, 8);

    // Nome digitado corresponde exatamente a uma paciente: seleciona na hora.
    const exata = pacientes.find((p) => normalizarTexto(p.nome) === buscaNome);
    if (exata) {
        escolherPaciente(exata.id);
        return;
    }

    listaPacientes.innerHTML = candidatosPaciente.length
        ? candidatosPaciente.map((p) => `
            <div class="pac-opcao" data-id="${esc(p.id)}">
                <span>${esc(p.nome)}</span>
                <small>CPF: ${esc(p.cpf || "Não informado")}</small>
            </div>
        `).join("")
        : `<div class="pac-vazio">Nenhum paciente encontrado.</div>`;

    listaPacientes.hidden = false;
}

function escolherPaciente(id) {
    const paciente = pacientes.find((p) => String(p.id) === String(id));
    if (!paciente) return;

    definirIdPaciente(paciente.id);
    $("pacienteBusca").value = paciente.nome;
    $("pacienteLista").hidden = true;
    limparEstadoPaciente();
}

$("pacienteBusca").addEventListener("input", () => {
    // Digitar de novo cancela a seleção anterior até escolher outra da lista.
    definirIdPaciente("");
    buscarPacientes();
});

$("pacienteBusca").addEventListener("focus", buscarPacientes);

$("pacienteBusca").addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
        // Enter não envia o formulário: seleciona a primeira candidata da lista.
        e.preventDefault();

        if (candidatosPaciente.length) {
            escolherPaciente(candidatosPaciente[0].id);
        }
    }

    if (e.key === "Escape") {
        $("pacienteLista").hidden = true;
    }
});

$("pacienteBusca").addEventListener("blur", () => {
    // Pequeno atraso para o clique numa opção da lista acontecer primeiro.
    setTimeout(() => {
        if (!$("pacienteBusca").value.trim()) {
            limparEstadoPaciente();
            return;
        }

        if ($("idPaciente").value) {
            limparEstadoPaciente();
            return;
        }

        if (candidatosPaciente.length === 1) {
            escolherPaciente(candidatosPaciente[0].id);
        } else {
            $("pacienteAviso").hidden = false;
            $("pacienteBusca").classList.add("pac-busca-invalida");
        }
    }, 150);
});

$("pacienteLista").addEventListener("click", (e) => {
    const opcao = e.target.closest(".pac-opcao");
    if (opcao) escolherPaciente(opcao.dataset.id);
});

document.addEventListener("click", (e) => {
    if (!e.target.closest(".busca-paciente")) {
        $("pacienteLista").hidden = true;
    }
});

/* ---- selects do formulário ---- */
function preencher(id, itens, vazio) {
    const seletor = $(id);
    const atual = seletor.value;

    seletor.innerHTML =
        (vazio ? `<option value="">${esc(vazio)}</option>` : "") +
        itens.map((x) => `
            <option value="${esc(x.id)}">
                ${esc(x.nome ?? x.nomeModelo ?? x.id)}
            </option>
        `).join("");

    if (atual && [...seletor.options].some((o) => o.value === atual)) {
        seletor.value = atual;
    }
}

async function carregarOpcoes() {
    const [pacientesApi, prof, salas, equipamentos, procedimentos] = await Promise.all([
        lista("/api/pacientes"),
        lista("/api/profissionais"),
        lista("/api/salas"),
        lista("/api/equipamentos"),
        lista("/api/procedimentos?status=Ativo")
    ]);

    pacientes = pacientesApi;
    procs = procedimentos;

    preencher(
        "idProfissional",
        prof.filter((p) => !p.status || p.status === "Ativo")
    );

    preencher(
        "idSala",
        salas.filter((s) => !s.status || s.status === "Ativa")
    );

    preencher(
        "idEquipamento",
        equipamentos.filter((e) => !e.status || e.status === "Disponível"),
        "(nenhum)"
    );

    preencher("idProcedimento", procedimentos);
}

/* ==== agenda do dia (visão por sala) ==== */
const horaRotulo = (h) => String(h).padStart(2, "0") + ":00";

async function carregar() {
    const grade = $("grade");
    $("dataTitulo").textContent = rotuloData(dataAtual);

    grade.innerHTML = `<p class="dica" style="grid-column:1/-1;padding:12px;">Carregando agenda…</p>`;

    try {
        const [agendamentos, salasApi, equipamentosApi] = await Promise.all([
            lista(`${API}?data=${dataAtual}`),
            lista("/api/salas"),
            lista("/api/equipamentos")
        ]);

        equipamentosAgenda = equipamentosApi;

        const idsComAgenda = new Set(agendamentos.map((a) => String(a.idSala)));

        salasAgenda = salasApi
            .filter((s) => s.status === "Ativa" || idsComAgenda.has(String(s.id)))
            .sort((x, y) => String(x.nome).localeCompare(String(y.nome), "pt-BR"));

        if (!salasAgenda.length) {
            grade.innerHTML = `<p class="dica" style="grid-column:1/-1;padding:12px;">
                Nenhuma sala ativa cadastrada. Cadastre salas em Salas &amp; Equipamentos.
            </p>`;
            return;
        }

        renderizarGrade(agendamentos);
    } catch (e) {
        grade.innerHTML = `<p class="dica" style="grid-column:1/-1;padding:12px;">
            Não foi possível carregar a agenda: ${esc(e.message)}
        </p>`;
    }
}

function renderizarGrade(ags) {
    const grade = $("grade");
    const nSalas = salasAgenda.length;
    const nSlots = HORA_FIM - HORA_INICIO + 1;
    const partes = [];

    // cabeçalho: canto vazio + uma coluna por sala
    partes.push(`<div class="ag-canto" style="grid-column:1;grid-row:1;"></div>`);

    salasAgenda.forEach((s, i) => {
        const eqs = equipamentosAgenda.filter(
            (e) => String(e.idSala) === String(s.id)
        );
        const linhaEq = eqs
            .map((e) =>
                esc(e.nomeModelo) +
                (e.status === "Em manutenção" ? " — em manutenção" : "")
            )
            .join(" · ");

        partes.push(`
            <div class="ag-sala-header" style="grid-column:${i + 2};grid-row:1;">
                <strong>${esc(s.nome)}</strong>
                ${linhaEq ? `<small>${linhaEq}</small>` : ""}
            </div>
        `);
    });

    // coluna de horários + células vazias
    for (let h = HORA_INICIO; h <= HORA_FIM; h++) {
        const r = h - HORA_INICIO + 2;

        partes.push(
            `<div class="ag-hora" style="grid-column:1;grid-row:${r};">${horaRotulo(h)}</div>`
        );

        for (let c = 1; c <= nSalas; c++) {
            partes.push(
                `<div class="ag-celula" style="grid-column:${c + 1};grid-row:${r};"></div>`
            );
        }
    }

    // salas com equipamento em manutenção ficam bloqueadas o dia todo
    salasAgenda.forEach((s, i) => {
        const bloqueada = equipamentosAgenda.some(
            (e) => String(e.idSala) === String(s.id) && e.status === "Em manutenção"
        );

        if (!bloqueada) return;

        partes.push(`
            <div class="ag-bloqueio" style="grid-column:${i + 2};grid-row:2 / span ${nSlots};">
                <strong>Bloqueado</strong>
                <small>Equipamento em manutenção</small>
            </div>
        `);
    });

    // agendamentos do dia
    ags
        .filter((a) => a.status !== "Cancelado")
        .forEach((a) => {
            const col = salasAgenda.findIndex(
                (s) => String(s.id) === String(a.idSala)
            );

            if (col < 0) return;

            const hora = Number(String(a.dataHora).substring(11, 13));
            const minuto = String(a.dataHora).substring(14, 16);

            if (Number.isNaN(hora)) return;

            const linha = Math.min(Math.max(hora, HORA_INICIO), HORA_FIM) - HORA_INICIO + 2;
            const dur = Math.max(1, Math.ceil((a.duracaoMinutos || 60) / 60));
            const span = Math.min(dur, HORA_FIM + 2 - linha);
            const aberto = a.status === "Agendado" || a.status === "Confirmado";

            const menu = aberto ? `
                <details class="menu ag-menu" onclick="event.stopPropagation()">
                    <summary class="ag-menu-btn">⋯</summary>
                    <div class="menu-lista">
                        <button onclick="editar(${a.id})">Editar</button>
                        ${a.status === "Agendado"
                    ? `<button onclick="mudar(${a.id}, 'Confirmado')">Confirmar</button>`
                    : ""}
                        <button onclick="mudar(${a.id}, 'Realizado')">Marcar realizado</button>
                        <button onclick="mudar(${a.id}, 'Faltou')">Marcar falta</button>
                        <button class="menu-perigo" onclick="abrirCancelar(${a.id})">Cancelar…</button>
                    </div>
                </details>
            ` : "";

            partes.push(`
                <div class="ag-bloco st-${esc(a.status)} ${aberto ? "aberto" : ""}"
                     style="grid-column:${col + 2};grid-row:${linha} / span ${span};"
                     ${aberto ? `onclick="editar(${a.id})"` : ""}
                     title="${esc(a.nomeProcedimento)} · ${esc(a.nomeProfissional)}">
                    ${menu}
                    <strong>${esc(a.nomePaciente)}</strong>
                    <small>
                        ${esc(a.nomeProcedimento)} · ${esc(a.nomeProfissional)}
                        ${minuto !== "00" ? ` · ${hora}:${minuto}` : ""}
                    </small>
                </div>
            `);
        });

    grade.style.gridTemplateColumns = `72px repeat(${nSalas}, minmax(175px, 1fr))`;
    grade.innerHTML = partes.join("");
}

/* ---- ações de status ---- */
function fecharMenus() {
    document.querySelectorAll("details.menu").forEach((d) => {
        d.open = false;
    });
}

async function mudar(id, status, tipo) {
    fecharMenus();

    const params = new URLSearchParams({ status });
    if (tipo) params.set("tipoCancelamento", tipo);

    try {
        const r = await fetch(`${API}/${id}/status?${params}`, {
            method: "PATCH"
        });

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

$("btnCancelFechar").addEventListener("click", () => {
    modalCancel.close();
});

/* ---- saldo de sessões da paciente selecionada ---- */
async function carregarSaldo() {
    const box = $("infoSaldo");
    const idPaciente = $("idPaciente").value;
    const meuToken = ++tokenSaldo;

    if (!idPaciente) {
        box.hidden = true;
        box.innerHTML = "";
        return;
    }

    box.hidden = false;
    box.innerHTML = `<div class="saldo-titulo">Consultando sessões disponíveis…</div>`;

    try {
        const saldos = await lista(
            `/api/saldos?idPaciente=${encodeURIComponent(idPaciente)}`
        );

        // Ignora uma resposta antiga se o usuário já trocou de paciente.
        if (meuToken !== tokenSaldo) return;
        if ($("idPaciente").value !== idPaciente) return;

        if (!saldos.length) {
            box.innerHTML = `
                <div class="saldo-titulo">Sessões disponíveis</div>
                <div class="saldo-vazio">
                    Esta paciente não possui sessões creditadas.
                    Registre uma venda em Vendas para liberar as sessões.
                </div>
            `;
            return;
        }

        const procSel = $("idProcedimento").value;

        box.innerHTML = `
            <div class="saldo-titulo">
                Sessões disponíveis — ${esc(saldos[0].pacienteNome)}
            </div>
            <table class="saldo-tabela">
                <thead>
                <tr>
                    <th>Procedimento</th>
                    <th>Total</th>
                    <th>Usadas</th>
                    <th>Disponíveis</th>
                    <th>Última sessão</th>
                    <th>Origem</th>
                </tr>
                </thead>
                <tbody>
                ${saldos.map((s) => `
                    <tr class="${String(s.idProcedimento) === String(procSel) ? "saldo-sel" : ""}">
                    <td>${esc(s.procedimentoNome)}</td>
                    <td>${s.quantidadeTotal}</td>
                    <td>${s.quantidadeUtilizada}</td>
                    <td><strong>${s.quantidadeDisponivel}</strong></td>
                    <td>${dataBR(s.dataUltimaSessao)}</td>
                    <td>${s.pacoteNome
                    ? "Pacote: " + esc(s.pacoteNome)
                    : "Avulso"} · #${esc(s.idVendaOrigem)}</td>
                    </tr>
                `).join("")}
                </tbody>
            </table>
        `;
    } catch (e) {
        if (meuToken !== tokenSaldo) return;

        box.innerHTML = `
            <div class="saldo-erro">
                Não foi possível consultar as sessões: ${esc(e.message)}
            </div>
        `;
    }
}

/* ---- horários livres ---- */
function selecionarHorario(valor) {
    $("dataHora").value = valor;
    $("horarioEscolhido").textContent =
        valor ? "· escolhido: " + valor.substring(11, 16) : "";

    document.querySelectorAll("#horarios button").forEach((b) => {
        b.classList.toggle("sel", b.dataset.valor === valor);
    });
}

async function atualizarHorarios() {
    const grid = $("horarios");
    const data = $("data").value;
    const idPaciente = $("idPaciente").value;
    const idProcedimento = $("idProcedimento").value;
    const idProfissional = $("idProfissional").value;
    const idSala = $("idSala").value;
    const idEquipamento = $("idEquipamento").value;
    const duracao = $("duracao").value;
    const editando = $("id").value;
    const antes = $("dataHora").value;

    if (!data || !idPaciente || !idProcedimento || !idProfissional || !idSala) {
        const dica = !idPaciente && $("pacienteBusca").value.trim()
            ? "Digite e selecione uma paciente da lista (clique no nome ou pressione Enter)."
            : "Escolha paciente, procedimento, profissional, sala e data para ver os horários do dia.";

        grid.innerHTML = `<p class="dica">${esc(dica)}</p>`;
        selecionarHorario("");
        return;
    }

    const params = new URLSearchParams({
        data,
        idPaciente,
        idProcedimento,
        idProfissional,
        idSala
    });

    if (idEquipamento) params.set("idEquipamento", idEquipamento);
    if (duracao) params.set("duracaoMinutos", duracao);
    if (editando) params.set("ignorarId", editando);

    const meuToken = ++tokenHorarios;
    grid.innerHTML = `<p class="dica">Consultando horários…</p>`;

    let slots;

    try {
        slots = await lista(`${API}/horarios-livres?${params}`);
    } catch (e) {
        if (meuToken !== tokenHorarios) return;

        grid.innerHTML = `
            <p class="dica">
                Não foi possível carregar os horários: ${esc(e.message)}
            </p>
        `;
        selecionarHorario("");
        return;
    }

    if (meuToken !== tokenHorarios) return;

    if (!slots.length) {
        grid.innerHTML = `
            <p class="dica">
                A duração do procedimento não cabe no expediente (09:00 às 19:00).
            </p>
        `;
        selecionarHorario("");
        return;
    }

    grid.innerHTML = slots.map((s) => {
        const valor = `${data}T${s.hora}`;

        return `
            <button
                type="button"
                class="slot ${s.livre ? "livre" : "ocupado"}"
                data-valor="${esc(valor)}"
                ${s.livre ? "" : `disabled title="${esc(s.motivo)}"`}
            >
                ${esc(s.hora)}
            </button>
        `;
    }).join("");

    if (!slots.some((s) => s.livre)) {
        const motivos = [
            ...new Set(
                slots
                    .map((s) => s.motivo)
                    .filter((m) => m && m !== "Horário já passou")
            )
        ].slice(0, 3);

        grid.innerHTML += `
            <p class="dica">
                Nenhum horário livre.
                ${motivos.length ? "Motivo: " + esc(motivos.join(" | ")) : ""}
            </p>
        `;
    }

    grid.querySelectorAll("button.livre").forEach((botao) => {
        botao.addEventListener("click", () => {
            selecionarHorario(botao.dataset.valor);
        });
    });

    const aindaDisponivel = grid.querySelector(
        `button.livre[data-valor="${antes}"]`
    );

    selecionarHorario(aindaDisponivel ? antes : "");
}

/* ---- atualização de horários e saldo ---- */
["idPaciente", "idProfissional", "idSala", "idEquipamento", "data"].forEach((id) => {
    $(id).addEventListener("change", atualizarHorarios);
});

$("idPaciente").addEventListener("change", carregarSaldo);
$("idProcedimento").addEventListener("change", carregarSaldo);

$("duracao").addEventListener("input", atualizarHorarios);

$("idProcedimento").addEventListener("change", () => {
    const procedimento = procs.find(
        (p) => String(p.id) === $("idProcedimento").value
    );

    if (procedimento && !$("id").value) {
        $("duracao").value = procedimento.duracaoMinutos;
    }

    atualizarHorarios();
});

/* ---- novo agendamento ---- */
async function abrirNovo() {
    $("form").reset();
    $("id").value = "";
    definirIdPaciente("");
    $("pacienteBusca").value = "";
    $("pacienteLista").hidden = true;
    candidatosPaciente = [];
    limparEstadoPaciente();
    $("dataHora").value = "";

    mostrarAviso($("erro"), "");
    $("infoSaldo").hidden = true;
    $("infoSaldo").innerHTML = "";
    $("tituloForm").textContent = "Novo agendamento";

    try {
        await carregarOpcoes();
    } catch (e) {
        toast("Não foi possível carregar as opções: " + e.message, "erro");
        return;
    }

    const procedimento = procs.find(
        (p) => String(p.id) === $("idProcedimento").value
    );

    if (procedimento) {
        $("duracao").value = procedimento.duracaoMinutos;
    }

    $("data").value = dataAtual;

    await atualizarHorarios();
    modal.showModal();
}

/* ---- editar agendamento ---- */
async function editar(id) {
    fecharMenus();

    let agendamento;

    try {
        agendamento = await lista(`${API}/${id}`);
        await carregarOpcoes();
    } catch (e) {
        toast("Não foi possível abrir o agendamento: " + e.message, "erro");
        return;
    }

    $("id").value = agendamento.id;
    definirIdPaciente(agendamento.idPaciente);
    $("pacienteBusca").value = agendamento.nomePaciente || "";
    $("pacienteLista").hidden = true;
    limparEstadoPaciente();

    $("idProcedimento").value = agendamento.idProcedimento;
    $("idProfissional").value = agendamento.idProfissional;
    $("idSala").value = agendamento.idSala;
    $("idEquipamento").value = agendamento.idEquipamento ?? "";
    $("duracao").value = agendamento.duracaoMinutos;

    const dataHora = String(agendamento.dataHora).substring(0, 16);
    $("data").value = dataHora.substring(0, 10);
    $("dataHora").value = dataHora;

    mostrarAviso($("erro"), "");
    $("tituloForm").textContent = "Editar agendamento";

    await carregarSaldo();
    await atualizarHorarios();
    selecionarHorario(dataHora);

    modal.showModal();
}

/* ---- salvar agendamento ---- */
$("form").addEventListener("submit", async (e) => {
    e.preventDefault();
    mostrarAviso($("erro"), "");

    if (!$("idPaciente").value) {
        mostrarAviso($("erro"), "Busque e selecione uma paciente pelo nome ou CPF.");
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
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (r.ok) {
            modal.close();
            toast(id ? "Agendamento atualizado." : "Agendamento criado.");
            carregar();
        } else {
            const erro = await r.json().catch(() => ({}));
            mostrarAviso($("erro"), erro.mensagem || "Erro ao salvar.");
            atualizarHorarios();
        }
    } catch (e2) {
        mostrarAviso($("erro"), "Não foi possível conectar ao servidor.");
    }
});

/* ---- botões ---- */
$("btnNovo").addEventListener("click", abrirNovo);
$("btnCancelar").addEventListener("click", () => modal.close());

$("btnDiaAnterior").addEventListener("click", () => {
    dataAtual = somarDias(dataAtual, -1);
    carregar();
});

$("btnDiaProximo").addEventListener("click", () => {
    dataAtual = somarDias(dataAtual, 1);
    carregar();
});

$("btnHoje").addEventListener("click", () => {
    dataAtual = new Date().toLocaleDateString("en-CA");
    carregar();
});

document.addEventListener("click", (e) => {
    if (!e.target.closest("details.menu")) {
        fecharMenus();
    }
});

/* ---- carregamento inicial ---- */
carregar();
