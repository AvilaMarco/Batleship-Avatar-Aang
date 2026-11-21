import { useEffect, useState } from "react";

// Lista cerrada: el backend acepta cualquier texto, así que solo se pinta lo conocido.
export const EMOTES = ["emote-1", "emote-2", "emote-3", "emote-4"] as const;
const src = (e: string) => `/assets/emotes/${e}.png`;
export const isKnownEmote = (e: string): boolean => (EMOTES as readonly string[]).includes(e);

export function EmoteBubble({ emote, name }: { emote: string | null; name: string }) {
  if (!emote || !isKnownEmote(emote)) return null;
  return <img src={src(emote)} width={48} height={48} alt={`Emote ${emote.slice(-1)} de ${name}`} />;
}

// Muestra un emote 3 s; el `key` fuerza reinicio si llega otro.
export function useTransient<T>(ms = 3000) {
  const [v, setV] = useState<T | null>(null);
  useEffect(() => {
    if (v === null) return;
    const t = setTimeout(() => setV(null), ms);
    return () => clearTimeout(t);
  }, [v, ms]);
  return [v, setV] as const;
}

export default function EmoteBar({ onSend }: { onSend: (e: string) => void }) {
  return (
    <div role="group" aria-label="Emotes">
      {EMOTES.map((e) => (
        <button key={e} type="button" className="emote" aria-label={`Emote ${e.slice(-1)}`} onClick={() => onSend(e)}>
          <img src={src(e)} width={40} height={40} alt="" />
        </button>
      ))}
    </div>
  );
}
