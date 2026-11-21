import { clearToken, getToken } from "./auth";
import type { ProblemDetail } from "./types";

export class ApiError extends Error {
  status: number;
  code: string;
  detail: string;
  errors?: Record<string, string[]>;
  constructor(p: Pick<ProblemDetail, "status" | "code" | "detail" | "errors">) {
    super(p.detail || p.code); // nunca incluye headers ni token
    this.name = "ApiError";
    this.status = p.status;
    this.code = p.code;
    this.detail = p.detail;
    this.errors = p.errors;
  }
}

export async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  const token = getToken();
  if (token) headers.Authorization = token;
  if (body !== undefined) headers["Content-Type"] = "application/json";

  const res = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!res.ok) {
    const p = await res.json().catch(() => null);
    if (res.status === 401 && path !== "/api/auth/login") {
      clearToken();
      location.hash = "#/login";
    }
    throw new ApiError({
      status: p?.status ?? res.status,
      code: p?.code ?? "UNKNOWN",
      detail: p?.detail ?? res.statusText,
      errors: p?.errors,
    });
  }
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
