import {shipOnGrid} from "../ships.js";
import {SEND_EMOTES, SEND_REMATCH, SEND_SHIPS} from "./url_send.js";
import {HEADER} from "./data_connection.js";

const sendShips = (stomp) => {
  /*  if (someShipOnDocker()) {
      alert("faltan colocar ships");
      return
    }*/

  stomp.send(SEND_SHIPS, HEADER, JSON.stringify(shipOnGrid()));
}

const sendEmote = (stomp, emote) => {

  stomp.send(
      SEND_EMOTES,
      HEADER,
      emote
  );
}

const sendRematch = (stomp) => {
  stomp.send(
      SEND_REMATCH,
      HEADER,
      JSON.stringify({})
  );
}

export {sendEmote, sendShips, sendRematch}