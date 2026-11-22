import { cleanup, render } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import GameMap from "./GameMap";

afterEach(cleanup);

it("el mapa se escala en modo cover y los marcadores se contraescalan con --s", () => {
  class RO { cb: () => void; constructor(cb: () => void) { this.cb = cb; } observe() { this.cb(); } disconnect() {} }
  vi.stubGlobal("ResizeObserver", RO);
  vi.spyOn(HTMLElement.prototype, "clientWidth", "get").mockReturnValue(700);
  vi.spyOn(HTMLElement.prototype, "clientHeight", "get").mockReturnValue(300);
  const { container } = render(<GameMap games={[]} myId={1} nation={null} onCreate={vi.fn()} onJoin={vi.fn()} onOpen={vi.fn()} />);
  const svg = container.querySelector("svg")!;
  // cover: max(700/1400, 300/800) = 0.5
  expect(svg.getAttribute("width")).toBe("700");
  expect(svg.getAttribute("height")).toBe("400");
  expect(svg.style.getPropertyValue("--s")).toBe("2");
  vi.restoreAllMocks(); vi.unstubAllGlobals();
});
