# 811 · qa · App.test espera un heading en el login que 810 prohíbe

- Estado: pendiente
- Origen: tarea 808 punto 5 ("Login: sin texto ni puntos") + `LoginScreen.design.test.tsx` (`queryByRole("heading")` es null).
- Riesgo: bajo (solo tests).

`src/App.test.tsx` (it.each "sin perfil y ruta %s sigue yendo a #/login") hace
`findByRole("heading", { name: "Iniciar sesión" })`. Choca con el test de diseño: el login ya no tiene heading
(el `<form>` conserva el nombre accesible "Iniciar sesión" con `aria-label`).

Hecho cuando: ese test busca `findByRole("form", { name: "Iniciar sesión" })` (o equivalente) y `npm test` queda verde.
No lo edita frontend-dev (solo qa modifica tests existentes).
