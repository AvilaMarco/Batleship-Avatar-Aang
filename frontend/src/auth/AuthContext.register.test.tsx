import { act, cleanup, renderHook } from "@testing-library/react";
import type { ReactNode } from "react";
import { afterEach, expect, it, vi } from "vitest";
import * as players from "../api/players";
import { AuthProvider, useAuth } from "./AuthContext";

vi.mock("../api/players");
afterEach(() => { cleanup(); vi.resetAllMocks(); });
const wrapper = ({ children }: { children: ReactNode }) => <AuthProvider>{children}</AuthProvider>;

it("register persiste la nación elegida tras el login", async () => {
  const { result } = renderHook(() => useAuth(), { wrapper });
  await act(() => result.current.register("A", "a@a.com", "12345678", "AIR"));
  expect(players.setNation).toHaveBeenCalledWith("AIR");
});

it("register sin nación no llama a setNation", async () => {
  const { result } = renderHook(() => useAuth(), { wrapper });
  await act(() => result.current.register("A", "a@a.com", "12345678"));
  expect(players.setNation).not.toHaveBeenCalled();
});
