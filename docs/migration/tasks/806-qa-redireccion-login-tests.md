# 806 · qa · Tests: usuario con sesión no ve el login
Estado: hecha
Fase: F8
Depende de: —
Paralelizable: no (comparte `frontend/src/App.tsx` con 807)
Origen: "crea una tarea para el redireccionamiento, si un usuario ya esta logeado tiene que ir al menu principal"
Riesgo: normal

Objetivo: tests (rojos hasta 807) del redireccionamiento en `App`: con sesión iniciada, `#/login` lleva al menú principal.

Archivos: `frontend/src/App.test.tsx` (nuevo o existente).

Casos:
- Con perfil cargado y ruta `#/login` → el hash pasa a `#/` y se ve el lobby (no el texto "Ya has iniciado sesión.", que desaparece).
- Tras un login correcto desde `#/login` → termina en `#/` (hoy no navega: comprobar y cubrir).
- Sin perfil y ruta `#/` o `#/games/{id}` → sigue yendo a `#/login` (sin regresión).
- Mientras `ready` es falso no se redirige a ningún lado (sin parpadeo del login).

Criterios de aceptación:
- `cd frontend && npm test` → los casos nuevos fallan por la razón esperada (rojo documentado) y el resto sigue en verde.
- Commit `test: redirección del login con sesión iniciada (tarea 806, rojo hasta 807)`.

## Resultado
`App.test.tsx` reescrito con mocks de `api/players`, Lobby y Game (se conserva el test del título). En rojo (1): "con sesión iniciada, #/login lleva a #/ y muestra el lobby": `App` muestra "Ya has iniciado sesión." y no navega (no aparece el lobby).
En verde: login correcto desde `#/login` termina en `#/` (ya navega en `AuthContext.login`, queda cubierto), sin perfil en `#/` y `#/games/5` va a `#/login`, y con `ready` falso no hay redirección ni parpadeo del login.
