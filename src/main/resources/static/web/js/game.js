import "sockjs"; // ejecuta codigo de sockJS y permite usar el objeto SockJS
import "stomp"; // ejecuta codigo de stompJS y permite usar el objeto Stomp
import {createGrid} from "./game-mjs/grid.js";
import {getHTML} from "./utils/utils.js";
import {getToken} from "./utils/payload.js";
import {createShipGrid, setupShipsHost, setupTitleGame, startGame, viewClientData,} from "./game-mjs/update_html.js";
import {CLIENT_TEST, HOST_TEST} from "./game-mjs/TEST_DATA.js";
import {gameId, HEADER, playerRoute, TOKEN} from "./game-mjs/websocket/data_connection.js";
import {subscribeShips, suscribeGame, suscribeWaitingClient,} from "./game-mjs/websocket/topics.js";
import {sendEmote} from "./game-mjs/websocket/sends.js";
import {GRID_SIZE} from "./game-mjs/CONSTANTS.js";

/* WEB SOCKET */
let stomp = null;
/* Player */
let HOST = {};
let CLIENT = {};
let GAME = {};

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
  createGrid(GRID_SIZE, getHTML("#grid"), "ships", "gridShips");
  const {status, data} = await statusGame(gameId, TOKEN);
  let {game, host, client} = status;
  updataHostAndClientData(data)
  GAME = data
  // const {status, data} = {data: DATA, status: STATUS};
  // let {game, host, client} = status;
  console.log(data);
  console.log(status);
  connectClientSocket(status);

  viewClientData(HOST_TEST);
  setupTitleGame(data.nation);
  if (game === "CREATED") {
    // HOST
    if (host === "WITHOUT_SHIPS") {
      setupShipsHost(stomp);
    } else if (host === "WITH_SHIPS") {
      createShipGrid(HOST.ships)
    }
    // CLIENT
    if (client === "WAITING") {
      // view señor x
      viewClientData({});
    } else if (client === "WITHOUT_SHIPS") {
      viewClientData(CLIENT_TEST);
    } else if (client === "WITH_SHIPS") {
      viewClientData(CLIENT_TEST);
    }
  } else if (game === "IN_GAME") {
    startGame(data);
  }
});

/* WEB SOCKET*/
async function connectClientSocket({game, client}) {
  const socket = new SockJS("/the-last-shipbender");
  stomp = Stomp.over(socket);
  const frame = await AsyncStomp(HEADER);

  console.log("Connected: " + frame);

  if (game === "CREATED") {
    if (client === "WAITING") {
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
  });
}

function statusGame(gameId, token) {
  return fetch(`/api/match/${gameId}/status`, getToken(token)).then((res) =>
      res.ok ? res.json() : Promise.reject(res.body)
  );
}

function updataHostAndClientData({game_players}) {
  HOST = game_players.find(gp => gp.player.id === playerRoute)
  HOST.user_type = "HOST"
  CLIENT = game_players.find(gp => gp.player.id === playerRoute)
  CLIENT.user_type = "CLIENT"
}

/* Load Data Local Storage */
// const params = new URLSearchParams(location.search);
// const playerRoute = params.get("player-id");
// const {gameId} = getItemStorage("player-" + playerRoute)

// let TOKEN = getItemStorage("user-token")
// const HEADER = {login: "marco@admin.com", passcode: "123", 'Authorization': TOKEN}
// const BASE_URL_TOPIC = `/topic/gameplay/${gameId}`;
// const BASE_URL_APP = `/app/${gameId}`;
