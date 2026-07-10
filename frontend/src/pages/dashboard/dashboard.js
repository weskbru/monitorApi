import { fetchJson, fetchNullable } from "../../shared/api/http.js";
import {
  escapeHtml,
  formatDate,
  formatTime,
  normalizeStatus,
  statusBadge,
  statusWeight,
} from "../../shared/ui/formatters.js";

const state = {
  systems: [],
  selectedSystemId: null,
};

export function renderDashboardPage(root) {
  root.innerHTML = dashboardTemplate();

  const elements = getDashboardElements(root);

  elements.refreshButton.addEventListener("click", () => loadDashboard(elements));
  elements.systemTableBody.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-system-id]");

    if (!button) {
      return;
    }

    state.selectedSystemId = Number(button.dataset.systemId);
    renderSystemRows(elements, state.systems);

    try {
      await loadEndpointsForSelectedSystem(elements);
    } catch (error) {
      elements.endpointTableBody.innerHTML = endpointErrorRow();
    }
  });

  loadDashboard(elements);
}

function dashboardTemplate() {
  return `
    <section class="app-view active-view">
      <header class="topbar">
        <div>
          <p class="eyebrow">Ambiente local</p>
          <h1>Dashboard operacional</h1>
          <p class="page-summary">Visao central dos sistemas monitorados, endpoints criticos e sinais de indisponibilidade.</p>
        </div>
        <div class="topbar-actions">
          <span class="health-pill health-neutral" id="health-pill">Sincronizando</span>
          <button class="ghost-button" id="refresh-button" type="button">Atualizar</button>
        </div>
      </header>

      <section class="metrics-grid" aria-label="Resumo operacional">
        <article class="metric-card">
          <span>Sistemas</span>
          <strong id="metric-total">0</strong>
          <small>monitorados</small>
        </article>
        <article class="metric-card">
          <span>Saude geral</span>
          <strong id="metric-health">0%</strong>
          <small id="metric-health-label">Sem dados</small>
        </article>
        <article class="metric-card">
          <span>Disponiveis</span>
          <strong class="status-up" id="metric-up">0</strong>
          <small>UP</small>
        </article>
        <article class="metric-card">
          <span>Lentos</span>
          <strong class="status-slow" id="metric-slow">0</strong>
          <small>SLOW</small>
        </article>
        <article class="metric-card">
          <span>Indisponiveis</span>
          <strong class="status-down" id="metric-down">0</strong>
          <small>DOWN</small>
        </article>
        <article class="metric-card">
          <span>Sem leitura</span>
          <strong id="metric-unknown">0</strong>
          <small>UNKNOWN</small>
        </article>
        <article class="metric-card">
          <span>Endpoints</span>
          <strong id="metric-endpoints">0</strong>
          <small>criticos ativos</small>
        </article>
        <article class="metric-card metric-accent">
          <span>Atualizacao</span>
          <strong id="metric-updated">-</strong>
          <small>horario local</small>
        </article>
      </section>

      <section class="status-strip" aria-label="Estado atual">
        <div>
          <span class="strip-label">Sinal operacional</span>
          <strong id="status-strip-title">Carregando ambiente</strong>
        </div>
        <p id="status-strip-message">Buscando dados do backend.</p>
      </section>

      <div class="content-grid">
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>Sistemas monitorados</h2>
              <p id="systems-panel-subtitle">Carregando dados do backend...</p>
            </div>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Sistema</th>
                  <th>Base URL</th>
                  <th>Status</th>
                  <th>Endpoints</th>
                  <th>Ultima leitura</th>
                  <th>Ativo</th>
                </tr>
              </thead>
              <tbody id="system-table-body">
                <tr>
                  <td colspan="6" class="empty-cell">Conectando ao backend...</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <aside class="side-stack" aria-label="Resumo lateral">
          <section class="panel compact-panel">
            <div class="panel-header">
              <div>
                <h2 id="detail-title">Sistema selecionado</h2>
                <p id="detail-subtitle">Aguardando dados.</p>
              </div>
            </div>
            <div class="detail-body">
              <div class="detail-status" id="detail-status">-</div>
              <dl class="detail-list">
                <div>
                  <dt>Endpoints ativos</dt>
                  <dd id="detail-total">0</dd>
                </div>
                <div>
                  <dt>Base URL</dt>
                  <dd id="detail-base-url">-</dd>
                </div>
                <div>
                  <dt>Ultima leitura</dt>
                  <dd id="detail-last-check">-</dd>
                </div>
              </dl>
            </div>
          </section>

          <section class="panel compact-panel">
            <div class="panel-header">
              <div>
                <h2>Fila operacional</h2>
                <p>Prioridade por sistema</p>
              </div>
            </div>
            <div class="priority-list" id="priority-list">
              <p class="empty-block">Nenhuma prioridade calculada.</p>
            </div>
          </section>
        </aside>
      </div>

      <section class="panel endpoint-panel">
        <div class="panel-header">
          <div>
            <h2>Endpoints do sistema</h2>
            <p id="endpoints-panel-subtitle">Selecione um sistema para ver os endpoints criticos.</p>
          </div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Endpoint</th>
                <th>URL</th>
                <th>Status</th>
                <th>Codigo</th>
                <th>Resposta</th>
                <th>Ultima leitura</th>
                <th>Ativo</th>
              </tr>
            </thead>
            <tbody id="endpoint-table-body">
              <tr>
                <td colspan="7" class="empty-cell">Aguardando sistema.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  `;
}

