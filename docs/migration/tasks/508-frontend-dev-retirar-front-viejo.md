# 508 · frontend-dev · Retirar el front viejo
Estado: pendiente
Fase: F5
Depende de: 501 (assets ya copiados a `frontend/public/assets`)
Paralelizable: sí, con 502-507 (no comparte archivos)
Origen: ADR 006 punto 8; ADR 004 Consecuencias ("el front viejo deja de funcionar"); PLAN.md Resultado F4.
Riesgo: normal
Ejecuta: **el usuario** (decisión U3): `git rm -r -q src/main/resources/static`. El central verifica los criterios y hace el commit; ningún agente intenta el borrado.

Objetivo: borrar `src/main/resources/static/` completo (no funciona contra el contrato v2 y sus assets ya están en `frontend/public`).

Archivos: `src/main/resources/static/**` (borrar).

Criterios de aceptación:
- `git ls-files src/main/resources/static | wc -l` → `0`.
- `ls frontend/public/assets/emotes/emote-1.png frontend/public/assets/img/fondo-mapa.png` → existen.
- `./mvnw test` → puede fallar solo `SecurityRulesIntegrationTest.publicPaths_withoutToken_areNotForbidden` si un path deja de existir (lo arregla qa en 509); cualquier otro fallo, no.
- Si el permiso de borrado se deniega (pasó en F2): no lo saltes; `Estado: parcial` y deja el comando para el usuario: `git rm -r -q src/main/resources/static`.
- Commit `chore: retirar front viejo de static/ (tarea 508)`.

## Resultado
