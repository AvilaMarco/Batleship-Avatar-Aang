import {suscribeGame} from "./topics.js";

const handlerWaitingPlayer = ({body}) => {
  console.log(JSON.parse(body))
}
const handlerShips = ({body}, stomp) => {
  const response = JSON.parse(body);
  console.log(response)
  if (response.game === "IN_GAME") {
    suscribeGame(stomp)
  }
}

const handlerEmotes = ({body}) => {
  console.log("a")
  console.log(JSON.parse(body))
}

const handlerSalvos = ({body}) => {
  console.log(JSON.parse(body))
}

const handlerRematch = ({body}) => {
  console.log(JSON.parse(body))
}

export {handlerWaitingPlayer, handlerShips, handlerEmotes, handlerSalvos, handlerRematch}