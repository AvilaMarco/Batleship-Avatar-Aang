import { describe, expect, it } from "vitest";
import { outcome, phase } from "./phase";
import { view } from "./fixtures";

const five = ["CARRIER", "BATTLESHIP", "SUBMARINE", "DESTROYER", "PATROL_BOAT"] as const;

describe("phase", () => {
  it("FINISHED", () => expect(phase(view({ status: "FINISH" }))).toBe("FINISHED"));
  it("FINISHED gana a opponent null", () => expect(phase(view({ status: "FINISH", opponent: null }))).toBe("FINISHED"));
  it("WAITING_OPPONENT", () => expect(phase(view({ status: "CREATED", opponent: null }))).toBe("WAITING_OPPONENT"));
  it("PLACE_SHIPS", () => expect(phase(view({ status: "CREATED", me: { ships_placed: false } }))).toBe("PLACE_SHIPS"));
  it("WAITING_OPPONENT_SHIPS", () =>
    expect(phase(view({ status: "CREATED", opponent: { ships_placed: false } }))).toBe("WAITING_OPPONENT_SHIPS"));
  it("WAITING_OPPONENT_SALVO", () =>
    expect(phase(view({ me: { salvos: [{ turn: 1, cells: ["A1"], hits: [] }] } }))).toBe("WAITING_OPPONENT_SALVO"));
  it("FIRE", () => expect(phase(view())).toBe("FIRE"));
  it("FIRE con salvos de turnos anteriores", () =>
    expect(phase(view({ turn: 2, me: { salvos: [{ turn: 1, cells: ["A1"], hits: [] }] } }))).toBe("FIRE"));
});

describe("outcome", () => {
  it("null si no FINISH", () => expect(outcome(view())).toBeNull());
  it("WON", () => expect(outcome(view({ status: "FINISH", opponent: { sunk: [...five] }, me: { sunk: ["CARRIER"] } }))).toBe("WON"));
  it("LOST", () => expect(outcome(view({ status: "FINISH", me: { sunk: [...five] }, opponent: { sunk: ["CARRIER"] } }))).toBe("LOST"));
  it("TIED", () => expect(outcome(view({ status: "FINISH", me: { sunk: [...five] }, opponent: { sunk: [...five] } }))).toBe("TIED"));
});