function getDashboardElements(root) {
  return {
    systemTableBody: root.querySelector("#system-table-body"),
    endpointTableBody: root.querySelector("#endpoint-table-body"),
    systemsPanelSubtitle: root.querySelector("#systems-panel-subtitle"),
    endpointsPanelSubtitle: root.querySelector("#endpoints-panel-subtitle"),
    refreshButton: root.querySelector("#refresh-button"),
    healthPill: root.querySelector("#health-pill"),
    priorityList: root.querySelector("#priority-list"),
    statusStripTitle: root.querySelector("#status-strip-title"),
    statusStripMessage: root.querySelector("#status-strip-message"),
    detailTitle: root.querySelector("#detail-title"),
    detailSubtitle: root.querySelector("#detail-subtitle"),
    detailStatus: root.querySelector("#detail-status"),
    detailTotal: root.querySelector("#detail-total"),
    detailBaseUrl: root.querySelector("#detail-base-url"),
    detailLastCheck: root.querySelector("#detail-last-check"),
    metricTotal: root.querySelector("#metric-total"),
    metricHealth: root.querySelector("#metric-health"),
    metricHealthLabel: root.querySelector("#metric-health-label"),
    metricUp: root.querySelector("#metric-up"),
    metricSlow: root.querySelector("#metric-slow"),
    metricDown: root.querySelector("#metric-down"),
    metricUnknown: root.querySelector("#metric-unknown"),
    metricEndpoints: root.querySelector("#metric-endpoints"),
    metricUpdated: root.querySelector("#metric-updated"),
  };
}

async function loadDashboard(elements) {
  elements.refreshButton.disabled = true;
  elements.systemsPanelSubtitle.textContent = "Carregando dados do backend...";

  try {
    const systems = await fetchJson("/api/monitored-systems");
    const items = await Promise.all(
      systems.map(async (system) => {
        const status = await fetchNullable(`/api/monitored-systems/${system.id}/status`);

        return buildSystemItem(system, status);
      })
    );

    state.systems = items;
    selectDefaultSystem(items);

    renderSystemRows(elements, items);
    updateMetrics(elements, items);
    renderPriorityList(elements, items);
    elements.systemsPanelSubtitle.textContent = `${items.length} sistema(s) monitorado(s) encontrado(s).`;

    try {
      await loadEndpointsForSelectedSystem(elements);
    } catch (error) {
      elements.endpointTableBody.innerHTML = endpointErrorRow();
      elements.endpointsPanelSubtitle.textContent = "Falha ao carregar endpoints do sistema selecionado.";
    }
  } catch (error) {
    renderDashboardError(elements);
  } finally {
    elements.refreshButton.disabled = false;
  }
}

function buildSystemItem(system, status) {
  return {
    system,
    status: status ?? {
      systemId: system.id,
      name: system.name,
      status: "UNKNOWN",
      message: "Ainda nao ha leitura suficiente para calcular a saude do sistema.",
      totalEndpoints: 0,
      upEndpoints: 0,
      slowEndpoints: 0,
      downEndpoints: 0,
      unknownEndpoints: 0,
      lastCheckedAt: null,
    },
  };
}

function sortSystemsByPriority(items) {
  return [...items].sort((first, second) => {
    const statusDiff = statusWeight(first.status?.status) - statusWeight(second.status?.status);

    if (statusDiff !== 0) {
      return statusDiff;
    }

    return String(first.system.name).localeCompare(String(second.system.name), "pt-BR");
  });
}

