import { fetchJson, postJson } from "../../shared/api/http.js";
import { escapeHtml, formatDate } from "../../shared/ui/formatters.js";

export function renderRegisterPage(root) {
  root.innerHTML = registerTemplate();

  const elements = getRegisterElements(root);

  elements.reloadButton.addEventListener("click", () => loadRegisterData(elements));
  elements.systemForm.addEventListener("submit", (event) => handleSystemSubmit(event, elements));
  elements.endpointForm.addEventListener("submit", (event) => handleEndpointSubmit(event, elements));

  loadRegisterData(elements);
}

function registerTemplate() {
  return `
    <section class="app-view active-view">
      <header class="topbar">
        <div>
          <p class="eyebrow">Cadastro operacional</p>
          <h1>Novo sistema monitorado</h1>
          <p class="page-summary">Cadastre o sistema principal e vincule os endpoints criticos que precisam entrar no monitoramento.</p>
        </div>
        <div class="topbar-actions">
          <button class="ghost-button" id="reload-register-button" type="button">Recarregar listas</button>
        </div>
      </header>

      <div class="register-grid">
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>Sistema</h2>
              <p>Base que agrupa os endpoints monitorados.</p>
            </div>
          </div>

          <form class="form-stack" id="system-form">
            <label class="field-group">
              <span>Nome do sistema</span>
              <input id="system-name" name="name" type="text" placeholder="Sistema Financeiro" required />
            </label>

            <label class="field-group">
              <span>Base URL</span>
              <input id="system-base-url" name="baseUrl" type="url" placeholder="https://financeiro.empresa.com" required />
            </label>

            <label class="field-group">
              <span>Descricao</span>
              <textarea id="system-description" name="description" rows="4" placeholder="Sistema responsavel por pagamentos e conciliacao"></textarea>
            </label>

            <div class="form-actions">
              <button class="primary-button" id="save-system-button" type="submit">Cadastrar sistema</button>
              <p class="form-message" id="system-form-message" role="status"></p>
            </div>
          </form>
        </section>

        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>Endpoint critico</h2>
              <p>URL que representa uma funcionalidade importante do sistema.</p>
            </div>
          </div>

          <form class="form-stack" id="endpoint-form">
            <label class="field-group">
              <span>Sistema</span>
              <select id="endpoint-system-id" name="systemId" required>
                <option value="">Carregando sistemas...</option>
              </select>
            </label>

            <label class="field-group">
              <span>Nome do endpoint</span>
              <input id="endpoint-name" name="name" type="text" placeholder="Login" required />
            </label>

            <label class="field-group">
              <span>URL do endpoint</span>
              <input id="endpoint-url" name="url" type="url" placeholder="https://financeiro.empresa.com/api/auth/status" required />
            </label>

            <label class="field-group">
              <span>Descricao</span>
              <textarea id="endpoint-description" name="description" rows="4" placeholder="Endpoint critico de login"></textarea>
            </label>

            <div class="form-actions">
              <button class="primary-button" id="save-endpoint-button" type="submit">Cadastrar endpoint</button>
              <p class="form-message" id="endpoint-form-message" role="status"></p>
            </div>
          </form>
        </section>
      </div>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>Sistemas cadastrados</h2>
            <p id="register-systems-subtitle">Lista usada para vincular novos endpoints.</p>
          </div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Sistema</th>
                <th>Base URL</th>
                <th>Ativo</th>
                <th>Criado em</th>
              </tr>
            </thead>
            <tbody id="register-systems-body">
              <tr>
                <td colspan="4" class="empty-cell">Carregando sistemas...</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  `;
}

function getRegisterElements(root) {
  return {
    reloadButton: root.querySelector("#reload-register-button"),
    systemForm: root.querySelector("#system-form"),
    endpointForm: root.querySelector("#endpoint-form"),
    endpointSystemSelect: root.querySelector("#endpoint-system-id"),
    registerSystemsBody: root.querySelector("#register-systems-body"),
    registerSystemsSubtitle: root.querySelector("#register-systems-subtitle"),
    systemFormMessage: root.querySelector("#system-form-message"),
    endpointFormMessage: root.querySelector("#endpoint-form-message"),
    saveSystemButton: root.querySelector("#save-system-button"),
    saveEndpointButton: root.querySelector("#save-endpoint-button"),
  };
}

