// Service worker mínimo: precache del shell + offline fallback. El juego necesita red (API y WebSocket no se tocan).
// Subir VERSION invalida la caché anterior.
const VERSION = "v1";
const CACHE = `shipbender-${VERSION}`;
const SHELL = ["/", "/manifest.webmanifest", "/favicon.svg", "/icons/icon-192.png", "/assets/sprite.svg"];

self.addEventListener("install", (e) => {
  e.waitUntil(caches.open(CACHE).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting()));
});

self.addEventListener("activate", (e) => {
  e.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim()),
  );
});

self.addEventListener("fetch", (e) => {
  const { request } = e;
  const url = new URL(request.url);
  if (request.method !== "GET" || url.origin !== location.origin || url.pathname.startsWith("/api/") || url.pathname.startsWith("/the-last-shipbender")) return;
  if (request.mode === "navigate") {
    // red primero; sin red, el shell cacheado
    e.respondWith(fetch(request).catch(() => caches.match("/")));
    return;
  }
  // estáticos: caché primero, y se guarda lo que llega (los bundles llevan hash)
  e.respondWith(
    caches.match(request).then((hit) => hit ?? fetch(request).then((res) => {
      if (res.ok) { const copy = res.clone(); void caches.open(CACHE).then((c) => c.put(request, copy)); }
      return res;
    })),
  );
});
