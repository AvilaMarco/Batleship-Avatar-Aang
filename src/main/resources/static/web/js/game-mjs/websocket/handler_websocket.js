import {suscribeGame} from "./topics.js";
import {createShipGrid, startGame} from "../update_html.js";
import {getShipsOf} from "../ship/helper.js";

const handlerWaitingPlayer = ({body}) => {
  console.log(JSON.parse(body))
}
const handlerShips = ({body}, stomp) => {
  const {status, data} = JSON.parse(body);
  const ships = getShipsOf(data)
  if (status.game === "IN_GAME") {
    suscribeGame(stomp)
    startGame(data);
  } else if (ships.length > 0) {
    createShipGrid(ships)
  }
}

const handlerEmotes = ({body}) => {
  console.log(JSON.parse(body))
}

const handlerSalvos = ({body}) => {
  console.log(JSON.parse(body))
}

const handlerRematch = ({body}) => {
  console.log(JSON.parse(body))
}

export {handlerWaitingPlayer, handlerShips, handlerEmotes, handlerSalvos, handlerRematch}