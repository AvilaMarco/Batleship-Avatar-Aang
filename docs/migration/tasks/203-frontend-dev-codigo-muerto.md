# 203 · frontend-dev · Borrar código muerto del front actual
Estado: hecha
Fase: F2
Depende de: — (se puede hacer en paralelo con la 201; mergear las dos juntas)

Objetivo: quitar del front servido desde `static/` los archivos y envíos que nada usa, para que F5 parta de un inventario real. Todo se verificó con grep: ningún HTML ni JS importa estos archivos.

Archivos y cambios (raíz: `src/main/resources/static/web/`):
1. Borrar `js/game-mjs/sobras.js`. No lo importa nadie; llama a endpoints que ya no existen (`/api/rematch`, `/api/games/players/.../salvos`).
2. Borrar la carpeta `delete/` (11 PNG). Ningún HTML ni CSS los referencia (`games.html:60` usa `mapa-game-mobile.png`, otro archivo).
3. Borrar `js/game-mjs/TEST_DATA.js` (nadie lo importa) y su única referencia, el comentario de `js/game.js:40` (`// const {status, data} = {data: DATA, status: STATUS};`), junto con la línea comentada siguiente.
4. Rematch/salvos por WebSocket (la 201 borra sus handlers falsos del backend):
   - `js/game-mjs/websocket/url_send.js`: quitar `SEND_REMATCH`.
   - `js/game-mjs/websocket/sends.js`: quitar `sendRematch` (no se llama desde ningún lado) y su import.
   - `js/game-mjs/websocket/url_topics.js`: quitar `TOPIC_SALVOS` y `TOPIC_REMATCH`.
   - `js/game-mjs/websocket/topics.js`: quitar las dos `stomp.subscribe` (líneas 25 y 29) y sus imports.
   - `js/game-mjs/websocket/handler_websocket.js`: quitar `handlerSalvos` y `handlerRematch` (solo hacen `console.log`).
- No toques `js/utils/test.js`: pese al nombre lo usa `js/main.js:9` (`randomNation`).

Criterios de aceptación:
- `grep -rnE "sobras|TEST_DATA|SEND_REMATCH|sendRematch|TOPIC_SALVOS|TOPIC_REMATCH|handlerSalvos|handlerRematch" src/main/resources/static` → sin resultados.
- `test -d src/main/resources/static/web/delete` → falla (la carpeta no existe).
- `./mvnw spring-boot:run`, login en `http://localhost:8080/`, abrir `web/games.html`, entrar a una partida (`web/game.html?...`): la consola del navegador no muestra errores de import/404 de JS y los barcos se pueden enviar (llega el mensaje de `/topic/gameplay/{id}/ships`).

## Resultado
Hecho: punto 3 (comentario en `game.js`) y punto 4 completo (url_send, sends, url_topics, topics, handler_websocket). El grep de aceptación no da resultados.
Pendiente (bloqueado: el sistema denegó el comando `rm`, solo lo puede aprobar una persona): borrar `web/js/game-mjs/sobras.js`, `web/js/game-mjs/TEST_DATA.js` y la carpeta `web/delete/`. Comando: `git rm -r src/main/resources/static/web/delete src/main/resources/static/web/js/game-mjs/sobras.js src/main/resources/static/web/js/game-mjs/TEST_DATA.js`.
No se probó en navegador.

Borrados finales ejecutados por el usuario (`git rm` de `web/delete/`, `sobras.js`, `TEST_DATA.js`) por permisos denegados al agente.
