"use strict";

function esc(valor) {
    return String(valor ?? "").replace(/[&<>"']/g, (c) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
    }[c]));
}

function formatarData(valor) {
    if (!valor) return "—";

    const partes = String(valor).split("-");
    if (partes.length !== 3) return esc(valor);

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function formatarMoeda(valor) {
    return Number(valor ?? 0).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

/* ---- abas: comissões / faltas / vendas ---- */
function alternarSecaoRelatorio(secao) {
    const secoes = {
        comissoes: document.getElementById("secaoComissoes"),
        faltas: document.getElementById("secaoFaltas"),
        vendas: document.getElementById("secaoVendas")
    };

    const botoes = document.querySelectorAll(".relatorios-tabs button");
    const nomes = ["comissoes", "faltas", "vendas"];

    Object.entries(secoes).forEach(([nome, elemento]) => {
        if (elemento) elemento.hidden = nome !== secao;
    });

    botoes.forEach((botao, indice) => {
        botao.classList.toggle("ativa", nomes[indice] === secao);
    });
}

function definirPeriodoPadrao(inicioId, fimId) {
    const hoje = new Date();
    const primeiroDia = new Date(hoje.getFullYear(), hoje.getMonth(), 1);

    const paraInputDate = data => {
        const ano = data.getFullYear();
        const mes = String(data.getMonth() + 1).padStart(2, "0");
        const dia = String(data.getDate()).padStart(2, "0");
        return `${ano}-${mes}-${dia}`;
    };

    document.getElementById(inicioId).value = paraInputDate(primeiroDia);
    document.getElementById(fimId).value = paraInputDate(hoje);
}

async function buscarJson(url) {
    const resposta = await fetch(url);
    let dados = null;

    try {
        dados = await resposta.json();
    } catch {
        dados = null;
    }

    if (!resposta.ok) {
        throw new Error(
            dados?.mensagem || "Não foi possível carregar o relatório."
        );
    }

    return dados;
}

function mostrarVazio(tabela, colunas, mensagem) {
    tabela.innerHTML = `
        <tr>
            <td colspan="${colunas}" class="vazio">${esc(mensagem)}</td>
        </tr>
    `;
}

async function carregarComissoes(inicio, fim) {
    const tabela = document.getElementById("tabelaComissoes");
    const mensagem = document.getElementById("comissaoMensagem");
    mensagem.textContent = "";

    try {
        const dados = await buscarJson(
            `/api/relatorios/comissoes?inicio=${encodeURIComponent(inicio)}&fim=${encodeURIComponent(fim)}`
        );

        if (!Array.isArray(dados) || dados.length === 0) {
            mostrarVazio(tabela, 4, "Nenhuma comissão encontrada nesse período.");
            return;
        }

        tabela.innerHTML = dados.map(item => `
            <tr>
                <td>${esc(item.nomeProfissional)}</td>
                <td>${esc(formatarMoeda(item.valorComissao))}</td>
                <td>${esc(item.nomeProcedimento)}</td>
                <td>${esc(formatarData(item.data))}</td>
            </tr>
        `).join("");
    } catch (erro) {
        tabela.innerHTML = "";
        mensagem.textContent = erro.message;
    }
}

async function carregarFaltas(inicio, fim) {
    const tabela = document.getElementById("tabelaFaltas");
    const mensagem = document.getElementById("faltaMensagem");
    mensagem.textContent = "";

    try {
        const dados = await buscarJson(
            `/api/relatorios/faltas?inicio=${encodeURIComponent(inicio)}&fim=${encodeURIComponent(fim)}`
        );

        if (!Array.isArray(dados) || dados.length === 0) {
            mostrarVazio(tabela, 3, "Nenhuma falta encontrada nesse período.");
            return;
        }

        tabela.innerHTML = dados.map(item => `
            <tr>
                <td>${esc(item.nomePaciente)}</td>
                <td>${esc(formatarData(item.dataFalta))}</td>
                <td>${esc(item.faltasAnteriores ?? 0)}</td>
            </tr>
        `).join("");
    } catch (erro) {
        tabela.innerHTML = "";
        mensagem.textContent = erro.message;
    }
}

async function carregarVendas(inicio, fim, tipo) {
    const tabela = document.getElementById("tabelaVendas");
    const mensagem = document.getElementById("vendaMensagem");
    mensagem.textContent = "";

    try {
        const parametros = new URLSearchParams({ inicio, fim, tipo });
        const dados = await buscarJson(`/api/relatorios/vendas?${parametros}`);

        if (!Array.isArray(dados) || dados.length === 0) {
            mostrarVazio(tabela, 5, "Nenhuma venda encontrada nesse período.");
            return;
        }

        tabela.innerHTML = dados.map(item => `
            <tr>
                <td>${esc(item.tipo === "PACOTE" ? "Pacote" : "Procedimento")}</td>
                <td>${esc(item.nomeItem)}</td>
                <td>${esc(item.quantidadeVendida ?? 0)}</td>
                <td>${esc(formatarMoeda(item.valorTotalVendas))}</td>
                <td>${esc(formatarData(item.dataUltimaVenda))}</td>
            </tr>
        `).join("");
    } catch (erro) {
        tabela.innerHTML = "";
        mensagem.textContent = erro.message;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    definirPeriodoPadrao("comissaoInicio", "comissaoFim");
    definirPeriodoPadrao("faltaInicio", "faltaFim");
    definirPeriodoPadrao("vendaInicio", "vendaFim");

    document.getElementById("formComissoes").addEventListener("submit", evento => {
        evento.preventDefault();

        carregarComissoes(
            document.getElementById("comissaoInicio").value,
            document.getElementById("comissaoFim").value
        );
    });

    document.getElementById("formFaltas").addEventListener("submit", evento => {
        evento.preventDefault();

        carregarFaltas(
            document.getElementById("faltaInicio").value,
            document.getElementById("faltaFim").value
        );
    });

    document.getElementById("formVendas").addEventListener("submit", evento => {
        evento.preventDefault();

        carregarVendas(
            document.getElementById("vendaInicio").value,
            document.getElementById("vendaFim").value,
            document.getElementById("vendaTipo").value
        );
    });

    alternarSecaoRelatorio("comissoes");
});
