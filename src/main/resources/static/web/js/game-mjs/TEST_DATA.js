const CLIENT = {
  name: "PLAYER2",
  score: 288,
  nation: "FIRE",
  user_type: "CLIENT",
};

const DATA = {
  id: 3,
  nation: "FIRE",
  location: "F1",
  game_players: [
    {
      id: 5,
      emote: null,
      score: null,
      player: {
        id: 6,
        name: "Marco Avila",
        email: "marco@admin.com",
        nation: "EARTH",
      },
      ships: [],
      salvos: [],
    },
    {
      id: 7,
      emote: null,
      score: null,
      player: {
        id: 5,
        name: "Marco Avila",
        email: "marco@aaa.com",
        nation: "EARTH",
      },
      ships: [],
      salvos: [],
    },
  ],
};
const STATUS = {
  game: "CREATED",
  host: "HOST_WITHOUT_SHIPS",
  client: "CLIENT_WITHOUT_SHIPS",
};

const HOST = {
  name: "RacnarWAn",
  score: 1222,
  nation: "WATER",
  user_type: "HOST",
};

const SIMULATE_LOGIN = () => {
  const headers = {
    "Content-Type": "application/json",
  };
  const user = { email: "marco@admin.com", password: 123 };

  return fetch("/api/players/login", {
    method: "POST",
    body: JSON.stringify(user),
    headers,
  }).then((res) => (res.ok ? res.json() : Promise.reject(res.json())));
};

export { HOST, CLIENT, DATA, STATUS, SIMULATE_LOGIN };
