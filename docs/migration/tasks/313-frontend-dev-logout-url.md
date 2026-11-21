# 313 · frontend-dev · Corregir la URL de logout del front viejo
Estado: hecha
Fase: F3
Depende de: 311

Objetivo: `logoutFunction` llama a `/api/logout`, que no existe; el backend atiende `POST /api/players/logout` (`WebSecurityConfig`, `LogoutFilter`). Antes daba 404; con deny-by-default (311) da 403. Lo descubrió la 311.

Archivos: `src/main/resources/static/web/js/games-mjs/player.js` (línea 14).

Cambio: `fetch("/api/logout", ...)` → `fetch("/api/players/logout", ...)`. Nada más.

Criterios de aceptación:
- `grep -rn "/api/logout" src/main/resources/static` → sin resultados.
- `grep -rn "/api/players/logout" src/main/resources/static` → 1 línea.

## Resultado
Cambiada la URL en `player.js:14` a `/api/players/logout`. Los dos grep de los criterios dan lo esperado (0 y 1 línea). No se probó en navegador.
