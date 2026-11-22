import { useEffect, useRef, useState, type FormEvent } from "react";
import { ApiError } from "../api/http";
import { useAuth } from "../auth/AuthContext";

// Orden = cues de intro.vtt (agua, tierra, fuego, aire); mobileN.png = silueta de cada nación
const NATIONS = [
  { key: "water", label: "Agua", img: "/assets/login/mobile1.png" },
  { key: "earth", label: "Tierra", img: "/assets/login/mobile2.png" },
  { key: "fire", label: "Fuego", img: "/assets/login/mobile3.png" },
  { key: "air", label: "Aire", img: "/assets/login/mobile4.png" },
];

const FIELDS: Record<string, string> = { password: "Contraseña", email: "Email", name: "Nombre" };

function messages(e: unknown): string[] {
  if (!(e instanceof ApiError)) return ["No se pudo conectar con el servidor."];
  if (e.code === "INVALID_CREDENTIALS") return ["Credenciales incorrectas."];
  if (e.code === "EMAIL_IN_USE") return ["Ese email ya está registrado."];
  if (e.code === "VALIDATION_FAILED" && e.errors)
    return Object.entries(e.errors).flatMap(([f, ms]) => ms.map((m) => `${FIELDS[f] ?? f}: ${m}`));
  return ["No se pudo completar la operación."];
}

export default function LoginScreen() {
  const { login, register } = useAuth();
  const [registering, setRegistering] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<string[]>([]);
  const [busy, setBusy] = useState(false);
  const [active, setActive] = useState(0);
  const [muted, setMuted] = useState(false);
  const audio = useRef<HTMLAudioElement>(null);

  // Autoplay suele estar bloqueado: se arranca al primer toque.
  useEffect(() => {
    const start = () => void audio.current?.play?.()?.catch?.(() => {});
    window.addEventListener("pointerdown", start, { once: true });
    return () => window.removeEventListener("pointerdown", start);
  }, []);

  // La pista VTT (mode hidden: sin subtítulos) marca qué nación suena; mismo estado que las miniaturas.
  const onCue = (t: TextTrack) => {
    const cue = (t.activeCues?.[0] as VTTCue | undefined)?.text;
    const i = NATIONS.findIndex((n) => n.key === cue);
    if (i >= 0) setActive(i);
  };

  async function submit(ev: FormEvent) {
    ev.preventDefault();
    setErrors([]);
    setBusy(true);
    try {
      await (registering ? register(name, email, password) : login(email, password));
    } catch (e) {
      setErrors(messages(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login">
      <img className="login-silhouette" src={NATIONS[active].img} alt="" aria-hidden="true" data-nation={NATIONS[active].key} />
      <audio ref={audio} src="/assets/login/intro.ogg" muted={muted} loop preload="auto">
        <track kind="metadata" src="/assets/login/intro.vtt" default
          onLoad={(e) => { if (e.currentTarget.track) e.currentTarget.track.mode = "hidden"; }}
          ref={(t) => { if (t?.track) t.track.oncuechange = () => onCue(t.track); }} />
      </audio>
      <button type="button" className="login-mute" aria-pressed={muted} onClick={() => setMuted(!muted)}>Silenciar</button>
    <form onSubmit={submit} aria-label={registering ? "Crear cuenta" : "Iniciar sesión"}>
      {registering && (
        <p>
          <label htmlFor="name">Nombre</label>
          <input id="name" value={name} onChange={(e) => setName(e.target.value)} required autoComplete="nickname" />
        </p>
      )}
      <p>
        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
      </p>
      <p>
        <label htmlFor="password">Contraseña</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          {...(registering && { minLength: 8, maxLength: 72, "aria-describedby": "password-help" })}
          autoComplete={registering ? "new-password" : "current-password"}
        />
        {registering && <small id="password-help">Mínimo 8 caracteres</small>}
      </p>
      {errors.length > 0 && (
        <div role="alert">
          {errors.map((m) => (
            <p key={m}>{m}</p>
          ))}
        </div>
      )}
      <button type="submit" disabled={busy}>{registering ? "Crear cuenta" : "Entrar"}</button>
      <button type="button" onClick={() => { setRegistering(!registering); setErrors([]); }}>
        {registering ? "Ya tengo cuenta" : "Registrarse"}
      </button>
    </form>
      <div className="login-thumbs">
        {NATIONS.map((n, i) => (
          <button key={n.key} type="button" aria-label={n.label} aria-current={i === active ? "true" : undefined} onClick={() => setActive(i)}>
            <img src={n.img} alt="" />
          </button>
        ))}
      </div>
    </div>
  );
}
