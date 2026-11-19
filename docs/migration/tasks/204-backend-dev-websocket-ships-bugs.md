# 105 · backend-dev · Bugs de colocar barcos por WebSocket
Estado: pendiente
Fase: F3

Detectados en 104 (`GameplayWebSocketTest`):
1. `GameplayService.createShips` lanza `LazyInitializationException` al armar la respuesta: no se publica nada en `/topic/gameplay/{id}/ships`.
2. Respuesta en topic compartido: el rival recibe los barcos de A. Enviar a un destino por usuario o sin posiciones.
3. `GameService.getGameMatch`: `gp.getId() != gamePlayerId` compara `Long` por referencia (usar `equals`).
4. `ShipValidation` no se usa: se aceptan celdas invalidas, tamanos incorrectos y barcos superpuestos.
5. Segundo envio de barcos: `RuntimeException` sin `@MessageExceptionHandler`; el cliente no recibe error.
Al corregir, actualizar los tests marcados `knownBug` en `GameplayWebSocketTest`.
