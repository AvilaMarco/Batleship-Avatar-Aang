import {handlerEmotes, handlerShips, handlerWaitingPlayer} from "./handler_websocket.js";
import {TOPIC_EMOTES, TOPIC_SHIPS, TOPIC_WAITING_PLAYER} from "./url_topics.js";
import {HEADER} from "./data_connection.js";

const suscribeWaitingClient = (stomp) => {
  /* Send Waiting Player */
  HEADER.id = "WAITING_CLIENT"
  stomp.subscribe(TOPIC_WAITING_PLAYER, handlerWaitingPlayer, HEADER);
}

const subscribeShips = (stomp) => {
  /* Send Ships */
  HEADER.id = "SHIPS"
  stomp.subscribe(TOPIC_SHIPS, (body) => handlerShips(body, stomp), HEADER);
}

const suscribeGame = (stomp) => {

  /* Send emotes */
  HEADER.id = "EMOTES"
  stomp.subscribe(TOPIC_EMOTES, handlerEmotes, HEADER);
}

export {suscribeWaitingClient, subscribeShips, suscribeGame}