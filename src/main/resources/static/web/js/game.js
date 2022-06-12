import {createGrid} from "./game-mjs/grid.js";
import {shipOnGrid,} from "./game-mjs/ships.js";
import {getHTML, getItemStorage} from "./utils/utils.js";
import {getToken} from "./utils/payload.js";
import {setupShipsHost, setupTitleGame, startGame, viewClientData} from "./game-mjs/update_html.js";
import {CLIENT, HOST} from "./game-mjs/TEST_DATA.js"

/* WEB SOCKET */
let stompClient = null;
/* Player */
let player = {};
let rival = {};
let game = {};
/* Load Data Local Storage */
const params = new URLSearchParams(location.search);
const playerRoute = params.get("player-id");
const {gameId, email} = getItemStorage("player-" + playerRoute)

let TOKEN = getItemStorage("user-token")
const HEADER = {login: "marco@admin.com", passcode: "123", 'Authorization': TOKEN}
const BASE_URL_TOPIC = `/topic/gameplay/${gameId}`;
const BASE_URL_APP = `/app/${gameId}`;

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
    if (host === "HOST_WITHOUT_SHIPS") setupShipsHost(sendShips)
    if (client !== "CLIENT_WAITING") viewClientData(CLIENT)
  } else if (game === "IN_GAME") {
    startGame()
  }

})

/* START GAME - WEB SOCKET*/
function connectClientSocket({game, client}) {
  const socket = new SockJS("/the-last-shipbender");
  stompClient = Stomp.over(socket);

  stompClient.connect(HEADER, function (frame) {
    console.log("Connected: " + frame);

    if (game === "CREATED") {
      if (client === "CLIENT_WAITING") {
        subscribeWaitingPlayer()
      }
      subscribeShips()
    } else {
      suscribeGame()
    }

  });
}

function subscribeWaitingPlayer() {
  /* Send Waiting Player */
  stompClient.subscribe(`${BASE_URL_TOPIC}/waiting-player`, ({body}) => {
    console.log(JSON.parse(body));
  }, HEADER);

}

function subscribeShips() {
  /* Send Ships */
  stompClient.subscribe(`${BASE_URL_TOPIC}/ships`, ({body}) => {
    console.log(JSON.parse(body));
  }, HEADER);
}

function suscribeGame() {
  /* Send emotes */
  stompClient.subscribe(
      `${BASE_URL_TOPIC}/emotes`,
      ({body}) => {
        console.log(JSON.parse(body));
      }, HEADER
  );

  /* Send Salvos */
  stompClient.subscribe(`${BASE_URL_TOPIC}/salvos`, ({body}) => {
    console.log(JSON.parse(body));
  }, HEADER);

  /* Send Rematch */
  stompClient.subscribe(`${BASE_URL_TOPIC}/rematch`, ({body}) => {
    console.log(JSON.parse(body));
  }, HEADER);
}

/* END GAME - WEB SOCKET*/

/* SEND DATA websocket */
function sendShips() {
  /*  if (someShipOnDocker()) {
      alert("faltan colocar ships");
      return
    }*/

  stompClient.send(`${BASE_URL_APP}/ships`, HEADER, JSON.stringify(shipOnGrid()));
}

function sendEmote(emote) {
  const emoteObject = {
    gp_id: playerRoute,
    emote: emote,
  };

  stompClient.send(
      `${BASE_URL_APP}/emotes`,
      HEADER,
      JSON.stringify(emoteObject)
  );
}

function sendRematch() {
  stompClient.send(
      `${BASE_URL_APP}/rematch`,
      HEADER,
      JSON.stringify({})
  );
}

function statusGame(gameId, token) {
  return fetch(`/api/match/${gameId}/status`, getToken(token))
      .then((res) => (res.ok ? res.json() : Promise.reject(res.body)))
}