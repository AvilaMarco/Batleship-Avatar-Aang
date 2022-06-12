const CLIENT = {
  name: "PLAYER2",
  score: 288,
  nation: "FIRE",
  user_type: "CLIENT"
}

const HOST = {
  name: "RacnarWAn",
  score: 1222,
  nation: "WATER",
  user_type: "HOST"
}

const SIMULATE_LOGIN = () => {
  const headers = {
    "Content-Type": "application/json",
  };
  const user = {email: "marco@admin.com", password: 123};

  return fetch("/api/players/login", {
    method: "POST",
    body: JSON.stringify(user),
    headers,
  })
      .then((res) => (res.ok ? res.json() : Promise.reject(res.json())))
}

export {HOST, CLIENT, SIMULATE_LOGIN}