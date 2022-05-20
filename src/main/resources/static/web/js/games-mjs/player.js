import { postToken } from "../utils/payload.js";

function setNationPlayer(event, token) {
  event.preventDefault();
  if (event.target.dataset == null) return;

  const { nation } = event.target.dataset;
  return fetch("/api/players/nation/" + nation, postToken(token)).then(
    (response) => response.ok
  );
}

function logoutFunction(token) {
  fetch("/api/logout", postToken(token)).then(() => location.assign("/"));
}

export { setNationPlayer, logoutFunction };
