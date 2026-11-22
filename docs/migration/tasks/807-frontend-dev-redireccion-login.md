# 807 · frontend-dev · Redirigir al menú si ya hay sesión
Estado: hecha
Fase: F8
Depende de: 806
Paralelizable: no
Origen: "crea una tarea para el redireccionamiento, si un usuario ya esta logeado tiene que ir al menu principal"
Riesgo: normal

Objetivo: si hay perfil y la ruta es `#/login`, navegar a `#/` en vez de mostrar "Ya has iniciado sesión." (hoy en `frontend/src/App.tsx`, `Routes`). Es el espejo del `needsLogin` existente: un `useEffect` que llama a `navigate("#/")`.

Archivos: `frontend/src/App.tsx` (y `screens/LoginScreen.tsx` solo si el login correcto no navega ya a `#/`). Sin CSS (sesión de diseño).

Criterios de aceptación:
- `cd frontend && npm test && npm run build` → exit 0, tests de 806 en verde.
- Sin parpadeo: mientras `!ready` se sigue mostrando "Cargando…".
- Prueba manual (la hace el chat central): con sesión, abrir `#/login` o recargar en él → termina en el menú principal; cerrar sesión → vuelve a `#/login`.
- Commit `fix(front): con sesión iniciada #/login redirige al menú (tarea 807)`.

## Resultado
`App.tsx`: `loggedAtLogin` (ready + perfil + ruta login) navega a `#/` en el mismo `useEffect` que `needsLogin`; mientras tanto muestra "Cargando…". Prueba manual pendiente (chat central).
