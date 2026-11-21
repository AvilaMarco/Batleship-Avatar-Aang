import type { Nation } from "../api/types";

export interface MapLocation { id: string; nation: Nation; x: number; y: number; r: number }

export const MAP_LOCATIONS: MapLocation[] = [
  { id: "W1", nation: "WATER", x: 635, y: 85, r: 30 },
  { id: "W2", nation: "WATER", x: 515, y: 707.5, r: 30 },
  { id: "E1", nation: "EARTH", x: 410, y: 292, r: 30 },
  { id: "E2", nation: "EARTH", x: 657.5, y: 225, r: 30 },
  { id: "E3", nation: "EARTH", x: 720, y: 360, r: 30 },
  { id: "E4", nation: "EARTH", x: 775, y: 420, r: 30 },
  { id: "E5", nation: "EARTH", x: 775, y: 565, r: 30 },
  { id: "E6", nation: "EARTH", x: 837.5, y: 515, r: 30 },
  { id: "E7", nation: "EARTH", x: 955, y: 425, r: 30 },
  { id: "E8", nation: "EARTH", x: 1032.5, y: 232.5, r: 30 },
  { id: "E9", nation: "EARTH", x: 1129, y: 307.5, r: 30 },
  { id: "F1", nation: "FIRE", x: 75, y: 350, r: 30 },
  { id: "F2", nation: "FIRE", x: 156, y: 397, r: 30 },
  { id: "F3", nation: "FIRE", x: 180, y: 438, r: 30 },
  { id: "F4", nation: "FIRE", x: 244, y: 378, r: 30 },
  { id: "F5", nation: "FIRE", x: 334, y: 408, r: 30 },
  { id: "F6", nation: "FIRE", x: 492, y: 368, r: 30 },
  { id: "A1", nation: "AIR", x: 203, y: 200, r: 30 },
  { id: "A2", nation: "AIR", x: 522, y: 575, r: 30 },
  { id: "A3", nation: "AIR", x: 802.5, y: 115, r: 30 },
  { id: "A4", nation: "AIR", x: 1230, y: 450, r: 30 },
];
