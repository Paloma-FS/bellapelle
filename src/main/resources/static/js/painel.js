const API = "/api/agendamentos";
const $ = (id) => document.getElementById(id);
const modal = $("modal");

let procs = [];
let pacientes = [];
let candidatosPaciente = [];
let tokenHorarios = 0;
let tokenSaldo = 0;

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

const dataBR = (d) => {
    if (!d) return "–";
    return new Date(`${d}T00:00:00`).toLocaleDateString("pt-BR");
};

async function lista(url) {
    const r = await fetch(url);

    if (!r.ok) {
        const e = await r.json().catch(() => ({}));
        throw new Error(e.mensagem || `Falha ao consultar o servidor (${r.status}).`);
    }

    return r.json();
}

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

/* ---- selects ---- */
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

    if ($("id").value) {
        // Edição: mantém a lista completa, como antes.
        preencher("idProcedimento", procedimentos);
    } else {
        // Novo agendamento: só o que a paciente já comprou e ainda pode agendar.
        await preencherProcedimentosPaciente();
    }
}

/* ---- procedimentos que a paciente já comprou e ainda pode agendar ---- */
let tokenProcsPaciente = 0;

async function preencherProcedimentosPaciente() {
    const seletor = $("idProcedimento");
    const idPaciente = $("idPaciente").value;
    const meuToken = ++tokenProcsPaciente;

    if (!idPaciente) {
        seletor.innerHTML = `<option value="">Selecione a paciente primeiro</option>`;
        return;
    }

    seletor.innerHTML = `<option value="">Consultando sessões…</option>`;

    try {
        const [saldos, agendados, confirmados] = await Promise.all([
            lista(`/api/saldos?idPaciente=${encodeURIComponent(idPaciente)}`),
            lista(`${API}?status=Agendado`),
            lista(`${API}?status=Confirmado`)
        ]);

        // Ignora resposta antiga se a paciente mudou enquanto consultava.
        if (meuToken !== tokenProcsPaciente) return;
        if ($("idPaciente").value !== idPaciente) return;

        // Sessões compradas e ainda não utilizadas, por procedimento.
        const disponiveis = new Map();
        saldos.forEach((s) => {
            const chave = String(s.idProcedimento);
            disponiveis.set(chave, (disponiveis.get(chave) || 0) + Number(s.quantidadeDisponivel || 0));
        });

        // Sessões já reservadas em agendamentos abertos (ainda não realizados).
        const reservadas = new Map();
        [...agendados, ...confirmados]
            .filter((a) => String(a.idPaciente) === String(idPaciente))
            .forEach((a) => {
                const chave = String(a.idProcedimento);
                reservadas.set(chave, (reservadas.get(chave) || 0) + 1);
            });

        const opcoes = procs
            .map((p) => ({
                p,
                livres: (disponiveis.get(String(p.id)) || 0) - (reservadas.get(String(p.id)) || 0)
            }))
            .filter((x) => x.livres > 0);

        if (!opcoes.length) {
            seletor.innerHTML = `<option value="">Paciente sem sessões disponíveis para agendar</option>`;
        } else {
            seletor.innerHTML = opcoes.map((x) => `
                <option value="${esc(x.p.id)}">
                    ${esc(x.p.nome)} (${x.livres} ${x.livres === 1 ? "sessão" : "sessões"})
                </option>
            `).join("");
        }

        // Atualiza duração, saldo e horários com o procedimento agora selecionado.
        seletor.dispatchEvent(new Event("change"));
    } catch (e) {
        if (meuToken !== tokenProcsPaciente) return;
        seletor.innerHTML = `<option value="">Não foi possível consultar as sessões</option>`;
        toast("Não foi possível consultar os procedimentos da paciente: " + e.message, "erro");
    }
}

// Só no NOVO agendamento (na edição o formulário continua como antes).
$("idPaciente").addEventListener("change", () => {
    if (!$("id").value) preencherProcedimentosPaciente();
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

    $("data").value = new Date().toISOString().substring(0, 10);

    await atualizarHorarios();
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
            carregarPainel();
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
$("btnNovoAgendamento").addEventListener("click", abrirNovo);
$("btnCancelar").addEventListener("click", () => modal.close());

/* ---- painel diário ---- */
async function carregarPainel() {
    const agenda = $("agenda");
    const alertas = $("alertas");
    const horas = $("janela").value;

    agenda.innerHTML = `<p class="vazio">Carregando agenda…</p>`;
    alertas.innerHTML = `<p class="vazio">Carregando alertas…</p>`;

    try {
        const dados = await lista(
            `/api/painel?horas=${encodeURIComponent(horas)}`
        );

        $("kpiAtendimentos").textContent = dados.resumo.atendimentosHoje;
        $("kpiFaltas").textContent = dados.resumo.faltasNoMes;
        $("kpiParcelas").textContent = dados.resumo.parcelasVencidas;
        $("kpiSaldo").textContent = dados.resumo.pacientesSaldoBaixo;

        agenda.innerHTML = dados.agenda.length
            ? dados.agenda.map((item) => `
                <article class="agenda-item ${item.emAndamento ? "andamento" : ""}">
                    <div class="agenda-hora">
                        ${esc(fmt(item.dataHora))}
                    </div>
                    <div>
                        <strong>${esc(item.nomePaciente)}</strong>
                        <p>${esc(item.nomeProcedimento)}</p>
                        <small>
                            ${esc(item.nomeProfissional)}
                            · Sala ${esc(item.nomeSala)}
                            ${item.nomeEquipamento
                                ? " · " + esc(item.nomeEquipamento)
                                : ""}
                            · ${esc(item.duracaoMinutos)} min
                            · ${esc(item.status)}
                        </small>
                    </div>
                </article>
            `).join("")
            : `<p class="vazio">Não há agendamentos nessa janela.</p>`;

        alertas.innerHTML = dados.alertas.length
            ? dados.alertas.map((alerta) => `
                <article class="alerta ${alerta.severidade === "vermelho" ? "vermelho" : ""}">
                    <strong>${esc(alerta.titulo)}</strong>
                    <span>${esc(alerta.detalhe)}</span>
                </article>
            `).join("")
            : `<p class="vazio">Nenhum alerta no momento.</p>`;
    } catch (erro) {
        agenda.innerHTML =
            `<p class="erro-painel">${esc(erro.message)}</p>`;
        alertas.innerHTML =
            `<p class="erro-painel">${esc(erro.message)}</p>`;
    }
}

$("janela").addEventListener("change", carregarPainel);

$("dataAtual").textContent = new Date().toLocaleDateString("pt-BR", {
    weekday: "long",
    day: "2-digit",
    month: "long",
    year: "numeric"
});

carregarPainel();