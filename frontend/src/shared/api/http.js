export async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);

  if (!response.ok) {
    throw new Error(await buildErrorMessage(response));
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export async function fetchNullable(url) {
  try {
    return await fetchJson(url);
  } catch (error) {
    return null;
  }
}

export function postJson(url, body) {
  return fetchJson(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
}

async function buildErrorMessage(response) {
  try {
    const body = await response.json();

    if (typeof body === "string") {
      return body;
    }

    if (body.message) {
      return body.message;
    }

    if (body.error) {
      return body.error;
    }
  } catch (error) {
    return `HTTP ${response.status}`;
  }

  return `HTTP ${response.status}`;
}
