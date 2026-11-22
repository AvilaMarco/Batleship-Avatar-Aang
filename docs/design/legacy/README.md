# Ver el front original (v1) sin backend
```sh
mkdir -p /tmp/legacy && git archive b40e276^ src/main/resources/static | tar -x -C /tmp/legacy
python -I docs/design/legacy/mock_legacy.py /tmp/legacy/src/main/resources/static 4791 WATER   # o "none" → pantalla de elegir nación
```
Abrir `http://127.0.0.1:4791/index.html`; para el menú: `localStorage.setItem('user-token','"fake"')` y `/web/games.html`. Menú en horizontal (812×375). La partida (`game.html`) necesita WebSocket: no se mockea.
