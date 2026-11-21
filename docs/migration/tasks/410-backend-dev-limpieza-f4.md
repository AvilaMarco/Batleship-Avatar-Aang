# 410 · backend-dev · Limpieza final de F4
Estado: pendiente
Fase: F4
Depende de: 409
Paralelizable: no (toca entidades y config que usan las tareas de salvos)

Objetivo: borrar lo que quedó sin uso tras 402-409 y dejar el esquema alineado con el dominio.

Archivos (`P = src/main/java/dev/racnar/shipbender`): `P/config/SpringBeans.java` → `P/config/DevDataSeeder.java` + `P/security/PasswordConfig.java`, `pom.xml`, `P/game/domain/{GamePlayer,Ship,Salvo}.java`, `P/player/domain/Player.java`, `P/player/PlayerRepository.java`, `P/game/{Ship,Salvo}Repository.java`, `src/main/resources/db/migration/V3__limpieza_f4.sql`.

Diseño (cerrado):
- `SpringBeans` se parte: `PasswordEncoder` → `PasswordConfig`; `initData` → `DevDataSeeder` (`@Profile("dev")`) usando `game.join(...)`, sin el código comentado. Se borra el bean y la dependencia de ModelMapper del `pom.xml`.
- Borrar `ShipRepository`, `SalvoRepository`, `PlayerRepository.findByEmailAndPassword` y `existsPlayer` (→ `existsByEmail`) si quedaron.
- `GamePlayer`: quitar `emote` y el bloque comentado; `setShips` (que en realidad agregaba) se borra si `placeShips` ya no lo usa. `Ship`: quitar `salvosImpacted`. `Salvo`: quitar `goodShoot`/`shipsDead` (la lógica de v2.1, si se aprueba, va al dominio en la 411).
- `Player.roles`: `@Enumerated(EnumType.STRING)` (hoy se guarda el ordinal: reordenar el enum cambiaría roles).
- `V3__limpieza_f4.sql`: `alter table game_player drop column emote;` `drop table ship_salvos_impacted;` `player_roles.roles` → `varchar(20)`: borrar el check generado en V1 (en Postgres se llama `player_roles_roles_check`), `alter column roles type varchar(20) using (case roles when 0 then 'GUEST' when 1 then 'PLAYER' when 2 then 'ADMIN' end)` y nuevo check de los 3 valores.
- `application.properties`: borrar comentarios de devtools/livereload.

Criterios de aceptación:
- `grep -rn "ModelMapper\|modelmapper" src pom.xml` → sin salida.
- `grep -rnE "^\s*(//|/\*)\s*\S.*;\s*$" src/main/java` → sin salida (no queda código comentado).
- `./mvnw test` → BUILD SUCCESS, 0 fallos (Flyway aplica V1-V3 y `ddl-auto=validate` pasa).
- `PORT=4704 ./mvnw spring-boot:test-run` arranca, login de `marco@admin.com` → 200 y `GET /api/games` lista las partidas del seed. Detener el proceso.
- Commit `refactor: limpieza final de F4 (tarea 410)`.

## Resultado
