import type { Nation } from "../api/types";

const NATIONS: { id: Nation; label: string; img?: string }[] = [
  { id: "WATER", label: "Agua", img: "card-agua.png" },
  { id: "EARTH", label: "Tierra", img: "card-tierra.png" },
  { id: "FIRE", label: "Fuego", img: "card-fuego.png" },
  { id: "AIR", label: "Aire", img: "card-viento.png" },
  { id: "LOTUS", label: "Loto Blanco" }, // sin carta ilustrada: emblema del sprite
];

export default function NationPicker({ onPick, disabled }: { onPick: (n: Nation) => void; disabled?: boolean }) {
  return (
    <section aria-labelledby="nation-title">
      <h2 id="nation-title">Elige tu nación</h2>
      {NATIONS.map((n) => (
        <button key={n.id} type="button" disabled={disabled} onClick={() => onPick(n.id)} aria-label={n.label}>
          {n.img ? <img src={`/assets/img/${n.img}`} alt="" width={120} /> : (
            <svg viewBox="0 0 48 48" aria-hidden="true" width="100%" height="100%" data-nation="lotus"><use href="/assets/sprite.svg#emb-lotus" /></svg>
          )}
        </button>
      ))}
    </section>
  );
}
