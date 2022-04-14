import {cleanModal, hideModal, showModal} from "./games-mjs/modal.js";
import {createGamesOnMap, createListenerInMap, infoGame} from "./games-mjs/map.js";
import {createTableRanking, verDatosUser, verTutorial} from "./games-mjs/modalViews.js";
import {getHTML} from "./utils/utils.js";
import {randomNation} from "./utils/test.js";

//cargar datos
let gamesData = [];
let players = [
  {
    email: "AIR@correo.com",
    scores: [1, 3, 3],
  },
  {
    email: "FIRE@correo.com",
    scores: [1, 0, 3],
  },
  {
    email: "WATER@correo.com",
    scores: [3, 3, 3],
  },
  {
    email: "EARTH@correo.com",
    scores: [1, 1, 1],
  },
  {
    email: "AIR@correo.com",
    scores: [1, 2, 3],
  },
];
let playerData = {
  id: 5,
  nation: "FIRE",
  email: "marco@digitalhouse.com",
  name: "racnar1",
  stats: {
    score: 3,
    won: 1,
    tied: 0,
    lost: 0,
    win_rate: 100
  }
};
let recargarMapa = false;
//referencias al DOM
const logout = getHTML("#logout");
const verMapa = getHTML("#verMapa");
const info = getHTML("#info");
const infoDataGame = getHTML("#infoGame");
const player = getHTML("#player");
const ladder = getHTML("#ladder");
const closeModal = getHTML("#hideModal");
const manageJoin = getHTML("#manageJoin");
//eventlistener
manageJoin.addEventListener("click", manageJoinGame);
closeModal.addEventListener("click", hideModal);
infoDataGame.addEventListener("click", verdatos);
info.addEventListener("click", verdatos);
player.addEventListener("click", verdatos);
ladder.addEventListener("click", verdatos);
logout.addEventListener("click", logoutFunction);
verMapa.addEventListener("click", viewMapa);

getHTML("#inicoNacion").addEventListener("click", (event) => {
  event.preventDefault();
  if (event.target.dataset == null) return;
  const {nation} = event.target.dataset;
  setNacionPlayer(nation);
});

// COSAS
let playerScore = [];

createListenerInMap();
reloadInfo();

// para formularios a JSON
// Object.fromEntries(new FormData(formulario));

function reloadInfo() {
  fetch("/api/games")
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then(function (json) {
        console.log(json);
        gamesData = json.games;
        players = json.players;
        /*playerData = json.player;*/

        /*playerData.nation != null ? inMenu(json) : chooseNation();*/
        inMenu(playerData.nation)
        if (gamesData.length != 0) {
          console.log(gamesData);
          createGamesOnMap(gamesData, playerData.id);
        }
      })
      .catch((error) => console.log(error))
}

/*funciones para modal y cambiar los datos que muestra*/
function verdatos(e) {
  const elementhtml = e.target;
  console.log(elementhtml);
  cleanModal(playerData.nation);
  randomNation(playerData);

  if (elementhtml.id === "player") {
    verDatosUser(playerData);
  } else if (elementhtml.id === "info") {
    verTutorial();
  } else if (elementhtml.id === "ladder") {
    createTableRanking(players);
  }else if(elementhtml.id === "infoGame"){
    infoGame()
  }

  showModal();
}

function chooseNation() {
  getHTML("#inicoNacion").classList.remove("hidden");
}

function inMenu(nation) {
  getHTML("#inicoNacion").classList.add("hidden");
  getHTML("#player").classList.add("logo-" + nation);
  getHTML("#webGames").classList.remove("hidden");
  getHTML("#botonera").classList.remove("hidden");
}

function setNacionPlayer(nacion) {
  fetch("/api/players/nation/" + nacion, {
    method: "POST",
  }).then((response) => {
    if (response.ok)
      fetch("/api/games")
          .then((response) => {
            if (response.ok) return response.json();
          })
          .then((_) => reloadInfo());
  });
}

/* funciones relacionadas con los juegos, unirse, crear, volver*/
function manageJoinGame(event) {
  let a = getHTML("div#" + getHTML("div[data-name*='selectGame']").dataset.id);
  console.log(a)
  const {name} = event.target
  console.log(name)
  if (name === "Create") {
    let data = getHTML("div[data-name*=selectGame]");
    crearjuego(data.dataset.location, data.dataset.id);
  } else if (name === "Enter") {
    const isPlayer1 = a.dataset.playerid1 === playerData.id;
    playerData.gamePlayerId = isPlayer1 ? a.dataset.gpid1 : a.dataset.gpid2;
    playerData.gameId = a.dataset.gameId;
    saveUserData();
    goGame();
  } else if (name === "Join") {
    joinGame(a.dataset.gameid);
  } else if (name === "InGame") {
    alert("Coming soon spectator mode");
  }
}

function joinGame(gameid) {
  fetch(`/api/match/games/${gameid}`, {method: "POST"})
      .then((res) => (res.ok ? res.json() : Promise.reject(res.json())))
      .then((json) => {
        playerData.gamePlayerId = json.game_player_id;
        playerData.gameId = json.game_id;
        saveUserData();
        goGame();
      })
      .catch(function (error) {
        console.log("Hubo un problema con la petición Fetch:" + error.message);
      });
}

function crearjuego(nation, location) {
  fetch(`/api/match/games/${nation}/${location}`, {method: "POST"})
      .then((res) => (res.ok ? res.json() : Promise.reject(res.json())))
      .then((json) => {
        playerData.gamePlayerId = json.game_player_id;
        playerData.gameId = json.game_id;
        saveUserData();
        goGame();
      })
      .catch((error) => error)
      .then((json) => console.log(json));
}

function viewMenu() {
  getHTML("#mapabg").classList.remove("hidden");
  getHTML("#back").classList.add("hidden");
  getHTML("#reload").classList.add("hidden");
  document
      .querySelectorAll("button[name*='botonesMenu']")
      .forEach((e) => e.classList.remove("hidden"));
  document
      .querySelectorAll("div [name*='dataGame']")
      .forEach((e) => e.classList.add("hidden"));
}

function viewMapa(event) {
  let back = getHTML("#back");
  back.classList.remove("hidden");
  back.addEventListener("click", viewMenu);

  let reload = getHTML("#reload");
  reload.classList.remove("hidden");
  reload.addEventListener("click", function () {
    recargarMapa = true;
    reloadInfo();
    reload.classList.add("Animation-reload");
    setTimeout(function () {
      getHTML("#reload").classList.remove("Animation-reload");
    }, 1100);
  });

  getHTML("#mapabg").classList.add("hidden");
  document
      .querySelectorAll("button[name*='botonesMenu']")
      .forEach((e) => e.classList.add("hidden"));
}

//LOGOUT
function logoutFunction() {
  fetch("/api/logout", {method: "POST"}).then(() => location.assign("/"));
}

function saveUserData() {
  let userData = JSON.stringify(playerData);
  localStorage.setItem("player", userData);
}

function goGame() {
  location.assign("/web/game.html");
}
