interface ApiErrorBody {
  message?: string;
  error?: string;
}

export class HttpError extends Error {
  constructor(message: string, public readonly status: number) {
    super(message);
    this.name = "HttpError";
  }
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, init);

  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const body = (await response.json()) as ApiErrorBody | string;
      message = typeof body === "string" ? body : body.message ?? body.error ?? message;
    } catch {
      // A resposta sem JSON usa a mensagem HTTP padrao.
    }
    throw new HttpError(message, response.status);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

const jsonHeaders = { "Content-Type": "application/json" };

export const http = {
  get: <T>(url: string) => request<T>(url),
  post: <T>(url: string, body?: unknown) => request<T>(url, {
    method: "POST", headers: jsonHeaders, body: body === undefined ? undefined : JSON.stringify(body),
  }),
  put: <T>(url: string, body: unknown) => request<T>(url, {
    method: "PUT", headers: jsonHeaders, body: JSON.stringify(body),
  }),
  patch: <T>(url: string, body: unknown) => request<T>(url, {
    method: "PATCH", headers: jsonHeaders, body: JSON.stringify(body),
  }),
  delete: (url: string) => request<void>(url, { method: "DELETE" }),
};
