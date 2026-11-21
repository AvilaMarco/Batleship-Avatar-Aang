import { useEffect } from "react";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { navigate, useHashRoute } from "./router";
import LobbyScreen from "./screens/LobbyScreen";
import GameScreen from "./screens/GameScreen";
import LoginScreen from "./screens/LoginScreen";

function Routes() {
  const route = useHashRoute();
  const { profile, ready } = useAuth();
  const needsLogin = ready && !profile && route.name !== "login";

  useEffect(() => {
    if (needsLogin) navigate("#/login");
  }, [needsLogin]);

  if (!ready || needsLogin) return <p>Cargando…</p>;
  if (route.name === "login") return profile ? <p>Ya has iniciado sesión.</p> : <LoginScreen />;
  return route.name === "lobby" ? <LobbyScreen /> : <GameScreen key={route.id} id={route.id} />;
}

export default function App() {
  return (
    <AuthProvider>
      <h1>The Last Shipbender</h1>
      <main><Routes /></main>
    </AuthProvider>
  );
}
