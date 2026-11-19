# ADR 001 — Decisiones base de la migración

Estado: aceptada · 2026-11-18

## Contexto
App propia de aprendizaje en Spring Boot 2.6 (sin soporte) + JS vanilla servido desde `static/`, H2 en memoria, pocos tests. Se quiere modernizar para poder seguir mejorándola y desplegarla gratis en un servidor local.

## Decisiones
1. **Actualizar, no reescribir** el backend: Spring Boot 4.x, Java 21, pasando por 2.7 y 3.x.
2. **Postgres + Flyway**; H2 solo para tests. No se conservan datos existentes.
3. **Front nuevo en React + TypeScript + Vite** (`/frontend`), compilado dentro del jar (un solo servicio, mismo origen, sin CORS).
4. **STOMP sobre WebSocket** se mantiene para juego y chat; se elimina SockJS. Broker en memoria (una instancia).
5. **Deploy**: Docker Compose (app + Postgres + cloudflared) en servidor local, expuesto con Cloudflare Tunnel.
6. **Proceso**: chat central orquesta subagentes por rol (`.claude/agents/`); backlog en `docs/migration/tasks/`; una rama por fase, merge a `main` tras aprobación.

## Alternativas descartadas
- Reescribir el backend en Node/NestJS: se pierde código que funciona sin ganancia concreta.
- Front y back desplegados por separado: agrega CORS y dos pipelines sin necesidad.
- Hexagonal completo: excesivo para el tamaño; solo se aísla el dominio del juego.

## Consecuencias
- Hace falta una red de tests (F1) antes de tocar dependencias.
- Para escalar a varias instancias habría que cambiar a un broker externo (RabbitMQ/Redis).