function renderSystemRows(elements, items) {
  if (items.length === 0) {
    elements.systemTableBody.innerHTML = `
      <tr>
        <td colspan="6" class="empty-cell">Nenhum sistema monitorado cadastrado.</td>
      </tr>
    `;
    return;
  }

  elements.systemTableBody.innerHTML = sortSystemsByPriority(items)
    .map(({ system, status }) => {
      const isSelected = system.id === state.selectedSystemId;

      return `
        <tr class="system-row ${isSelected ? "selected-row" : ""}" data-system-id="${escapeHtml(system.id)}">
          <td>
            <button class="row-button" type="button" data-system-id="${escapeHtml(system.id)}">
              <strong>${escapeHtml(system.name)}</strong>
              <span class="muted">#${escapeHtml(system.id)}</span>
            </button>
          </td>
          <td class="url-cell">${escapeHtml(system.baseUrl)}</td>
          <td>${statusBadge(status?.status)}</td>
          <td>${status?.totalEndpoints ?? 0}</td>
          <td>${formatDate(status?.lastCheckedAt)}</td>
          <td>${system.active ? "Sim" : "Nao"}</td>
        </tr>
      `;
    })
    .join("");
}

function renderEndpointRows(elements, items) {
  if (items.length === 0) {
    elements.endpointTableBody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-cell">Nenhum endpoint critico cadastrado para este sistema.</td>
      </tr>
    `;
    return;
  }

  elements.endpointTableBody.innerHTML = items
    .sort((first, second) => statusWeight(first.status?.status) - statusWeight(second.status?.status))
    .map(({ api, status }) => {
      return `
        <tr>
          <td>
            <strong>${escapeHtml(api.name)}</strong>
            <span class="muted">#${escapeHtml(api.id)}</span>
          </td>
          <td class="url-cell">${escapeHtml(api.url)}</td>
          <td>${statusBadge(status?.status)}</td>
          <td>${status?.statusCode ?? "-"}</td>
          <td>${status?.responseTimeMs != null ? `${status.responseTimeMs}ms` : "-"}</td>
          <td>${formatDate(status?.checkedAt)}</td>
          <td>${api.active ? "Sim" : "Nao"}</td>
        </tr>
      `;
    })
    .join("");
}

function updateMetrics(elements, items) {
  const totals = items.reduce(
    (accumulator, item) => {
      const status = normalizeStatus(item.status?.status);

      accumulator.total += 1;
      accumulator.endpoints += item.status?.totalEndpoints ?? 0;

      if (status === "UP") {
        accumulator.up += 1;
      }

      if (status === "SLOW") {
        accumulator.slow += 1;
      }

      if (status === "DOWN") {
        accumulator.down += 1;
      }

      if (status === "UNKNOWN") {
        accumulator.unknown += 1;
      }

      return accumulator;
    },
    { total: 0, up: 0, slow: 0, down: 0, unknown: 0, endpoints: 0 }
  );

  const healthPercent = totals.total
    ? Math.round(((totals.up + totals.slow) / totals.total) * 100)
    : 0;

  elements.metricTotal.textContent = totals.total;
  elements.metricHealth.textContent = `${healthPercent}%`;
  elements.metricHealthLabel.textContent = totals.total ? "Sistemas operacionais" : "Sem dados";
  elements.metricUp.textContent = totals.up;
  elements.metricSlow.textContent = totals.slow;
  elements.metricDown.textContent = totals.down;
  elements.metricUnknown.textContent = totals.unknown;
  elements.metricEndpoints.textContent = totals.endpoints;
  elements.metricUpdated.textContent = formatTime(new Date());

  updateOperationalState(elements, totals, healthPercent);
}

function updateOperationalState(elements, totals, healthPercent) {
  elements.healthPill.className = "health-pill";

  if (totals.down > 0) {
    elements.healthPill.classList.add("health-danger");
    elements.healthPill.textContent = "Incidente";
    elements.statusStripTitle.textContent = `${totals.down} sistema(s) com falha critica`;
    elements.statusStripMessage.textContent = "Priorize os sistemas marcados como DOWN e valide os endpoints afetados.";
    return;
  }

  if (totals.slow > 0) {
    elements.healthPill.classList.add("health-warning");
    elements.healthPill.textContent = "Atencao";
    elements.statusStripTitle.textContent = `${totals.slow} sistema(s) com lentidao`;
    elements.statusStripMessage.textContent = "Acompanhe os endpoints SLOW antes que a lentidao vire indisponibilidade.";
    return;
  }

  if (totals.total === 0 || totals.unknown === totals.total) {
    elements.healthPill.classList.add("health-neutral");
    elements.healthPill.textContent = "Sem leitura";
    elements.statusStripTitle.textContent = "Nenhuma leitura operacional";
    elements.statusStripMessage.textContent = "Cadastre sistemas e endpoints ou execute verificacoes para formar o primeiro estado.";
    return;
  }

  elements.healthPill.classList.add("health-ok");
  elements.healthPill.textContent = `${healthPercent}% saudavel`;
  elements.statusStripTitle.textContent = "Ambiente sem incidentes criticos";
  elements.statusStripMessage.textContent = "Os sistemas com leitura recente estao respondendo dentro do esperado.";
}

function renderPriorityList(elements, items) {
  const priorities = sortSystemsByPriority(items)
    .filter((item) => normalizeStatus(item.status?.status) !== "UP")
    .slice(0, 4);

  if (priorities.length === 0) {
    elements.priorityList.innerHTML = '<p class="empty-block">Nenhuma prioridade no momento.</p>';
    return;
  }

  elements.priorityList.innerHTML = priorities
    .map(({ system, status }) => {
      const normalizedStatus = normalizeStatus(status?.status);
      const label = normalizedStatus === "DOWN"
        ? "Validar incidente"
        : normalizedStatus === "SLOW"
          ? "Acompanhar lentidao"
          : "Completar leitura";

      return `
        <article class="priority-item">
          <span>${escapeHtml(label)}</span>
          <strong>${escapeHtml(system.name)}</strong>
          <small>${escapeHtml(status?.message ?? "Sem leitura consolidada.")}</small>
        </article>
      `;
    })
    .join("");
}

function renderSelectedSystem(elements, systemItem) {
  if (!systemItem) {
    elements.detailTitle.textContent = "Sistema selecionado";
    elements.detailSubtitle.textContent = "Aguardando dados.";
    elements.detailStatus.innerHTML = "-";
    elements.detailTotal.textContent = "0";
    elements.detailBaseUrl.textContent = "-";
    elements.detailLastCheck.textContent = "-";
    elements.endpointsPanelSubtitle.textContent = "Selecione um sistema para ver os endpoints criticos.";
    renderEndpointRows(elements, []);
    return;
  }

  const { system, status } = systemItem;

  elements.detailTitle.textContent = system.name;
  elements.detailSubtitle.textContent = status?.message ?? "Sem leitura consolidada.";
  elements.detailStatus.innerHTML = statusBadge(status?.status);
  elements.detailTotal.textContent = status?.totalEndpoints ?? 0;
  elements.detailBaseUrl.textContent = system.baseUrl || "-";
  elements.detailLastCheck.textContent = formatDate(status?.lastCheckedAt);
  elements.endpointsPanelSubtitle.textContent = `Endpoints criticos vinculados a ${system.name}.`;
}

async function loadEndpointsForSelectedSystem(elements) {
  const selectedSystem = state.systems.find((item) => item.system.id === state.selectedSystemId);

  renderSelectedSystem(elements, selectedSystem);

  if (!selectedSystem) {
    return;
  }

  elements.endpointTableBody.innerHTML = `
    <tr>
      <td colspan="7" class="empty-cell">Carregando endpoints do sistema...</td>
    </tr>
  `;

  const apis = await fetchJson(`/api/monitored-systems/${selectedSystem.system.id}/apis`);
  const endpoints = await Promise.all(
    apis.map(async (api) => ({
      api,
      status: await fetchNullable(`/api/monitored-apis/${api.id}/status`),
    }))
  );

  renderEndpointRows(elements, endpoints);
}

function selectDefaultSystem(items) {
  if (items.length === 0) {
    state.selectedSystemId = null;
    return;
  }

  const stillExists = items.some((item) => item.system.id === state.selectedSystemId);

  if (!stillExists) {
    state.selectedSystemId = sortSystemsByPriority(items)[0].system.id;
  }
}

function renderDashboardError(elements) {
  elements.systemTableBody.innerHTML = `
    <tr>
      <td colspan="6" class="empty-cell">Nao foi possivel conectar ao backend.</td>
    </tr>
  `;
  elements.priorityList.innerHTML = '<p class="empty-block">Sem conexao com a API.</p>';
  elements.healthPill.className = "health-pill health-danger";
  elements.healthPill.textContent = "Offline";
  elements.statusStripTitle.textContent = "Backend indisponivel";
  elements.statusStripMessage.textContent = "Nao foi possivel carregar os sistemas monitorados.";
  elements.systemsPanelSubtitle.textContent = "Verifique se o container da API esta ativo.";
  renderSelectedSystem(elements, null);
  elements.endpointsPanelSubtitle.textContent = "Sem conexao com o backend.";
  elements.endpointTableBody.innerHTML = `
    <tr>
      <td colspan="7" class="empty-cell">Aguardando backend.</td>
    </tr>
  `;
}

function endpointErrorRow() {
  return `
    <tr>
      <td colspan="7" class="empty-cell">Nao foi possivel carregar os endpoints deste sistema.</td>
    </tr>
  `;
}
