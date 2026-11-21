# 605 · devops · Runbook de operación
Estado: pendiente
Fase: F6
Depende de: 604
Paralelizable: sí, con 606 (solo escribe un doc)
Origen: ADR 007 punto 13; pedido del usuario "un runbook de operación (actualizar versión, rollback, ver logs, restaurar)".
Riesgo: normal

Objetivo: una persona puede actualizar, hacer rollback, ver logs, hacer un backup y restaurar sin leer el ADR.

Archivos: `docs/deploy/runbook.md` (excepción explícita a tu alcance: solo este archivo).

Diseño (cerrado): el ADR 007 Anexo E, más la primera instalación (ADR 007 "Pasos manuales" 2, 7 y 8) y una sección "Problemas conocidos": WebSocket 403 → revisar `X-Forwarded-Proto`/Host (ADR 007 punto 6, plan B); `MSYS_NO_PATHCONV`; después de un reinicio el juego no vuelve hasta iniciar sesión (U7). Cada comando, probado contra el stack de prueba de 603/604 (`COMPOSE_PROJECT_NAME=shipbender-test`).

Criterios de aceptación:
- Cada comando del runbook se ejecutó una vez contra el proyecto de prueba (anotarlo en el Resultado), incluido un ciclo actualizar → rollback con dos `APP_TAG` distintos.
- El runbook no contiene ningún secreto ni ningún token (revisar con `grep -iE 'secret|token|password' docs/deploy/runbook.md`: solo nombres de variables).
- Advertencia visible sobre `down -v` / `volume prune`.
- Commit `docs: runbook de operación del deploy (tarea 605)`.

## Resultado
