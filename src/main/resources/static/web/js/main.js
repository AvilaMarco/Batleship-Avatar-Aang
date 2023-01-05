import {cleanModal, hideModal, showModal} from "./games-mjs/modal.js";
import {createGamesOnMap, createListenerInMap, infoGame, reloadMap,} from "./games-mjs/map.js";
import {createRankingTable, viewUserData, watchTutorial,} from "./games-mjs/modalViews.js";
import {chooseNation, inMenu, toggleView,} from "./games-mjs/manageViewHTML.js";
import {createGame, enterGame, joinGame, watchGame,} from "./games-mjs/manageJoinGame.js";
import {logoutFunction, setNationPlayer} from "./games-mjs/player.js";
import {addClickEvent, gameSelected, getHTML} from "./utils/utils.js";
import {getToken} from "./utils/payload.js";
import {randomNation} from "./utils/test.js";

// Load Data
let playersData = [];
let playerData = {};
const TOKEN = getUserToken();

// Add Events Listener
(function () {
  addClickEvent("#back", toggleView);
  addClickEvent("#verMapa", toggleView);

  addClickEvent("#hideModal", hideModal);
  addClickEvent("#manageJoin", manageJoinGame);
  addClickEvent("#logout", () => logoutFunction(TOKEN));
  addClickEvent("#reload", () => {
    reloadMap();
    reloadInfo();
  });
  addClickEvent("#inicoNacion", async (event) => {
    const response = await setNationPlayer(event, TOKEN);
    if (response) reloadInfo();
  });

  addClickEvent("#info", viewModalWithData);
  addClickEvent("#infoGame", viewModalWithData);
  addClickEvent("#player", viewModalWithData);
  addClickEvent("#ladder", viewModalWithData);
})()

createListenerInMap();
reloadInfo();

function reloadInfo() {
  fetch("/api/games", getToken(TOKEN))
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then(({player, games, players}) => {
        playersData = players;
        playerData = player;

        playerData.nation != null ? inMenu(playerData.nation) : chooseNation();
        /*inMenu(playerData.nation)*/
        createGamesOnMap(games, playerData.id);
      })
      .catch((error) => console.log(error));
}

/* functions for change modal's view*/
function viewModalWithData({target: {id}}) {
  cleanModal(playerData.nation);
  randomNation(playerData);

  if (id === "player") viewUserData(playerData);
  else if (id === "info") watchTutorial();
  else if (id === "ladder") createRankingTable(playersData);
  else if (id === "infoGame") infoGame();

  showModal();
}

/* functions for game, join, create, enter, watch*/
function manageJoinGame({target: {name}}) {
  const {
    dataset: {location, id},
  } = gameSelected();
  const {dataset} = getHTML("div#" + id) || {dataset: "none"};

  if (name === "Create") createGame(location, id, playerData, TOKEN);
  else if (name === "Enter") enterGame(dataset, playerData);
  else if (name === "Join") joinGame(dataset.gameid, playerData, TOKEN);
  else if (name === "InGame") watchGame();
}

function getUserToken() {
  const jsonToken = localStorage.getItem("user-token");
  return JSON.parse(jsonToken);
}