export function formatDate(value) {
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

export function formatTime(value) {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

export function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

export function normalizeStatus(status) {
  return status || "UNKNOWN";
}

export function statusBadge(status) {
  const normalizedStatus = normalizeStatus(status);
  const className = `badge badge-${normalizedStatus.toLowerCase()}`;

  return `<span class="${className}">${normalizedStatus}</span>`;
}

export function statusWeight(status) {
  const normalizedStatus = normalizeStatus(status);

  if (normalizedStatus === "DOWN") {
    return 1;
  }

  if (normalizedStatus === "SLOW") {
    return 2;
  }

  if (normalizedStatus === "UNKNOWN") {
    return 3;
  }

  return 4;
}
