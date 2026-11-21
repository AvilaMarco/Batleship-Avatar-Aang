# 307 · frontend-dev · Front viejo con WebSocket nativo y webjar de STOMP con versión
Estado: parcial
Fase: F3
Depende de: 306

Objetivo: que el front actual siga jugable durante F3 sin SockJS y sin `webjars-locator-core` (Spring 7 ya no resuelve webjars sin versión). Son 3 cambios; el front se reescribe en F5.

Archivos: `src/main/resources/static/web/game.html`, `src/main/resources/static/web/js/game.js`.

Cambios (decididos):
- `game.html`, importmap: borra la entrada `"sockjs"`; `"stomp"` pasa a `"/webjars/stomp-websocket/2.3.4/stomp.min.js"` (ruta real dentro del jar).
- `game.js`: borra `import "sockjs";` y en `connectClientSocket` reemplaza las dos líneas `new SockJS(...)` / `Stomp.over(socket)` por
  `stomp = Stomp.client(`${location.protocol === "https:" ? "wss" : "ws"}://${location.host}/the-last-shipbender`);`
  (stomp.js 2.3.4 manda por defecto heartbeats de 10 s, igual que el servidor).
- No cambies nada más.

Criterios de aceptación:
- `grep -rn "sockjs\|SockJS" src/main/resources/static/web --include=*.js --include=*.html` → sin resultados.
- Con `PORT=4704 ./mvnw spring-boot:run`: `curl -s -o /dev/null -w "%{http_code}" http://localhost:4704/webjars/stomp-websocket/2.3.4/stomp.min.js` → `200`.
- Manual, dos ventanas (una en incógnito): login con dos usuarios, A crea una partida, B se une, ambos colocan barcos y envían; en la consola del navegador aparece `Connected` y en la pestaña Network el WebSocket `/the-last-shipbender` con estado 101 y frames `PING`/heartbeat cada ~10 s. Detén el proceso.

## Resultado
Cambios: `game.html` (importmap sin `sockjs`, `stomp` -> `/webjars/stomp-websocket/2.3.4/stomp.min.js`) y `game.js` (sin `import "sockjs"`, `Stomp.client(ws|wss://host/the-last-shipbender)`).
Verificado:
- grep `sockjs|SockJS` en static/web (js/html): sin resultados. Único `webjars` en static/ y resources: el importmap de game.html.
- `PORT=4704 ./mvnw spring-boot:run` + curl a `/webjars/stomp-websocket/2.3.4/stomp.min.js` -> 200 (devuelve stomp.js real). Proceso detenido por PID (puerto 4704 libre).
- Nota: la ruta SIN versión `/webjars/stomp-websocket/stomp.min.js` también devolvió 200 en esta prueba, así que el curl no demuestra por sí solo que la versión sea obligatoria; la ruta versionada sí funciona.
Pendiente de verificar (sin navegador): prueba manual de dos ventanas (`Connected` en consola, WebSocket 101 y heartbeats ~10 s).
