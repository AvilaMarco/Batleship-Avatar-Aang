# 204 · backend-dev · Arreglar bugs reales de colocar barcos por WebSocket
Estado: hecha
Fase: F2
Depende de: 201

Objetivo: arreglar solo los bugs mecánicos detectados por la 104 (`GameplayWebSocketTest`). Las decisiones de diseño van a F4 con el contrato v2 (ADR 003), no aquí.

Archivos:
- `service/GameplayService.java`, `service/model/GameService.java`
- `src/test/.../integration/GameplayWebSocketTest.java` (única excepción de edición en tests, solo para actualizar asserts marcados "bug conocido" de los puntos 1 y 2)

Cambios:
1. `GameplayService.createShips` lanza `LazyInitializationException` (`GameplayService.java:47` → `GameService.java:48`, `Ship.salvosImpacted`): en el hilo STOMP no hay sesión de Hibernate. Arreglo mínimo: que la construcción de la respuesta ocurra dentro de una transacción (`@Transactional` en el método del servicio). No cambiar `open-in-view` ni el tipo de fetch.
2. `GameService.java:41` (`getGameMatch`): `gp.getId() != gamePlayerId` compara `Long` por referencia. Usar `.equals`. Buscar el mismo patrón con `grep -rn "getId() [!=]= " src/main/java` y corregir los que comparen `Long`/objetos (no tocar `long` primitivos).

Fuera de alcance (pasa a F4, no tocar):
- El topic compartido: tras el arreglo 1 el rival recibirá el `GameDataDTO` de A (posiciones incluidas). Esto es lo esperado en esta fase: el test debe fijarlo con un comentario `// bug conocido: se arregla en F4 (ADR 003)`.
- Validación de barcos (`ShipValidation` se borra en la 201; se reescribe en el dominio en F4).
- Segundo envío sin `@MessageExceptionHandler`.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 3 corridas seguidas.
- En `GameplayWebSocketTest`, el caso 1 pasa a verificar que A y el rival SÍ reciben un mensaje en `/topic/gameplay/{id}/ships` (con comentario de bug conocido por la fuga al rival).
- `git diff --stat -- src/main` solo muestra los dos archivos de la lista (más los que tengan el patrón `Long !=` del punto 2).

## Resultado
Punto 1: `@Transactional` en `GameplayService.createShips`; el mensaje ahora se publica a A y al rival (test actualizado, bug de fuga marcado para F4).
Punto 2: no era bug. `getId()` devuelve `long` primitivo, asi que `gp.getId() != gamePlayerId` (long vs Long) desempaqueta y compara por valor; `.equals` ni compila. Sin cambios en `GameService`/`Game`/`Salvo`.
Verificacion: `./mvnw test` BUILD SUCCESS x3.