async function loadRegisterData(elements, selectedSystemId = elements.endpointSystemSelect.value) {
  elements.registerSystemsSubtitle.textContent = "Carregando sistemas...";

  try {
    const systems = await fetchJson("/api/monitored-systems");

    renderRegisterSystems(elements, systems);
    populateEndpointSystemSelect(elements, systems, selectedSystemId);
  } catch (error) {
    elements.registerSystemsBody.innerHTML = `
      <tr>
        <td colspan="4" class="empty-cell">Nao foi possivel carregar os sistemas.</td>
      </tr>
    `;
    elements.endpointSystemSelect.innerHTML = '<option value="">Backend indisponivel</option>';
    elements.endpointSystemSelect.disabled = true;
    elements.saveEndpointButton.disabled = true;
    elements.registerSystemsSubtitle.textContent = "Verifique se o backend esta ativo.";
  }
}

async function handleSystemSubmit(event, elements) {
  event.preventDefault();
  elements.saveSystemButton.disabled = true;
  setFormMessage(elements.systemFormMessage, "Salvando sistema...", "neutral");

  try {
    const createdSystem = await postJson("/api/monitored-systems", {
      name: getInputValue(elements.systemForm, "name"),
      baseUrl: getInputValue(elements.systemForm, "baseUrl"),
      description: getInputValue(elements.systemForm, "description"),
    });

    elements.systemForm.reset();
    setFormMessage(elements.systemFormMessage, "Sistema cadastrado com sucesso.", "success");
    await loadRegisterData(elements, createdSystem.id);
  } catch (error) {
    setFormMessage(elements.systemFormMessage, `Erro ao cadastrar sistema: ${error.message}`, "error");
  } finally {
    elements.saveSystemButton.disabled = false;
  }
}

async function handleEndpointSubmit(event, elements) {
  event.preventDefault();
  elements.saveEndpointButton.disabled = true;
  setFormMessage(elements.endpointFormMessage, "Salvando endpoint...", "neutral");

  const systemId = getInputValue(elements.endpointForm, "systemId");

  try {
    await postJson(`/api/monitored-systems/${systemId}/apis`, {
      name: getInputValue(elements.endpointForm, "name"),
      url: getInputValue(elements.endpointForm, "url"),
      description: getInputValue(elements.endpointForm, "description"),
    });

    elements.endpointForm.reset();
    elements.endpointSystemSelect.value = systemId;
    setFormMessage(elements.endpointFormMessage, "Endpoint cadastrado com sucesso.", "success");
    await loadRegisterData(elements, systemId);
  } catch (error) {
    setFormMessage(elements.endpointFormMessage, `Erro ao cadastrar endpoint: ${error.message}`, "error");
  } finally {
    elements.saveEndpointButton.disabled = false;
  }
}

function renderRegisterSystems(elements, systems) {
  if (systems.length === 0) {
    elements.registerSystemsBody.innerHTML = `
      <tr>
        <td colspan="4" class="empty-cell">Nenhum sistema cadastrado ainda.</td>
      </tr>
    `;
    elements.registerSystemsSubtitle.textContent = "Crie o primeiro sistema para liberar o cadastro de endpoints.";
    return;
  }

  elements.registerSystemsBody.innerHTML = systems
    .map((system) => {
      return `
        <tr>
          <td>
            <strong>${escapeHtml(system.name)}</strong>
            <span class="muted">#${escapeHtml(system.id)}</span>
          </td>
          <td class="url-cell">${escapeHtml(system.baseUrl)}</td>
          <td>${system.active ? "Sim" : "Nao"}</td>
          <td>${formatDate(system.createdAt)}</td>
        </tr>
      `;
    })
    .join("");
  elements.registerSystemsSubtitle.textContent = `${systems.length} sistema(s) disponivel(is) para vinculo.`;
}

function populateEndpointSystemSelect(elements, systems, selectedId) {
  if (systems.length === 0) {
    elements.endpointSystemSelect.innerHTML = '<option value="">Cadastre um sistema primeiro</option>';
    elements.endpointSystemSelect.disabled = true;
    elements.saveEndpointButton.disabled = true;
    return;
  }

  elements.endpointSystemSelect.disabled = false;
  elements.saveEndpointButton.disabled = false;
  elements.endpointSystemSelect.innerHTML = systems
    .map((system) => {
      const selected = Number(selectedId) === system.id ? "selected" : "";

      return `<option value="${escapeHtml(system.id)}" ${selected}>${escapeHtml(system.name)}</option>`;
    })
    .join("");
}

function getInputValue(form, name) {
  return new FormData(form).get(name)?.toString().trim() ?? "";
}

function setFormMessage(element, message, type) {
  element.className = `form-message ${type ? `form-message-${type}` : ""}`.trim();
  element.textContent = message;
}
