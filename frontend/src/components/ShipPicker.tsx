import type { ShipType } from "../api/types";
import { SHIP_SIZES, type Orientation } from "../domain/fleet";

export const SHIP_NAMES: Record<ShipType, string> = {
  CARRIER: "Portaaviones", BATTLESHIP: "Acorazado", SUBMARINE: "Submarino",
  DESTROYER: "Destructor", PATROL_BOAT: "Patrullero",
};

interface Props {
  placed: ShipType[];
  selected: ShipType | null;
  orientation: Orientation;
  onSelect: (t: ShipType) => void;
  onRotate: () => void;
}

export default function ShipPicker({ placed, selected, orientation, onSelect, onRotate }: Props) {
  return (
    <div>
      {(Object.keys(SHIP_SIZES) as ShipType[]).map((t) => (
        <button key={t} type="button" disabled={placed.includes(t)} aria-pressed={selected === t} onClick={() => onSelect(t)}>
          <img src={`/assets/ships/${t.toLowerCase()}.png`} alt="" height={24} /> {SHIP_NAMES[t]} ({SHIP_SIZES[t]})
        </button>
      ))}
      <button type="button" onClick={onRotate}>Rotar ({orientation === "H" ? "horizontal" : "vertical"})</button>
    </div>
  );
}
