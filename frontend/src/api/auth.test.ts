import { afterEach, describe, expect, it, vi } from "vitest";

afterEach(() => {
  vi.unstubAllEnvs();
  vi.resetModules();
  sessionStorage.clear();
  localStorage.clear();
});

describe("auth", () => {
  it("en DEV usa sessionStorage", async () => {
    vi.stubEnv("DEV", true);
    const a = await import("./auth");
    a.setToken("Bearer x");
    expect(sessionStorage.getItem("shipbender.token")).toBe("Bearer x");
    expect(localStorage.getItem("shipbender.token")).toBeNull();
    expect(a.getToken()).toBe("Bearer x");
    a.clearToken();
    expect(a.getToken()).toBeNull();
  });

  it("en prod usa localStorage", async () => {
    vi.stubEnv("DEV", false);
    const a = await import("./auth");
    a.setToken("Bearer y");
    expect(localStorage.getItem("shipbender.token")).toBe("Bearer y");
    expect(sessionStorage.getItem("shipbender.token")).toBeNull();
  });
});
