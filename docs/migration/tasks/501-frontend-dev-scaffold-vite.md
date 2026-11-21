# 501 · frontend-dev · Esqueleto de `/frontend` (Vite + React + TS + Vitest + proxy)
Estado: pendiente
Fase: F5
Depende de: —
Paralelizable: no (todo F5 depende de esta)
Origen: PLAN.md F5 "Partida completa jugable en React + TS"; ADR 006 puntos 1, 6, 7 y Anexos A y C.
Riesgo: normal

Objetivo: proyecto Vite vacío pero funcional, con tests y proxy al backend, y los assets copiados.

Archivos: `frontend/` nuevo (`npm create vite@latest frontend -- --template react-ts`), `frontend/vite.config.ts`, `frontend/package.json`, `frontend/src/{main.tsx,App.tsx,App.test.tsx}`, `frontend/public/assets/{emotes,img,ships,icons}/` (copiar, no mover, desde `src/main/resources/static/web/assets/`; `texturas`, `wallpapers` y `main.ogg` no).

Diseño (cerrado):
- Borrar del template lo de demo (logo de Vite, contador, `App.css` de ejemplo). TS `strict`. ESLint del template tal cual.
- Dev deps: `vitest`, `jsdom`, `@testing-library/react`, `@testing-library/jest-dom`. Config de test dentro de `vite.config.ts` (`environment: "jsdom"`).
- Scripts: `"dev": "vite --port 4705 --strictPort"`, `"build": "tsc -b && vite build"`, `"test": "vitest run"`.
- Proxy y `Origin` exactamente como ADR 006 Anexo C.
- `App.tsx` muestra el título "The Last Shipbender"; `App.test.tsx` lo verifica (test primero).

Criterios de aceptación:
- `cd frontend && npm ci && npm run build && npm test` → exit 0, 1 test verde.
- `git check-ignore frontend/node_modules frontend/dist` → ambos ignorados.
- Con backend en 4704 (`PORT=4704 ./mvnw spring-boot:test-run`) y `npm run dev`: `curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" -H "Origin: http://localhost:4705" -d '{"email":"x@x.com","password":"x"}' http://localhost:4705/api/auth/login` → `401` (no 403). Si no puedes levantar Docker: `Estado: parcial` y dilo.
- Detener ambos procesos al terminar (ver Puertos en `CLAUDE.md`).
- Commit `build: esqueleto del front React + TS con Vite y Vitest (tarea 501)`.

## Resultado
