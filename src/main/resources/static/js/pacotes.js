const API = "/api/pacotes";
const $ = (id) => document.getElementById(id);
const modal = $("modal");
let itensAtuais = {};
let idsListados = [];

function esc(t) {
    return String(t ?? "").replace(/[&<>"']/g, (c) => ({
        "&": "&amp;", "<​": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[c]));
}

const moeda = (v) => Number(v).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

async function lista(url) {
    const r = await fetch(url);
    if (!r.ok) {
        const e = await r.json().catch(() => ({}));
        throw new Error(e.mensagem || `Falha ao consultar o servidor (${r.status}).`);
    }
    return r.json();
}

async function carregar() {
    const params = new URLSearchParams();
    if ($("busca").value.trim()) params.set("busca", $("busca").value.trim());
    if ($("filtroStatus").value) params.set("status", $("filtroStatus").value);
    try {
        const pacotes = await lista(`${API}?${params}`);
        const itens = await Promise.all(pacotes.map(p => lista(`${API}/${p.id}/itens`)));
        pacotes.forEach((p, i) => { p.itens = itens[i]; });
        $("tabela").innerHTML = pacotes.map(linha).join("")
            || `<tr><td colspan="7">Nenhum pacote encontrado.</td></tr>`;
    } catch (e) {
        $("tabela").innerHTML = `<tr><td colspan="7">Não foi possível carregar os pacotes: ${esc(e.message)}</td></tr>`;
    }
}

function linha(p) {
    const acao = p.status === "Ativo"
        ? `<button class="perigo" onclick="alterarStatus(${p.id}, 'Inativo')">Inativar</button>`
        : `<button onclick="alterarStatus(${p.id}, 'Ativo')">Reativar</button>`;
    const itens = Array.isArray(p.itens) ? p.itens : [];
    const total = itens.reduce((s, i) => s + i.numeroSessoes, 0);
    const resumo = itens.length
        ? itens.map(i => `${esc(i.nomeProcedimento)} (${i.numeroSessoes}x)`).join("<​br>")
          + `<br><small>Total: ${total} sessões</small>`
        : "-";
    return `<tr>
        <td>${esc(p.nome)}</td><td>${esc(p.descricao || "-")}</td><td>${resumo}</td>
        <td>${moeda(p.valorTotal)}</td><td>${p.validadeDias} dias</td>
        <td><span class="badge ${p.status}">${p.status}</span></td>
        <td><button onclick="editar(${p.id})">Editar</button>${acao}</td></tr>`;
}

async function montarLista(idPacote) {
    const procs = await lista("/api/procedimentos?status=Ativo");
    const itens = idPacote ? await lista(`${API}/${idPacote}/itens`) : [];
    itensAtuais = {};
    itens.forEach(i => { itensAtuais[i.idProcedimento] = i; });
    idsListados = procs.map(p => p.id);
    $("listaProcs").innerHTML = procs.map(p => {
        const item = itensAtuais[p.id];
        return `<div class="proc-linha">
            <input type="checkbox" class="chkProc" value="${p.id}" ${item ? "checked" : ""}>
            <span class="proc-nome">${esc(p.nome)}<small>${moeda(p.valor)} · ${p.duracaoMinutos} min</small></span>
            <span class="proc-qtd">
                <input type="number" class="qtdProc" data-id="${p.id}" min="1" value="${item ? item.numeroSessoes : 1}" ${item ? "" : "disabled"}>
                <small>sessões</small>
            </span>
        </div>`;
    }).join("") || "Nenhum procedimento ativo cadastrado.";
}

$("listaProcs").addEventListener("change", (e) => {
    if (!e.target.classList.contains("chkProc")) return;
    const qtd = document.querySelector(`.qtdProc[data-id="${e.target.value}"]`);
    qtd.disabled = !e.target.checked;
    if (e.target.checked && !qtd.value) qtd.value = 1;
});

