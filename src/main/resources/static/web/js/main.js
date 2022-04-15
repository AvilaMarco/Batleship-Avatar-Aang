import {cleanModal, hideModal, showModal} from "./games-mjs/modal.js";
import {createGamesOnMap, createListenerInMap, infoGame, reloadMap} from "./games-mjs/map.js";
import {createRankingTable, viewUserData, watchTutorial} from "./games-mjs/modalViews.js";
import {addClickEvent, gameSelected, getHTML} from "./utils/utils.js";
import {playerTest, randomNation} from "./utils/test.js";
import {chooseNation, inMenu, toggleView} from "./games-mjs/manageViewHTML.js";
import {createGame, joinGame, enterGame, watchGame} from "./games-mjs/manageJoinGame.js";
import {logoutFunction, setNationPlayer} from "./games-mjs/player.js";

// Load Data
let playersData = [];
let playerData = {}

// Add Events Listener
addClickEvent("#back", toggleView)
addClickEvent("#verMapa", toggleView)

addClickEvent("#hideModal", hideModal)
addClickEvent("#manageJoin", manageJoinGame)
addClickEvent("#logout", logoutFunction)
addClickEvent("#reload", () => {
  reloadMap()
  reloadInfo();
})
addClickEvent("#inicoNacion", async (event) => {
  const response = await setNationPlayer(event)
  if(response)  reloadInfo()
})

addClickEvent("#info", viewModalWithData)
addClickEvent("#infoGame", viewModalWithData)
addClickEvent("#player", viewModalWithData)
addClickEvent("#ladder", viewModalWithData)

createListenerInMap();
reloadInfo();

function reloadInfo() {
  fetch("/api/games")
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then(({player, games, players}) =>{
        playersData = players;
        playerData = player;

        playerData.nation != null ? inMenu(playerData.nation) : chooseNation();
        /*inMenu(playerData.nation)*/
        createGamesOnMap(games, playerData.id);
      })
      .catch((error) => console.log(error))
}

/* functions for change modal's view*/
function viewModalWithData({target: {id}}) {
  cleanModal(playerData.nation);
  randomNation(playerData);

  if      (id === "player")   viewUserData(playerData);
  else if (id === "info")     watchTutorial();
  else if (id === "ladder")   createRankingTable(playersData);
  else if (id === "infoGame") infoGame()

  showModal();
}

/* functions for game, join, create, enter, watch*/
function manageJoinGame({target: {name}}) {
  const {dataset: {location, id}} = gameSelected();
  const {dataset} = getHTML("div#" + id) || {dataset:"none"};

  if      (name === "Create") createGame(location, id, playerData);
  else if (name === "Enter")  enterGame(dataset, playerData)
  else if (name === "Join")   joinGame(dataset.gameid, playerData);
  else if (name === "InGame") watchGame();

}

// get form info
// Object.fromEntries(new FormData(form));

//reference to DOM
/*
const back = getHTML("#back");
back.addEventListener("click", toggleView);
const viewMap = getHTML("#verMapa");
viewMap.addEventListener("click", toggleView);
const closeModal = getHTML("#hideModal");
closeModal.addEventListener("click", hideModal);
const info = getHTML("#info");
info.addEventListener("click", viewModalWithData);
const infoDataGame = getHTML("#infoGame");
infoDataGame.addEventListener("click", viewModalWithData);
const player = getHTML("#player");
player.addEventListener("click", viewModalWithData);
const ladder = getHTML("#ladder");
ladder.addEventListener("click", viewModalWithData);
const manageJoin = getHTML("#manageJoin");
manageJoin.addEventListener("click", manageJoinGame);
const logout = getHTML("#logout");
logout.addEventListener("click", logoutFunction);
getHTML("#reload")
reload.addEventListener("click", reloadMap);
*/