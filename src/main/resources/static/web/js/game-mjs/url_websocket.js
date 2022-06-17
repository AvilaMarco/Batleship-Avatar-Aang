import {getItemStorage} from "../utils/utils.js";

const params = new URLSearchParams(location.search);
const playerRoute = params.get("player-id");
const {gameId, email} = getItemStorage("player-" + playerRoute)

let TOKEN = getItemStorage("user-token")
const HEADER = {login: email, 'Authorization': TOKEN}
const BASE_URL_TOPIC = `/topic/gameplay/${gameId}`;
const BASE_URL_APP = `/app/${gameId}`;

// TOPICS
const TOPIC_WAITING_PLAYER = `${BASE_URL_TOPIC}/waiting-player`
const TOPIC_SHIPS = `${BASE_URL_TOPIC}/ships`
const TOPIC_EMOTES = `${BASE_URL_TOPIC}/emotes`
const TOPIC_SALVOS = `${BASE_URL_TOPIC}/salvos`
const TOPIC_REMATCH = `${BASE_URL_TOPIC}/rematch`

// Send Data
const SEND_SHIPS = `${BASE_URL_APP}/ships`
const SEND_EMOTES = `${BASE_URL_APP}/emotes`
const SEND_REMATCH = `${BASE_URL_APP}/rematch`

export {
  TOPIC_WAITING_PLAYER,
  TOPIC_SHIPS,
  TOPIC_EMOTES,
  TOPIC_SALVOS,
  TOPIC_REMATCH,
  SEND_EMOTES,
  SEND_SHIPS,
  SEND_REMATCH,
  HEADER,
  TOKEN,
  gameId
}