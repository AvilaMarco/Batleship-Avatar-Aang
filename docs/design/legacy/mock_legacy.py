# Sirve el front viejo restaurado + respuestas falsas de /api para capturas. Uso: python -I mock_legacy.py <dir_static> <port> [nation|none]
import http.server, json, sys, os

ROOT, PORT = sys.argv[1], int(sys.argv[2])
NATION = None if len(sys.argv) < 4 or sys.argv[3] == "none" else sys.argv[3]
def p(i, name, nation, score): return {"id": i, "name": name, "email": f"{name.lower()}@test", "nation": nation,
    "stats": {"score": score, "won": score // 3, "tied": 1, "lost": 2}}
ME = p(1, "Katara", NATION, 27)
OTHERS = [p(2, "Toph", "EARTH", 36), p(3, "Zuko", "FIRE", 21), p(4, "Aang", "AIR", 18)]
def gp(pl): return {"id": pl["id"], "player": pl}
GAMES = [
    {"id": 10, "location": "W1", "game_players": [gp(ME), gp(OTHERS[1])]},
    {"id": 11, "location": "E1", "game_players": [gp(OTHERS[0])]},
    {"id": 12, "location": "E5", "game_players": [gp(OTHERS[2]), gp(OTHERS[0])]},
]

class H(http.server.SimpleHTTPRequestHandler):
    def __init__(s, *a, **k): super().__init__(*a, directory=ROOT, **k)
    def _json(s, obj, code=200):
        b = json.dumps(obj).encode(); s.send_response(code)
        s.send_header("Content-Type", "application/json"); s.send_header("Content-Length", str(len(b))); s.end_headers(); s.wfile.write(b)
    def do_GET(s):
        if s.path.startswith("/api/games"): return s._json({"player": ME, "games": GAMES, "players": [ME] + OTHERS})
        if s.path.startswith("/api/"): return s._json({})
        return super().do_GET()
    def do_POST(s):
        if s.path.startswith("/api/players/login"): return s._json({"token": "fake"})
        return s._json({})
    def log_message(s, *a): pass

http.server.ThreadingHTTPServer(("127.0.0.1", PORT), H).serve_forever()