function selecionados() {
    return [...document.querySelectorAll(".chkProc:checked")].map(c => ({
        idProcedimento: Number(c.value),
        numeroSessoes: Number(document.querySelector(`.qtdProc[data-id="${c.value}"]`).value)
    }));
}

async function sincronizarItens(idPacote, marcados) {
    const marcadosIds = marcados.map(m => m.idProcedimento);
    for (const idProc of idsListados) {
        const atual = itensAtuais[idProc];
        if (atual && !marcadosIds.includes(idProc)) {
            const r = await fetch(`${API}/${idPacote}/itens/${atual.id}`, { method: "DELETE" });
            if (!r.ok) throw new Error("Erro ao remover procedimento do pacote.");
        }
    }
    for (const m of marcados) {
        const atual = itensAtuais[m.idProcedimento];
        if (atual && atual.numeroSessoes === m.numeroSessoes) continue;
        const r = await fetch(atual ? `${API}/${idPacote}/itens/${atual.id}` : `${API}/${idPacote}/itens`, {
            method: atual ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                idProcedimento: m.idProcedimento,
                numeroSessoes: m.numeroSessoes,
                periodicidadeDias: atual ? atual.periodicidadeDias : null
            })
        });
        if (!r.ok) {
            const e = await r.json().catch(() => ({}));
            throw new Error(e.mensagem || "Erro ao salvar procedimento do pacote.");
        }
    }
}

async function abrirNovo() {
    $("form").reset();
    $("id").value = "";
    $("validadeDias").value = 365;
    $("erro").textContent = "";
    $("tituloForm").textContent = "Novo pacote";
    await montarLista(null);
    modal.showModal();
}

async function editar(id) {
    const p = await lista(`${API}/${id}`);
    $("form").reset();
    $("id").value = p.id;
    $("nome").value = p.nome;
    $("descricao").value = p.descricao ?? "";
    $("valorTotal").value = p.valorTotal;
    $("validadeDias").value = p.validadeDias;
    $("erro").textContent = "";
    $("tituloForm").textContent = "Editar pacote";
    await montarLista(p.id);
    modal.showModal();
}

async function alterarStatus(id, status) {
    if (status === "Inativo" && !confirm("Inativar este pacote?")) return;
    await fetch(`${API}/${id}/status?status=${status}`, { method: "PATCH" });
    carregar();
}

$("form").addEventListener("submit", async (e) => {
    e.preventDefault();
    $("erro").textContent = "";
    const marcados = selecionados();
    if (marcados.length === 0) { $("erro").textContent = "Selecione ao menos um procedimento."; return; }
    if (marcados.some(m => !Number.isInteger(m.numeroSessoes) || m.numeroSessoes < 1)) {
        $("erro").textContent = "Informe pelo menos 1 sessão para cada procedimento marcado."; return;
    }
    const dados = {
        nome: $("nome").value.trim(),
        descricao: $("descricao").value.trim() || null,
        valorTotal: Number($("valorTotal").value),
        validadeDias: Number($("validadeDias").value)
    };
    const id = $("id").value;
    const resp = await fetch(id ? `${API}/${id}` : API, {
        method: id ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dados)
    });
    if (!resp.ok) {
        const erro = await resp.json().catch(() => ({}));
        $("erro").textContent = erro.mensagem || "Erro ao salvar.";
        return;
    }
    const salvo = await resp.json();
    try {
        await sincronizarItens(salvo.id, marcados);
        modal.close();
    } catch (err) {
        $("id").value = salvo.id;
        $("tituloForm").textContent = "Editar pacote";
        await montarLista(salvo.id);
        $("erro").textContent = err.message;
    }
    carregar();
});

$("btnNovo").addEventListener("click", abrirNovo);
$("btnCancelar").addEventListener("click", () => modal.close());
$("busca").addEventListener("input", carregar);
$("filtroStatus").addEventListener("change", carregar);
carregar();