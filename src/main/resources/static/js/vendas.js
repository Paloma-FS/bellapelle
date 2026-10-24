const API_VENDAS = "/api/vendas";
const API_SALDOS = "/api/saldos";

const $ = (id) => document.getElementById(id);

let procedimentos = [];
let pacotes = [];
let vendas = [];

function esc(t) {
    return String(t ?? "").replace(/[&<>"']/g, (c) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    }[c]));
}

function moeda(v) {
    return Number(v ?? 0).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

function dataBR(iso) {
    return iso ? iso.split("-").reverse().join("/") : "-";
}

function normalizarTexto(t) {
    return String(t ?? "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
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

/* ---- abas: nova venda / realizadas / saldo ---- */
function alternarSecaoVenda(secao) {
    const secoes = {
        nova: $("secaoNovaVenda"),
        realizadas: $("secaoVendasRealizadas"),
        saldo: $("secaoSaldoPaciente")
    };

    const botoes = document.querySelectorAll(".vendas-tabs button");
    const nomes = ["nova", "realizadas", "saldo"];

    Object.entries(secoes).forEach(([nome, elemento]) => {
        if (elemento) elemento.hidden = nome !== secao;
    });

    botoes.forEach((botao, indice) => {
        botao.classList.toggle("ativa", nomes[indice] === secao);
    });
}

/* ---- carregamento de opções ---- */
async function carregarPacientes() {
    const resp = await fetch("/api/pacientes?status=Ativo");
    if (!resp.ok) return;

    const lista = await resp.json();
    const opcoes = lista
        .map((p) => `<option value="${p.id}">${esc(p.nome)}</option>`)
        .join("");

    $("paciente").innerHTML = opcoes;
    $("pacienteSaldo").innerHTML = opcoes;
}

async function carregarProcedimentos() {
    const resp = await fetch("/api/procedimentos");
    if (!resp.ok) return;

    procedimentos = await resp.json();

    $("procedimento").innerHTML = procedimentos
        .filter((p) => !p.status || p.status === "Ativo")
        .map((p) => `
            <option value="${p.id}" data-valor="${p.valor}">
                ${esc(p.nome)} — ${moeda(p.valor)}
            </option>
        `)
        .join("");
}

async function carregarPacotes() {
    const resp = await fetch("/api/pacotes?status=Ativo");
    if (!resp.ok) return;

    pacotes = await resp.json();

    $("pacote").innerHTML = pacotes
        .map((p) => `
            <option value="${p.id}" data-valor="${p.valorTotal}">
                ${esc(p.nome)} — ${moeda(p.valorTotal)}
            </option>
        `)
        .join("");
}

/* ---- formulário de nova venda ---- */
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
    const base = Math.floor((restante * 100) / n) / 100;
    const primeira = restante - base * (n - 1);

    $("resumo").textContent =
        `Total: ${moeda(total)} • Entrada: ${moeda(entrada)} • ` +
        `1ª parcela: ${moeda(primeira)} • Demais: ${moeda(base)}/mês`;
}

function atualizarCampos() {
    const ehPacote = $("tipo").value === "pacote";

    $("campoProcedimento").style.display = ehPacote ? "none" : "";
    $("campoPacote").style.display = ehPacote ? "" : "none";
    $("procedimento").disabled = ehPacote;
    $("pacote").disabled = !ehPacote;

    const credito = $("formaPagamento").value === "Crédito";

    $("campoParcelas").style.display = credito ? "" : "none";
    $("campoEntrada").style.display =
        credito && Number($("numeroParcelas").value) > 1 ? "" : "none";

    atualizarResumo();
}

/* ---- vendas realizadas + filtros ---- */
function vendasFiltradas() {
    const ini = $("filtroInicio").value;
    const fim = $("filtroFim").value;
    const nome = normalizarTexto($("filtroPaciente").value);

    return vendas.filter((v) => {
        if (ini && v.dataVenda < ini) return false;
        if (fim && v.dataVenda > fim) return false;
        if (nome && !normalizarTexto(v.pacienteNome).includes(nome)) return false;
        return true;
    });
}

function filtrosAtivos() {
    return Boolean(
        $("filtroInicio").value ||
        $("filtroFim").value ||
        $("filtroPaciente").value.trim()
    );
}

async function carregarVendas() {
    const resp = await fetch(API_VENDAS);
    if (!resp.ok) return;

    vendas = await resp.json();
    renderizarVendas();
}

function renderizarVendas() {
    const lista = vendasFiltradas();

    $("tabelaVendas").innerHTML = lista.map(linhaVenda).join("")
        || `<tr><td colspan="7">Nenhuma venda encontrada.</td></tr>`;

    $("resumoFiltro").textContent = filtrosAtivos()
        ? `${lista.length} venda(s) encontrada(s) para os filtros aplicados.`
        : "";
}

function linhaVenda(v) {
    const item = v.pacoteNome
        ? `Pacote: ${esc(v.pacoteNome)}`
        : `Procedimento: ${esc(v.procedimentoNome)}`;

    const pag = v.parcelas.length > 1
        ? `${v.formaPagamento} (${v.parcelas.length}x)`
        : v.formaPagamento;

    return `
        <tr>
            <td>${dataBR(v.dataVenda)}</td>
            <td>${esc(v.pacienteNome)}</td>
            <td>${item}</td>
            <td>${moeda(v.valorTotal)}</td>
            <td>${esc(pag)}</td>
            <td>${moeda(v.valorEntrada)}</td>
            <td><button onclick="abrirParcelas(${v.id})">Parcelas</button></td>
        </tr>`;
}

/* ---- parcelas ---- */
function abrirParcelas(id) {
    const venda = vendas.find((x) => x.id === id);
    if (!venda) return;

    $("tabelaParcelas").innerHTML = venda.parcelas.map((p) => `
        <tr>
            <td>${p.numero}</td>
            <td>${moeda(p.valor)}</td>
            <td>${dataBR(p.vencimento)}</td>
            <td>${dataBR(p.pagamento)}</td>
            <td><span class="badge ${esc(p.status)}">${esc(p.status)}</span></td>
            <td>
                ${p.status !== "Paga"
                    ? `<button onclick="pagarParcela(${venda.id}, ${p.id})">
                           Registrar pagamento
                       </button>`
                    : ""}
            </td>
        </tr>
    `).join("");

    $("erroParcelas").textContent = "";
    $("modalParcelas").showModal();
}

async function pagarParcela(idVenda, idParcela) {
    const resp = await fetch(`/api/parcelas/${idParcela}/pagar`, {
        method: "PATCH"
    });

    if (resp.ok) {
        const vendaAtualizada = await resp.json();
        const indice = vendas.findIndex((x) => x.id === idVenda);

        if (indice >= 0) vendas[indice] = vendaAtualizada;

        abrirParcelas(idVenda);
        renderizarVendas();
    } else {
        const erro = await resp.json().catch(() => ({}));
        $("erroParcelas").textContent =
            erro.mensagem || "Erro ao registrar pagamento.";
    }
}

/* ---- saldo de sessões ---- */
async function carregarSaldo() {
    const id = $("pacienteSaldo").value;
    if (!id) return;

    const resp = await fetch(`${API_SALDOS}?idPaciente=${id}`);
    if (!resp.ok) return;

    const lista = await resp.json();

    $("tabelaSaldo").innerHTML = lista.map((s) => `
        <tr>
            <td>${esc(s.procedimentoNome)}</td>
            <td>${s.quantidadeTotal}</td>
            <td>${s.quantidadeUtilizada}</td>
            <td><strong>${s.quantidadeDisponivel}</strong></td>
            <td>${dataBR(s.dataUltimaSessao)}</td>
            <td>#${s.idVendaOrigem}</td>
        </tr>
    `).join("") || `
        <tr>
            <td colspan="6">Nenhuma sessão creditada para esta paciente.</td>
        </tr>`;
}

/* ---- eventos ---- */
$("form").addEventListener("submit", async (e) => {
    e.preventDefault();

    const tipo = $("tipo").value;

    const corpo = {
        idPaciente: Number($("paciente").value),
        idProcedimento: tipo === "procedimento"
            ? Number($("procedimento").value)
            : null,
        idPacote: tipo === "pacote"
            ? Number($("pacote").value)
            : null,
        formaPagamento: $("formaPagamento").value,
        dataVenda: $("dataVenda").value || null,
        numeroParcelas: Number($("numeroParcelas").value) || 1,
        valorEntrada: $("valorEntrada").value === ""
            ? null
            : Number($("valorEntrada").value)
    };

    try {
        const resp = await fetch(API_VENDAS, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(corpo)
        });

        if (resp.ok) {
            $("erro").textContent = "";
            $("valorEntrada").value = "";
            toast("Venda registrada com sucesso.");
            carregarVendas();
            carregarSaldo();
        } else {
            const erro = await resp.json().catch(() => ({}));
            $("erro").textContent =
                erro.mensagem || "Erro ao registrar venda.";
        }
    } catch {
        $("erro").textContent = "Não foi possível conectar ao servidor.";
    }
});

$("tipo").addEventListener("change", atualizarCampos);
$("formaPagamento").addEventListener("change", atualizarCampos);
$("procedimento").addEventListener("change", atualizarResumo);
$("pacote").addEventListener("change", atualizarResumo);
$("numeroParcelas").addEventListener("change", atualizarCampos);
$("valorEntrada").addEventListener("input", atualizarResumo);

$("filtroInicio").addEventListener("change", renderizarVendas);
$("filtroFim").addEventListener("change", renderizarVendas);
$("filtroPaciente").addEventListener("input", renderizarVendas);

$("btnLimparFiltros").addEventListener("click", () => {
    $("filtroInicio").value = "";
    $("filtroFim").value = "";
    $("filtroPaciente").value = "";
    renderizarVendas();
});

$("btnVerSaldo").addEventListener("click", carregarSaldo);
$("btnFecharParcelas").addEventListener("click", () => $("modalParcelas").close());

/* ---- inicialização ---- */
$("numeroParcelas").innerHTML = Array.from({ length: 12 }, (_, i) => {
    const n = i + 1;
    return `<option value="${n}" ${n === 1 ? "selected" : ""}>
        ${n === 1 ? "À vista" : `${n}x`}
    </option>`;
}).join("");

const hoje = new Date();
const ano = hoje.getFullYear();
const mes = String(hoje.getMonth() + 1).padStart(2, "0");
const dia = String(hoje.getDate()).padStart(2, "0");
$("dataVenda").value = `${ano}-${mes}-${dia}`;

carregarPacientes();
carregarProcedimentos();
carregarPacotes();
carregarVendas();

atualizarCampos();
alternarSecaoVenda("nova");
