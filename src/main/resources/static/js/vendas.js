const API_VENDAS = "/api/vendas";
const API_SALDOS = "/api/saldos";

const $ = (id) => document.getElementById(id);

let procedimentos = [];
let pacotes = [];
let vendas = [];

function esc(t) {
    return String(t ?? "").replace(/[&<>"']/g, (c) => ({
        "&": "&amp;",
        "<​": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    }[c]));
}

function moeda(v) {
    return Number(v ?? 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function dataBR(iso) {
    return iso ? iso.split("-").reverse().join("/") : "-";
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

async function carregarPacientes() {
    const resp = await fetch("/api/pacientes?status=Ativo");
    if (!resp.ok) return;
    const lista = await resp.json();
    const opcoes = lista.map((p) => `<option value="${p.id}">${esc(p.nome)}</option>`).join("");
    $("paciente").innerHTML = opcoes;
    $("pacienteSaldo").innerHTML = opcoes;
}

async function carregarProcedimentos() {
    const resp = await fetch("/api/procedimentos");
    if (!resp.ok) return;
    procedimentos = await resp.json();
    const opcoes = procedimentos
        .filter((p) => !p.status || p.status === "Ativo")
        .map((p) => `<option value="${p.id}" data-valor="${p.valor}">${esc(p.nome)} — ${moeda(p.valor)}</option>`)
        .join("");
    $("procedimento").innerHTML = opcoes;
}

async function carregarPacotes() {
    const resp = await fetch("/api/pacotes?status=Ativo");
    if (!resp.ok) return;
    pacotes = await resp.json();
    const opcoes = pacotes
        .map((p) => `<option value="${p.id}" data-valor="${p.valorTotal}">${esc(p.nome)} — ${moeda(p.valorTotal)}</option>`)
        .join("");
    $("pacote").innerHTML = opcoes;
}

function totalSelecionado() {
    const tipo = $("tipo").value;
    const seletor = tipo === "pacote" ? "#pacote" : "#procedimento";
    const opcao = document.querySelector(`${seletor} option:checked`);
    return opcao ? Number(opcao.dataset.valor ?? 0) : 0;
}

function atualizarResumo() {
    const total = totalSelecionado();
    const n = Number($("numeroParcelas").value) || 1;

    if (n <= 1) {
        $("resumo").textContent = `Total: ${moeda(total)} — pagamento único`;
        return;
    }

    const entrada = Number($("valorEntrada").value || 0);
    const restante = Math.max(total - entrada, 0);
    const base = Math.floor(restante * 100 / n) / 100;
    const primeira = restante - base * (n - 1);

    $("resumo").textContent =
        `Total: ${moeda(total)} • Entrada: ${moeda(entrada)} • 1ª parcela: ${moeda(primeira)} • Demais: ${moeda(base)}/mês`;
}

function atualizarCampos() {
    const ehPacote = $("tipo").value === "pacote";

    $("campoProcedimento").style.display = ehPacote ? "none" : "";
    $("campoPacote").style.display = ehPacote ? "" : "none";
    $("procedimento").disabled = ehPacote;
    $("pacote").disabled = !ehPacote;

    const credito = $("formaPagamento").value === "Crédito";
    $("campoParcelas").style.display = credito ? "" : "none";
    $("campoEntrada").style.display = (credito && Number($("numeroParcelas").value) > 1) ? "" : "none";

    atualizarResumo();
}

/* ---- listagem com filtros ---- */
async function carregarVendas() {
    const resp = await fetch(API_VENDAS);
    if (!resp.ok) {
        $("tabelaVendas").innerHTML =
            `<tr><td colspan="7">Não foi possível carregar as vendas.</td></tr>`;
        return;
    }
    vendas = await resp.json();
    renderizarVendas();
}

function vendasFiltradas() {
    const ini = $("filtroInicio").value;
    const fim = $("filtroFim").value;
    const nome = $("filtroPaciente").value.trim().toLowerCase();

    return vendas.filter((v) => {
        const d = v.dataVenda || "";
        if (ini && (!d || d < ini)) return false;
        if (fim && (!d || d > fim)) return false;
        if (nome && !(v.pacienteNome || "").toLowerCase().includes(nome)) return false;
        return true;
    });
}

function renderizarVendas() {
    const lista = vendasFiltradas();

    $("tabelaVendas").innerHTML = lista.map(linhaVenda).join("")
        || `<tr><td colspan="7">Nenhuma venda encontrada com os filtros atuais.</td></tr>`;

    const total = lista.reduce((s, v) => s + Number(v.valorTotal ?? 0), 0);
    $("resumoFiltro").textContent =
        `${lista.length} venda(s) encontrada(s) — total ${moeda(total)}`;
}

function linhaVenda(v) {
    const item = v.pacoteNome ? `Pacote: ${esc(v.pacoteNome)}` : `Procedimento: ${esc(v.procedimentoNome)}`;
    const pag = v.parcelas.length > 1 ? `${v.formaPagamento} (${v.parcelas.length}x)` : v.formaPagamento;
    return `<tr>
        <td>${dataBR(v.dataVenda)}</td>
        <td>${esc(v.pacienteNome)}</td>
        <td>${item}</td>
        <td>${moeda(v.valorTotal)}</td>
        <td>${esc(pag)}</td>
        <td>${moeda(v.valorEntrada)}</td>
        <td><button onclick="abrirParcelas(${v.id})">Parcelas</button></td>
    </tr>`;
}

function abrirParcelas(id) {
    const venda = vendas.find((x) => x.id === id);
    if (!venda) return;
    $("tabelaParcelas").innerHTML = venda.parcelas.map((p) => `<tr>
        <td>${p.numero}</td>
        <td>${moeda(p.valor)}</td>
        <td>${dataBR(p.vencimento)}</td>
        <td>${dataBR(p.pagamento)}</td>
        <td><span class="badge ${p.status}">${p.status}</span></td>
        <td>${p.status !== "Paga"
            ? `<button onclick="pagarParcela(${venda.id}, ${p.id})">Registrar pagamento</button>`
            : ""}</td>
    </tr>`).join("");
    $("erroParcelas").textContent = "";
    $("modalParcelas").showModal();
}

async function pagarParcela(idVenda, idParcela) {
    const resp = await fetch(`/api/parcelas/${idParcela}/pagar`, { method: "PATCH" });
    if (resp.ok) {
        const vendaAtualizada = await resp.json();
        const indice = vendas.findIndex((x) => x.id === idVenda);
        if (indice >= 0) vendas[indice] = vendaAtualizada;
        abrirParcelas(idVenda);
        carregarVendas();
    } else {
        const erro = await resp.json().catch(() => ({}));
        $("erroParcelas").textContent = erro.mensagem || "Erro ao registrar pagamento.";
    }
}

async function carregarSaldo() {
    const id = $("pacienteSaldo").value;
    if (!id) return;
    const resp = await fetch(`${API_SALDOS}?idPaciente=${id}`);
    if (!resp.ok) return;
    const lista = await resp.json();
    $("tabelaSaldo").innerHTML = lista.map((s) => `<tr>
        <td>${esc(s.procedimentoNome)}</td>
        <td>${s.quantidadeTotal}</td>
        <td>${s.quantidadeUtilizada}</td>
        <td><strong>${s.quantidadeDisponivel}</strong></td>
        <td>${dataBR(s.dataUltimaSessao)}</td>
        <td>#${s.idVendaOrigem}</td>
    </tr>`).join("") || `<tr><td colspan="6">Nenhuma sessão creditada para esta paciente.</td></tr>`;
}

/* ---- nova venda ---- */
$("form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const tipo = $("tipo").value;
    const corpo = {
        idPaciente: Number($("paciente").value),
        idProcedimento: tipo === "procedimento" ? Number($("procedimento").value) : null,
        idPacote: tipo === "pacote" ? Number($("pacote").value) : null,
        formaPagamento: $("formaPagamento").value,
        dataVenda: $("dataVenda").value || null,
        numeroParcelas: Number($("numeroParcelas").value) || 1,
        valorEntrada: $("valorEntrada").value === "" ? null : Number($("valorEntrada").value)
    };
    const resp = await fetch(API_VENDAS, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(corpo)
    });
    if (resp.ok) {
        $("erro").textContent = "";
        toast("Venda registrada com sucesso!");
        $("form").reset();
        $("dataVenda").value = new Date().toISOString().slice(0, 10);
        atualizarCampos();
        carregarVendas();
        carregarSaldo();
    } else {
        const erro = await resp.json().catch(() => ({}));
        $("erro").textContent = erro.mensagem || "Erro ao registrar venda.";
        toast(erro.mensagem || "Erro ao registrar venda.", "erro");
    }
});

/* ---- filtros ---- */
$("filtroInicio").addEventListener("change", renderizarVendas);
$("filtroFim").addEventListener("change", renderizarVendas);
$("filtroPaciente").addEventListener("input", renderizarVendas);
$("btnLimparFiltros").addEventListener("click", () => {
    $("filtroInicio").value = "";
    $("filtroFim").value = "";
    $("filtroPaciente").value = "";
    renderizarVendas();
});

$("tipo").addEventListener("change", atualizarCampos);
$("formaPagamento").addEventListener("change", atualizarCampos);
$("procedimento").addEventListener("change", atualizarResumo);
$("pacote").addEventListener("change", atualizarResumo);
$("numeroParcelas").addEventListener("change", atualizarCampos);
$("valorEntrada").addEventListener("input", atualizarResumo);
$("btnVerSaldo").addEventListener("click", carregarSaldo);
$("btnFecharParcelas").addEventListener("click", () => $("modalParcelas").close());

$("numeroParcelas").innerHTML = Array.from({ length: 12 }, (_, i) => {
    const n = i + 1;
    return `<option value="${n}" ${n === 1 ? "selected" : ""}>${n === 1 ? "À vista" : n + "x"}</option>`;
}).join("");
$("dataVenda").value = new Date().toISOString().slice(0, 10);

carregarPacientes();
carregarProcedimentos();
carregarPacotes();
carregarVendas();
atualizarCampos();