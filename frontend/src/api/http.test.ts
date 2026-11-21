import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { getToken, setToken } from "./auth";
import { ApiError, request } from "./http";

const problem = (status: number, code: string) =>
  new Response(JSON.stringify({ type: "about:blank", title: "t", status, code, detail: "d", instance: "/x" }), {
    status,
    headers: { "Content-Type": "application/problem+json" },
  });

let fetchMock: ReturnType<typeof vi.fn>;
beforeEach(() => {
  fetchMock = vi.fn();
  vi.stubGlobal("fetch", fetchMock);
  sessionStorage.clear();
  localStorage.clear();
  location.hash = "";
});
afterEach(() => vi.unstubAllGlobals());

describe("request", () => {
  it("manda Authorization con el token guardado y Content-Type si hay body", async () => {
    setToken("Bearer abc");
    fetchMock.mockResolvedValue(new Response("{}", { status: 200 }));
    await request("POST", "/api/games", { a: 1 });
    const [path, init] = fetchMock.mock.calls[0];
    expect(path).toBe("/api/games");
    expect(init.headers.Authorization).toBe("Bearer abc");
    expect(init.headers["Content-Type"]).toBe("application/json");
    expect(init.body).toBe('{"a":1}');
  });

  it("sin token ni body no manda esos headers", async () => {
    fetchMock.mockResolvedValue(new Response("{}", { status: 200 }));
    await request("GET", "/api/games");
    const h = fetchMock.mock.calls[0][1].headers;
    expect(h.Authorization).toBeUndefined();
    expect(h["Content-Type"]).toBeUndefined();
  });

  it("409 problem+json lanza ApiError con code", async () => {
    fetchMock.mockResolvedValue(problem(409, "GAME_FULL"));
    const err = (await request("POST", "/api/games/1/players").catch((e: unknown) => e)) as ApiError;
    expect(err).toBeInstanceOf(ApiError);
    expect(err.status).toBe(409);
    expect(err.code).toBe("GAME_FULL");
  });

  it("401 borra el token y redirige a #/login", async () => {
    setToken("Bearer abc");
    fetchMock.mockResolvedValue(problem(401, "UNAUTHENTICATED"));
    await expect(request("GET", "/api/players/me")).rejects.toBeInstanceOf(ApiError);
    expect(getToken()).toBeNull();
    expect(location.hash).toBe("#/login");
  });

  it("401 del login no redirige", async () => {
    fetchMock.mockResolvedValue(problem(401, "INVALID_CREDENTIALS"));
    await expect(request("POST", "/api/auth/login", {})).rejects.toMatchObject({ code: "INVALID_CREDENTIALS" });
    expect(location.hash).toBe("");
  });

  it("error sin cuerpo problem+json igual lanza ApiError", async () => {
    fetchMock.mockResolvedValue(new Response("boom", { status: 502 }));
    await expect(request("GET", "/api/games")).rejects.toMatchObject({ status: 502 });
  });
});
