import {gameId} from "./data_connection.js";

const BASE_URL_TOPIC = `/topic/gameplay/${gameId}`;

const TOPIC_WAITING_PLAYER = `${BASE_URL_TOPIC}/waiting-player`
const TOPIC_SHIPS = `${BASE_URL_TOPIC}/ships`
const TOPIC_EMOTES = `${BASE_URL_TOPIC}/emotes`

export {TOPIC_WAITING_PLAYER, TOPIC_SHIPS, TOPIC_EMOTES}