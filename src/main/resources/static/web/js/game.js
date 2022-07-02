import "sockjs"; // ejecuta codigo de sockJS y permite usar el objeto SockJS
import "stomp"; // ejecuta codigo de stompJS y permite usar el objeto Stomp
import {createGrid} from "./game-mjs/grid.js";
import {getHTML} from "./utils/utils.js";
import {getToken} from "./utils/payload.js";
import {setupShipsHost, setupTitleGame, startGame, viewClientData,} from "./game-mjs/update_html.js";
import {CLIENT, DATA, HOST, STATUS} from "./game-mjs/TEST_DATA.js";
import {gameId, HEADER, TOKEN} from "./game-mjs/websocket/data_connection.js";
import {subscribeShips, suscribeGame, suscribeWaitingClient,} from "./game-mjs/websocket/topics.js";
import {sendEmote} from "./game-mjs/websocket/sends.js";

/* WEB SOCKET */
let stomp = null;
/* Player */
let player = {};
let rival = {};
let game = {};

// TEST
const status = document.getElementById("HOME_TEST");
status.addEventListener("click", async () => {
  const {status, data} = await statusGame(gameId, TOKEN);
  let {game, host, client} = status;
  console.log(data);
  console.log(status);
});

const emote = document.getElementById("EMOTE_CHAT");
emote.addEventListener("click", async () => {
  const emote = prompt("ingresa un emote");
  sendEmote(stomp, emote);
});

document.addEventListener("DOMContentLoaded", async () => {
  createGrid(9, getHTML("#grid"), "ships", "gridShips");
  /*   const { status, data } = await statusGame(gameId, TOKEN);
  let { game, host, client } = status; */
  const {status, data} = {data: DATA, status: STATUS};
  let {game, host, client} = status;
  console.log(data);
  console.log(status);
  connectClientSocket(status);

  viewClientData(HOST);
  setupTitleGame(data.nation);
  if (game === "CREATED") {
    // HOST
    if (host === "HOST_WITHOUT_SHIPS") {
      setupShipsHost(stomp);
    } else if (host === "HOST_WITH_SHIPS") {
      // created existends ships
    }
    // CLIENT
    if (client === "CLIENT_WAITING") {
      // view señor x
    } else if (client === "CLIENT_WITHOUT_SHIPS") {
      viewClientData(CLIENT);
    } else if (client === "CLIENT_WITH_SHIPS") {
      viewClientData(CLIENT);
    }
  } else if (game === "IN_GAME") {
    startGame();
  }
});

/* WEB SOCKET*/
async function connectClientSocket({game, client}) {
  const socket = new SockJS("/the-last-shipbender");
  stomp = Stomp.over(socket);
  const frame = await AsyncStomp(HEADER);

  console.log("Connected: " + frame);

  if (game === "CREATED") {
    if (client === "CLIENT_WAITING") {
      suscribeWaitingClient(stomp);
    }
    subscribeShips(stomp);
  } else if (game === "IN_GAME") {
    suscribeGame(stomp);
  }
}

function AsyncStomp(Header) {
  return new Promise((resolve) => {
    stomp.connect(Header, resolve);
  })
}

function statusGame(gameId, token) {
  return fetch(`/api/match/${gameId}/status`, getToken(token)).then((res) =>
      res.ok ? res.json() : Promise.reject(res.body)
  );
}

/* Load Data Local Storage */
// const params = new URLSearchParams(location.search);
// const playerRoute = params.get("player-id");
// const {gameId} = getItemStorage("player-" + playerRoute)

// let TOKEN = getItemStorage("user-token")
// const HEADER = {login: "marco@admin.com", passcode: "123", 'Authorization': TOKEN}
// const BASE_URL_TOPIC = `/topic/gameplay/${gameId}`;
// const BASE_URL_APP = `/app/${gameId}`;
