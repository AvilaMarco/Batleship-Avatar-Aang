const handlerWaitingPlayer = ({body}) => {
  console.log(JSON.parse(body))
}
const handlerShips = ({body}) => {
  console.log(JSON.parse(body))
  console.log(body)
}

const handlerEmotes = ({body}) => {
  console.log(JSON.parse(body))
  console.log(body)
}

const handlerSalvos = ({body}) => {
  console.log(JSON.parse(body))
}

const handlerRematch = ({body}) => {
  console.log(JSON.parse(body))
}

export {handlerWaitingPlayer, handlerShips, handlerEmotes, handlerSalvos, handlerRematch}