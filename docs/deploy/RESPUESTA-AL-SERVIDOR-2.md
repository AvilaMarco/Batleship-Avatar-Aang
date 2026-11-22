# Respuesta 2 de Shipbender al administrador (pegar tal cual)

Contrato `paquete` v1 aceptado con tus 6 ajustes. Sigue siendo solo consulta; aún no instales nada. Ya tengo `compose.yaml`, `deploy/servidor.env` y `scripts/ship.sh` adaptados; probados en local con las mismas restricciones (read_only, cap_drop ALL, no-new-privileges, user no root, mem/memswap/pids, redes web/interna imitadas): db y app `healthy`, WebSocket 101 con `X-Forwarded-Proto: https` y 403 sin él o con origen ajeno, app ~253 MiB/768, db ~45 MiB/256, paquete de 137 MB.

Cambios que hice a tu medida: imagen `shipbender/app:<sha>`; Postgres `17.11-alpine@sha256:e5d2edd28b1f8d40a0a6c0c9cdc545b19b56a0d5d88f94bc11a97d970d4700d0` (17 días, Docker Hub `postgres`); sin `name:`, `container_name`, `ports`, redes ni volúmenes con nombre; `SECRETOS=JWT_SECRET:48,POSTGRES_PASSWORD:32` (hex); `SALIDA=ninguna`; `SALUD=/` con `SALUD_ESPERA=150`.

Preguntas (solo respuesta):
1. **`${VERSION}`**: mi compose usa `image: shipbender/app:${VERSION}` para que un cambio de imagen no cambie el compose (si no, cada deploy pediría aprobación). ¿El runner exporta una variable con la `<version>` de `recibir` al hacer `up`? ¿Con qué nombre (propongo `VERSION`) y se permite en la validación de "`${NOMBRE}` solo si está en SECRETOS"?
2. **Postgres** con `./data/pgdata` y `user: "70:70"`: lo probé con volumen con nombre (NTFS no deja chmod). ¿Cabe que el runner cree `data/pgdata` con dueño 70:70 y modo 700 antes del primer `up`?
3. `memswap_limit` = `mem_limit` en ambos y suma 1024m ≤ `MEMORIA_MAX=1100m`: ¿correcto? Pido `pids_limit` 200 (app) y 100 (db): ¿entran en `PIDS_MAX`?
4. **Healthcheck** `wget` en la app (existe en la imagen) y `pg_isready` en db: confirma que el validador acepta `healthcheck` con `CMD`.
5. **Rate limit**: dime cuando lo creaste (10 cada 10 s por IP, POST a `/api/auth/login` y `/api/players`).
6. Mándame las fuentes de `recibir`, el validador y el runner cuando estén escritas, antes de instalar (yo las reviso). Para el `scp` de mi lado, los archivos están en la rama `migration/f7-deploy-ajustes` del repo: `compose.yaml`, `deploy/servidor.env`, `scripts/deploy.sh` (solo referencia).
