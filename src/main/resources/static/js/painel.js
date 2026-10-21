const $ = (id) => document.getElementById(id);

function esc(valor) {
    return String(valor ?? "").replace(/[&<>"']/g, (caractere) => ({
        "&": "&amp;",
        "<​": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    }[caractere]));
}

async function consultar(url) {
    const resposta = await fetch(url);

    if (!resposta.ok) {
        const erro = await resposta.json().catch(() => ({}));
        throw new Error(
            erro.mensagem || `Falha ao consultar o servidor (${resposta.status}).`
        );
    }

    return resposta.json();
}

function exibirErro(elemento, mensagem) {
    elemento.textContent = mensagem;
    elemento.hidden = !mensagem;
}

function dataHoraBR(valor) {
    return new Date(valor).toLocaleString("pt-BR", {
        dateStyle: "short",
        timeStyle: "short"
    });
}

async function carregarPainel() {
    const agenda = $("agenda");
    const alertas = $("alertas");
    const horas = $("janela").value;

    agenda.innerHTML = `<p class="vazio">Carregando agenda…</p>`;
    alertas.innerHTML = `<p class="vazio">Carregando alertas…</p>`;

    try {
        const dados = await consultar(
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
                        ${esc(dataHoraBR(item.dataHora))}
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

function preencherSelect(elemento, itens, textoVazio) {
    elemento.innerHTML = `
        <option value="">${esc(textoVazio)}</option>
        ${itens.map((item) => `
            <option value="${esc(item.id)}">
                ${esc(item.nome ?? item.nomeModelo ?? item.id)}
            </option>
        `).join("")}
    `;
}

async function carregarOpcoesAgendamento() {
    const [pacientes, procedimentos, profissionais, salas, equipamentos] =
        await Promise.all([
            consultar("/api/pacientes?status=Ativo"),
            consultar("/api/procedimentos?status=Ativo"),
            consultar("/api/profissionais?status=Ativo"),
            consultar("/api/salas?status=Ativa"),
            consultar("/api/equipamentos?status=Disponível")
        ]);

    preencherSelect($("paciente"), pacientes, "Selecione a paciente");
    preencherSelect($("procedimento"), procedimentos, "Selecione o procedimento");
    preencherSelect($("profissional"), profissionais, "Selecione o profissional");
    preencherSelect($("sala"), salas, "Selecione a sala");
    preencherSelect($("equipamento"), equipamentos, "Nenhum equipamento");

    if (procedimentos.length) {
        $("procedimento").dispatchEvent(new Event("change"));
    }
}

$("procedimento").addEventListener("change", async () => {
    try {
        const procedimentos = await consultar("/api/procedimentos?status=Ativo");
        const selecionado = procedimentos.find(
            (item) => String(item.id) === $("procedimento").value
        );

        if (selecionado) {
            $("duracao").value = selecionado.duracaoMinutos;
        }
    } catch (erro) {
        exibirErro($("erroAgendamento"), erro.message);
    }
});

$("btnNovoAgendamento").addEventListener("click", async () => {
    $("formAgendamento").reset();
    exibirErro($("erroAgendamento"), "");

    try {
        await carregarOpcoesAgendamento();
        $("modalAgendamento").showModal();
    } catch (erro) {
        alert(`Não foi possível carregar as opções: ${erro.message}`);
    }
});

$("btnFecharModal").addEventListener("click", () => {
    $("modalAgendamento").close();
});

$("formAgendamento").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    exibirErro($("erroAgendamento"), "");

    const dataHora = $("dataHora").value;

    const dados = {
        idPaciente: Number($("paciente").value),
        idProcedimento: Number($("procedimento").value),
        idProfissional: Number($("profissional").value),
        idSala: Number($("sala").value),
        idEquipamento: $("equipamento").value
            ? Number($("equipamento").value)
            : null,
        dataHora: dataHora ? `${dataHora}:00` : "",
        duracaoMinutos: Number($("duracao").value)
    };

    try {
        const resposta = await fetch("/api/agendamentos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({}));
            throw new Error(erro.mensagem || "Não foi possível salvar o agendamento.");
        }

        $("modalAgendamento").close();
        await carregarPainel();
        alert("Agendamento criado.");
    } catch (erro) {
        exibirErro($("erroAgendamento"), erro.message);
    }
});

$("janela").addEventListener("change", carregarPainel);

$("dataAtual").textContent = new Date().toLocaleDateString("pt-BR", {
    weekday: "long",
    day: "2-digit",
    month: "long",
    year: "numeric"
});

carregarPainel();