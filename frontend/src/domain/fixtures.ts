import type { GameView, MySide, OpponentSide } from "../api/types";

const base = { player: { id: 1, name: "p", nation: null }, ships_placed: true, salvos: [], sunk: [] };
type V = Partial<Omit<GameView, "me" | "opponent">> & { me?: Partial<MySide>; opponent?: Partial<OpponentSide> | null };
export const view = (o: V = {}): GameView => ({
  id: 1, nation: "FIRE", location: "F1", status: "IN_GAME", turn: 1,
  ...o,
  me: { ...base, ships: [], ...o.me },
  opponent: o.opponent === null ? null : { ...base, ...o.opponent },
});
