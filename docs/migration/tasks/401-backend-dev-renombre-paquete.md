# 401 · backend-dev · Renombrar paquete a `dev.racnar.shipbender`
Estado: hecha
Fase: F4
Depende de: — (ADR 004 y 005 aprobadas)
Paralelizable: no (toca todos los archivos Java)

Objetivo: cambio puramente mecánico de paquete base y `groupId` (ADR 002 punto 1), sin cambios de lógica, para que el diff de la 402 sea solo de estructura.

Archivos: `pom.xml`, todo `src/main/java/com/codeblockacademy/shipbender/**` y `src/test/java/com/codeblockacademy/shipbender/**`.

Diseño (cerrado):
- `git mv src/main/java/com/codeblockacademy/shipbender src/main/java/dev/racnar/shipbender` (ídem `src/test/java`) y borrar los directorios vacíos que queden.
- Reemplazar `com.codeblockacademy.shipbender` por `dev.racnar.shipbender` en `package` e `import`.
- `pom.xml`: `<groupId>com.codeblockacademy</groupId>` del proyecto → `<groupId>dev.racnar</groupId>`.
- `SalvoApplication` → `ShipbenderApplication` (archivo y clase); actualizar `TestShipbenderApplication` (`SpringApplication.from(ShipbenderApplication::main)`).
- Excepción de rol (ADR 005 punto 9): en `src/test` solo paquetes e imports, sin tocar aserciones.
- Nada más: ni formato, ni nombres de otras clases, ni código comentado.

Criterios de aceptación:
- `grep -rn "codeblockacademy" src pom.xml` → sin salida.
- `grep -c "<groupId>dev.racnar</groupId>" pom.xml` → `1`.
- `git diff --cached -M --stat` (antes del commit): todos los `.java` aparecen como renombres (`=>`); los cambios de contenido son solo líneas `package`/`import` y el nombre de la clase de arranque.
- `./mvnw test` → BUILD SUCCESS, 40 tests, 0 fallos, 0 errores.
- Commit `refactor: paquete base dev.racnar.shipbender (tarea 401)`.

## Resultado
Paquete base y groupId renombrados a `dev.racnar`; `SalvoApplication` -> `ShipbenderApplication` (+ `TestShipbenderApplication`). 93 archivos, 303+/303-.
- `grep -rn codeblockacademy src pom.xml`: sin salida; groupId `dev.racnar`: 1.
- `git diff --cached -M --stat`: todos renombres salvo `IGameService.java` (archivo de 11 lineas, similitud <50%, git lo muestra como delete+add; el contenido cambia solo en `package`/`import`). Resto de cambios: solo `package`/`import`, groupId y clase de arranque.
- `./mvnw test` x2: 40 tests, 0 fallos, 0 errores, BUILD SUCCESS.
