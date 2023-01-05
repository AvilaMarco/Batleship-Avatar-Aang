import {gameId} from "./data_connection.js";

const BASE_URL_APP = `/app/${gameId}`;

const SEND_SHIPS = `${BASE_URL_APP}/ships`
const SEND_EMOTES = `${BASE_URL_APP}/emotes`
const SEND_REMATCH = `${BASE_URL_APP}/rematch`

export {SEND_EMOTES, SEND_SHIPS, SEND_REMATCH}