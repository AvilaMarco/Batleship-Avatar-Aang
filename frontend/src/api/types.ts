// Tipos del contrato ADR 004 (Anexos A.3, B, C, F). snake_case tal cual viaja.

export type Nation = "WATER" | "AIR" | "FIRE" | "EARTH";
export type GameStatus = "CREATED" | "IN_GAME" | "FINISH";
export type ShipType = "CARRIER" | "BATTLESHIP" | "SUBMARINE" | "DESTROYER" | "PATROL_BOAT";

export interface Stats {
  score: number;
  won: number;
  tied: number;
  lost: number;
  win_rate: number;
}

export interface PlayerProfile {
  id: number;
  name: string;
  email: string;
  nation: Nation | null;
  stats: Stats;
}

export interface PlayerSummary {
  id: number;
  name: string;
  nation: Nation | null;
}

export interface RankingEntry {
  player: PlayerSummary;
  stats: Stats;
}

export interface GameSummary {
  id: number;
  nation: Nation;
  location: string;
  status: GameStatus;
  players: PlayerSummary[];
}

export interface ShipPlacement {
  type: ShipType;
  cells: string[];
}

// Anexo F (v2.1)
export interface SalvoRecord {
  turn: number;
  cells: string[];
  hits: string[];
}

export interface MySide {
  player: PlayerSummary;
  ships_placed: boolean;
  ships: ShipPlacement[];
  salvos: SalvoRecord[];
  sunk: ShipType[];
}

// El rival nunca lleva ships.
export interface OpponentSide {
  player: PlayerSummary;
  ships_placed: boolean;
  salvos: SalvoRecord[];
  sunk: ShipType[];
}

export interface GameView {
  id: number;
  nation: Nation;
  location: string;
  status: GameStatus;
  turn: number;
  me: MySide;
  opponent: OpponentSide | null;
}

export interface LoginResponse {
  email: string;
  token: string; // "Bearer eyJ..."
}

export interface PlayerResult {
  player_id: number;
  hits: string[];
  misses: string[];
  sunk: ShipType[];
}

export type GameEvent =
  | { type: "PLAYER_JOINED"; game_id: number; player: PlayerSummary }
  | { type: "SHIPS_PLACED"; game_id: number; player_id: number }
  | { type: "GAME_STARTED"; game_id: number; turn: number }
  | { type: "EMOTE"; game_id: number; player_id: number; emote: string }
  | { type: "SALVO_FIRED"; game_id: number; player_id: number; turn: number }
  | { type: "TURN_RESOLVED"; game_id: number; turn: number; results: PlayerResult[] }
  | { type: "GAME_FINISHED"; game_id: number; winner_id: number | null; scores: Record<string, number> };

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  code: string;
  detail: string;
  instance: string;
  errors?: Record<string, string[]>;
}
