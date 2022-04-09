import { locations } from "./datosmap.mjs";
import {
  getHTML,
  gameSelected,
  resizeCoord,
  resizeCoordWithMargin,
} from "../utils/utils.mjs";

/*crear area clickable para la imagen*/
function createListenerInMap() {
  locations.forEach((area) => {
    const areahtml = document.createElement("area");
    const { id, location, x, y, r } = area;
    areahtml.dataset.location = location;
    areahtml.id = id;
    areahtml.shape = "circle";
    areahtml.alt = "aremap";
    areahtml.coords = `${resizeCoord(x, "width")}, ${resizeCoord(
      y,
      "height"
    )}, ${r}`;
    areahtml.addEventListener("click", selectGame);
    getHTML("map[name*=mapeo]").appendChild(areahtml);
  });
}

function createGamesOnMap(games) {
  Array.from(getHTML("#pivotMap").children).forEach((e) => e.remove());
  games.forEach((e) => {
    if (e.game_players[0].Score == null) {
      const { id, coords, dataset } = getHTML("#" + e.direction);
      const [width, height] = resizeCoordWithMargin(coords);
      const div = document.createElement("div");
      div.id = id;
      div.classList.add("selectMap");
      div.style.left = width + "px";
      div.style.top = height + "px";
      div.dataset.game = "true";
      div.dataset.gameid = e.id;
      div.dataset.location = dataset.location;

      div.addEventListener("click", selectGame);
      if (e.game_players.length == 2) {
        div.dataset.gpid1 = e.game_players[0].id;
        div.dataset.playerid1 = e.game_players[0].player.id;
        div.dataset.playername1 = e.game_players[0].player.email;

        div.dataset.gpid2 = e.game_players[1].id;
        div.dataset.playerid2 = e.game_players[1].player.id;
        div.dataset.playername2 = e.game_players[1].player.email;

        if (e.game_players.some((f) => f.player.id == playerData.id)) {
          div.dataset.name = "Enter";
          div.classList.add("SelectEnter");
        } else {
          div.classList.add("SelectInGame");
          div.dataset.name = "InGame";
        }
      } else {
        div.dataset.gpid1 = e.game_players[0].id;
        div.dataset.playerid1 = e.game_players[0].player.id;
        div.dataset.playername1 = e.game_players[0].player.email;

        if (e.game_players.some((f) => f.player.id == playerData.id)) {
          div.dataset.name = "Enter";
          div.classList.add("SelectEnter");
        } else {
          div.classList.add("selectJoin");
          div.dataset.name = "Join";
        }
      }

      getHTML("#pivotMap").appendChild(div);
    }
  });
}

function selectGame(event) {
  if (gameSelected() != null) {
    document.querySelector("#pivotMap").removeChild(gameSelected());
  }

  const { id, dataset, coords } = event.target;
  const [width, height] = resizeCoordWithMargin(coords);
  let div = document.createElement("div");
  div.dataset.id = id;
  div.classList.add("selectMap");
  div.style.left = width + "px";
  div.style.top = height + "px";
  div.dataset.name = "selectGame";
  div.dataset.location = dataset.location;
  div.addEventListener("click", unselectGame);

  if (dataset.game == "true") {
    div.classList.add("selectGameCreate");
    document.querySelectorAll("div [name*='dataGame']").forEach((e) => {
      e.classList.remove("hidden");
      if (!(e.innerText == "Info") && !(dataset.playerid1 == playerData.id)) {
        e.innerText = dataset.name;
      } else if (
        !(e.innerText == "Info") &&
        dataset.playerid1 == playerData.id
      ) {
        e.innerText = "Enter";
      }
    });
  } else {
    div.classList.add("selectCreate");

    document.querySelectorAll("div [name*='dataGame']").forEach((e) => {
      e.classList.remove("hidden");
      if (!(e.innerText == "Info")) {
        e.innerText = "Create";
      }
    });
  }

  getHTML("#pivotMap").appendChild(div);
  if (getHTML("div#" + gameSelected().dataset.id) == undefined) {
    getHTML("#infoGame").classList.add("hidden");
  }
  console.log(div);
}

function unselectGame(event) {
  getHTML("#pivotMap").removeChild(event.target);
  document
    .querySelectorAll("div [name*='dataGame']")
    .forEach((e) => e.classList.add("hidden"));
}

function infoGame() {
  showModal();
  let modalDiv = getHTML(".div-modal");
  modalDiv.innerHTML = "";
  modalDiv.classList.remove("bg" + playerData.nation);
  let select = getHTML("div#" + gameSelected().dataset.id);
  let div = document.createElement("div");
  div.classList.add("div-infoGame");
  let title = document.createElement("H1");
  title.style.margin = "0";
  let textoPlayer1 = document.createElement("P");
  let textoPlayer2 = document.createElement("P");
  let divStatus = document.createElement("div");
  divStatus.classList.add("divStatus");
  let textoId = document.createElement("P");
  let textoStatus = document.createElement("P");

  title.innerText = "Nacion de " + select.dataset.location;
  textoId.innerText = "Game Id: " + select.dataset.gameid;
  textoPlayer1.innerText = "Player 1: " + select.dataset.playername1;
  if (select.dataset.playername2 != undefined) {
    textoPlayer2.innerText = "Player 2: " + select.dataset.playername2;
    textoStatus.innerText = "Status: In Game";
  } else {
    textoPlayer2.innerText = "Player 2: -";
    textoStatus.innerText = "Status: Create";
  }

  divStatus.appendChild(textoId);
  divStatus.appendChild(textoStatus);
  div.appendChild(title);
  div.appendChild(divStatus);
  div.appendChild(textoPlayer1);
  div.appendChild(textoPlayer2);
  modalDiv.appendChild(div);
}

export { createListenerInMap, createGamesOnMap, infoGame };
