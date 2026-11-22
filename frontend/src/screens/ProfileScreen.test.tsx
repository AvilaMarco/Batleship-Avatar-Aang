import { cleanup, render } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import ProfileScreen from "./ProfileScreen";

// Contrato inventado (tarea 815): ProfileScreen sin props, lee useAuth().profile (getMe).
vi.mock("../auth/AuthContext", () => ({
  useAuth: () => ({
    profile: { id: 1, name: "Aang", email: "a@a.com", nation: "AIR", stats: { score: 30, won: 3, tied: 1, lost: 2, win_rate: 0.5 } },
  }),
}));
afterEach(cleanup);

it("el perfil lleva la nación del jugador en data-nation", () => {
  const { container } = render(<ProfileScreen />);
  expect(container.querySelector(".profile")).toHaveAttribute("data-nation", "air");
});

it("la cabecera muestra el emblema de la nación y el nombre", () => {
  const { container } = render(<ProfileScreen />);
  const head = container.querySelector(".profile-head")!;
  expect(head.querySelector("use")).toHaveAttribute("href", "/assets/sprite.svg#emb-air");
  expect(head).toHaveTextContent("Aang");
});

it("las estadísticas son una dl con partidas (suma), victorias y derrotas", () => {
  const { container } = render(<ProfileScreen />);
  const dl = container.querySelector("dl.profile-stats")!;
  const rows = [...dl.querySelectorAll(":scope > div")].map((d) => [d.querySelector("dt")?.textContent, d.querySelector("dd")?.textContent]);
  expect(rows).toEqual([["Partidas", "6"], ["Victorias", "3"], ["Derrotas", "2"]]);
});
