import 'sockjs' // ejecuta codigo de sockJS y permite usar el objeto SockJS
import 'stomp' // ejecuta codigo de stompJS y permite usar el objeto Stomp
import {createGrid} from "./game-mjs/grid.js";
import {shipOnGrid,} from "./game-mjs/ships.js";
import {getHTML} from "./utils/utils.js";
import {getToken} from "./utils/payload.js";
import {setupShipsHost, setupTitleGame, startGame, viewClientData} from "./game-mjs/update_html.js";
import {CLIENT, HOST} from "./game-mjs/TEST_DATA.js"
import {
  handlerEmotes,
  handlerRematch,
  handlerSalvos,
  handlerShips,
  handlerWaitingPlayer
} from "./game-mjs/handler_websocket.js";
import {
  gameId,
  HEADER,
  SEND_EMOTES,
  SEND_REMATCH,
  SEND_SHIPS,
  TOKEN,
  TOPIC_EMOTES,
  TOPIC_REMATCH,
  TOPIC_SALVOS,
  TOPIC_SHIPS,
  TOPIC_WAITING_PLAYER
} from "./game-mjs/url_websocket.js";

/* WEB SOCKET */
let stomp = null;
/* Player */
let player = {};
let rival = {};
let game = {};
/* Load Data Local Storage */
// const params = new URLSearchParams(location.search);
// const playerRoute = params.get("player-id");
// const {gameId} = getItemStorage("player-" + playerRoute)

// let TOKEN = getItemStorage("user-token")
// const HEADER = {login: "marco@admin.com", passcode: "123", 'Authorization': TOKEN}
// const BASE_URL_TOPIC = `/topic/gameplay/${gameId}`;
// const BASE_URL_APP = `/app/${gameId}`;

// TEST
const status = document.getElementById("HOME_TEST")
status.addEventListener("click", async () => {
  const {status, data} = await statusGame(gameId, TOKEN);
  let {game, host, client} = status;
  console.log(data)
  console.log(status)
})

const emote = document.getElementById("EMOTE_CHAT")
emote.addEventListener("click", async () => {
  const emote = prompt("ingresa un emote")
  sendEmote(emote);
})

document.addEventListener('DOMContentLoaded', async () => {

  /*  const {token} = await SIMULATE_LOGIN()
    TOKEN = token;
    localStorage.setItem(`user-token`, JSON.stringify(token));*/

  createGrid(9, getHTML("#grid"), "ships", "gridShips");
  const {status, data} = await statusGame(gameId, TOKEN);
  let {game, host, client} = status;
  console.log(data)
  console.log(status)
  connectClientSocket(status);

  viewClientData(HOST)
  setupTitleGame(data.nation)
  if (game === "CREATED") {

    // HOST
    if (host === "HOST_WITHOUT_SHIPS") {
      setupShipsHost(sendShips)
    } else if (host === "HOST_WITH_SHIPS") {
      // created existends ships
    }
    // CLIENT
    if (client === "CLIENT_WAITING") {
      // view señor x
    } else if (client === "CLIENT_WITHOUT_SHIPS") {
      viewClientData(CLIENT)
    } else if (client === "CLIENT_WITH_SHIPS") {
      viewClientData(CLIENT)
    }

  } else if (game === "IN_GAME") {
    startGame()
  }

})

/* START GAME - WEB SOCKET*/
function connectClientSocket({game, client}) {
  const socket = new SockJS("/the-last-shipbender");
  stomp = Stomp.over(socket);

  if (game === "CREATED") {
    if (client === "CLIENT_WAITING") {
      connectAndSuscribeWaitingClient()
    }
    connectAndSubscribeShips()
  } else {
    connectAndSuscribeGame()
  }

}

function connectAndSuscribeWaitingClient() {
  stomp.connect(HEADER, (frame) => {
    console.log("Connected: " + frame);
    /* Send Waiting Player */
    stomp.subscribe(TOPIC_WAITING_PLAYER, handlerWaitingPlayer, HEADER);

    /* Send Ships */
    stomp.subscribe(TOPIC_SHIPS, handlerShips, HEADER);
  });
}

function connectAndSubscribeShips() {
  stomp.connect(HEADER, (frame) => {
    console.log("Connected: " + frame);
    if (true) {
      if (true) {
        /* Send Ships */
        stomp.subscribe(TOPIC_SHIPS, handlerShips, HEADER);
      }
    }
  });
}

function connectAndSuscribeGame() {
  stomp.connect(HEADER, (frame) => {
    console.log("Connected: " + frame);
    /* Send emotes */
    stomp.subscribe(TOPIC_EMOTES, handlerEmotes, HEADER);

    /* Send Salvos */
    stomp.subscribe(TOPIC_SALVOS, handlerSalvos, HEADER);

    /* Send Rematch */
    stomp.subscribe(TOPIC_REMATCH, handlerRematch, HEADER);
  });
}

/* END GAME - WEB SOCKET*/

/* SEND DATA websocket */
function sendShips() {
  /*  if (someShipOnDocker()) {
      alert("faltan colocar ships");
      return
    }*/

  stomp.send(SEND_SHIPS, HEADER, JSON.stringify(shipOnGrid()));
}

function sendEmote(emote) {

  stomp.send(
      SEND_EMOTES,
      HEADER,
      emote
  );
}

function sendRematch() {
  stomp.send(
      SEND_REMATCH,
      HEADER,
      JSON.stringify({})
  );
}

function statusGame(gameId, token) {
  return fetch(`/api/match/${gameId}/status`, getToken(token))
      .then((res) => (res.ok ? res.json() : Promise.reject(res.body)))
}