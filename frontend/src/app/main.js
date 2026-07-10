const tableBody = document.querySelector("#api-table-body");
const panelSubtitle = document.querySelector("#panel-subtitle");
const refreshButton = document.querySelector("#refresh-button");

const metrics = {
  total: document.querySelector("#metric-total"),
  up: document.querySelector("#metric-up"),
  slow: document.querySelector("#metric-slow"),
  down: document.querySelector("#metric-down"),
};

async function fetchJson(url) {
  const response = await fetch(url);

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}

async function getCurrentStatus(apiId) {
  try {
    return await fetchJson(`/api/monitored-apis/${apiId}/status`);
  } catch (error) {
    return null;
  }
}

function formatDate(value) {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

function statusBadge(status) {
  const normalizedStatus = status || "SEM DADOS";
  const className = status ? `badge badge-${status.toLowerCase()}` : "badge badge-muted";

  return `<span class="${className}">${normalizedStatus}</span>`;
}

function renderRows(items) {
  if (items.length === 0) {
    tableBody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-cell">Nenhuma API monitorada cadastrada.</td>
      </tr>
    `;
    return;
  }

  tableBody.innerHTML = items
    .map(({ api, status }) => {
      return `
        <tr>
          <td>
            <strong>${api.name}</strong>
            <span class="muted">#${api.id}</span>
          </td>
          <td class="url-cell">${api.url}</td>
          <td>${statusBadge(status?.status)}</td>
          <td>${status?.statusCode ?? "-"}</td>
          <td>${status?.responseTimeMs ? `${status.responseTimeMs}ms` : "-"}</td>
          <td>${formatDate(status?.checkedAt)}</td>
          <td>${api.active ? "Sim" : "Nao"}</td>
        </tr>
      `;
    })
    .join("");
}

function updateMetrics(items) {
  const totals = items.reduce(
    (accumulator, item) => {
      const status = item.status?.status;

      accumulator.total += 1;

      if (status === "UP") {
        accumulator.up += 1;
      }

      if (status === "SLOW") {
        accumulator.slow += 1;
      }

      if (status === "DOWN") {
        accumulator.down += 1;
      }

      return accumulator;
    },
    { total: 0, up: 0, slow: 0, down: 0 }
  );

  metrics.total.textContent = totals.total;
  metrics.up.textContent = totals.up;
  metrics.slow.textContent = totals.slow;
  metrics.down.textContent = totals.down;
}

async function loadDashboard() {
  refreshButton.disabled = true;
  panelSubtitle.textContent = "Carregando dados do backend...";

  try {
    const apis = await fetchJson("/api/monitored-apis");
    const items = await Promise.all(
      apis.map(async (api) => ({
        api,
        status: await getCurrentStatus(api.id),
      }))
    );

    renderRows(items);
    updateMetrics(items);
    panelSubtitle.textContent = `${items.length} API(s) monitorada(s) encontradas.`;
  } catch (error) {
    tableBody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-cell">Nao foi possivel conectar ao backend.</td>
      </tr>
    `;
    panelSubtitle.textContent = "Verifique se o container da API esta ativo.";
  } finally {
    refreshButton.disabled = false;
  }
}

refreshButton.addEventListener("click", loadDashboard);
loadDashboard();
