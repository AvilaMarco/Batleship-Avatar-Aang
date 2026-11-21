# 205 · qa · Arreglar test intermitente de firma manipulada
Estado: hecha
Fase: F2
Depende de: —

Objetivo: `AuthIntegrationTest.setNation_withTamperedSignature_throwsInsteadOfResponding` falla ~1 de cada 4 corridas ("Expected Exception to be thrown, but nothing was thrown", línea 77).
Causa raíz (confirmada): el test altera el ÚLTIMO carácter del token. La firma HMAC-SHA256 son 32 bytes = 43 caracteres base64url, y el último solo aporta 4 bits útiles (los otros 2 son relleno). Si el cambio cae en esos bits, la firma decodificada es idéntica y el token sigue siendo válido.

Archivos: `src/test/java/com/codeblockacademy/shipbender/integration/AuthIntegrationTest.java` (solo ese test).

Cambio: alterar un carácter del MEDIO de la firma (por ejemplo, el primero tras el último `.`), sustituyéndolo por otro distinto del original. No tocar `src/main`.

Criterios de aceptación:
- Bucle de 12 corridas seguidas de `./mvnw test` → 12 BUILD SUCCESS (reportar el resultado real de las 12).
- `git diff --stat -- src/main` vacío.

## Resultado
Test ahora altera el primer caracter de la firma (tras el ultimo `.`), no el ultimo. 12 corridas de `./mvnw test`: 12 exit 0 (BUILD SUCCESS), 0 fallos. `git diff --stat -- src/main` vacio.
