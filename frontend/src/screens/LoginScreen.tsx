import { useState, type FormEvent } from "react";
import { ApiError } from "../api/http";
import { useAuth } from "../auth/AuthContext";

function messages(e: unknown): string[] {
  if (!(e instanceof ApiError)) return ["No se pudo conectar con el servidor."];
  if (e.code === "INVALID_CREDENTIALS") return ["Credenciales incorrectas."];
  if (e.code === "EMAIL_IN_USE") return ["Ese email ya está registrado."];
  if (e.code === "VALIDATION_FAILED" && e.errors)
    return Object.entries(e.errors).flatMap(([f, ms]) => ms.map((m) => `${f}: ${m}`));
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
    <form onSubmit={submit} aria-labelledby="login-title">
      <h2 id="login-title">{registering ? "Crear cuenta" : "Iniciar sesión"}</h2>
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
          autoComplete={registering ? "new-password" : "current-password"}
        />
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
  );
}
