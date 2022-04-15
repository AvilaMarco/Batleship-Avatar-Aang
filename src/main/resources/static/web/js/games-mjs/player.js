const POST = {method: "POST"}

function setNationPlayer(event) {
  event.preventDefault();
  if (event.target.dataset == null) return;

  const {nation} = event.target.dataset;
  return fetch("/api/players/nation/" + nation, POST)
      .then((response) => response.ok);
}

function logoutFunction() {
  fetch("/api/logout", POST).then(() => location.assign("/"));
}

export {setNationPlayer, logoutFunction}