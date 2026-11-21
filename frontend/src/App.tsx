import { useEffect } from "react";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { navigate, useHashRoute } from "./router";
import LoginScreen from "./screens/LoginScreen";

function Routes() {
  const route = useHashRoute();
  const { profile, ready, logout } = useAuth();
  const needsLogin = ready && !profile && route.name !== "login";

  useEffect(() => {
    if (needsLogin) navigate("#/login");
  }, [needsLogin]);

  if (!ready || needsLogin) return <p>Cargando…</p>;
  if (route.name === "login") return profile ? <p>Ya has iniciado sesión.</p> : <LoginScreen />;
  return (
    <>
      <button type="button" onClick={logout}>Salir</button>
      {/* placeholders hasta 505/506 */}
      {route.name === "lobby" ? <p>Lobby</p> : <p>Partida {route.id}</p>}
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <h1>The Last Shipbender</h1>
      <main><Routes /></main>
    </AuthProvider>
  );
}
