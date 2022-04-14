import {locations} from "./datosmap.js";
import {gameSelected, getAllHTML, getHTML, resizeCoord, resizeCoordWithMargin,} from "../utils/utils.js";

/*crear area clickable para la imagen*/
function createListenerInMap() {
  locations.forEach((area) => {
    const areahtml = document.createElement("area");
    const {id, location, x, y, r} = area;
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

function createGamesOnMap(games, playerId) {
  Array.from(getHTML("#pivotMap").children).forEach((e) => e.remove());
  games.forEach((game) => {
    const {id, coords, dataset} = getHTML("#" + game.location);
    const [width, height] = resizeCoordWithMargin(coords);

    const div = document.createElement("div");
    div.id = id;
    div.classList.add("selectMap");
    div.style.left = width + "px";
    div.style.top = height + "px";
    div.coords = coords

    div.dataset.game = "true";
    div.dataset.location = dataset.location;
    div.addEventListener("click", selectGame);

    div.dataset.info = JSON.stringify(game);

    div.dataset.gameid = game.id;

    if (game.game_players.some(gp => gp.player.id === playerId)) {
      div.classList.add("SelectEnter");
      div.dataset.name = "Enter";
    } else if (game.game_players.length === 1) {
      div.classList.add("selectJoin");
      div.dataset.name = "Join";
    } else if (game.game_players.length === 2) {
      div.classList.add("SelectInGame");
      div.dataset.name = "InGame";
    }

    if (game.game_players.length == 2) {
      div.dataset.gpid1 = game.game_players[0].id;
      div.dataset.playerid1 = game.game_players[0].player.id;
      div.dataset.playername1 = game.game_players[0].player.email;

      div.dataset.gpid2 = game.game_players[1].id;
      div.dataset.playerid2 = game.game_players[1].player.id;
      div.dataset.playername2 = game.game_players[1].player.email;

    } else {
      div.dataset.gpid1 = game.game_players[0].id;
      div.dataset.playerid1 = game.game_players[0].player.id;
      div.dataset.playername1 = game.game_players[0].player.email;

    }
    console.log(div)
    getHTML("#pivotMap").appendChild(div);

  });
}

function selectGame(event) {
  if (gameSelected() != null) {
    getHTML("#pivotMap").removeChild(gameSelected());
    getHTML("#infoGame").classList.add("hidden")
  }

  const {id, dataset, coords} = event.target;
  const [width, height] = resizeCoordWithMargin(coords);
  const manageJoin = getHTML("#manageJoin");

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
    manageJoin.innerText = dataset.name;
    manageJoin.name = dataset.name
    getHTML("#infoGame").classList.remove("hidden")
  } else {
    div.classList.add("selectCreate");
    manageJoin.innerText = "Create"
    manageJoin.name = "Create"
  }
  manageJoin.classList.remove("hidden")
  getHTML("#pivotMap").appendChild(div);
  console.log(div);
}

function unselectGame(event) {
  getHTML("#pivotMap").removeChild(event.target);
  getAllHTML("div [name*='dataGame']")
      .forEach((e) => e.classList.add("hidden"));
}

function infoGame() {
  const {dataset: {id}} = gameSelected();
  const {dataset: {location, gameid, playername1, playername2}} = getHTML("div#" + id);
  const thereP2 = playername2 !== undefined
  const body = `
    <div class="div-infoGame" style="background: white;">
        <h1>${location} Nation</h1>
        <div class="divStatus">
            <p>Game Id: ${gameid}</p>
            <p>Status: ${thereP2 ? "In Game" : "Create"}</p>
        </div>
        <p>Player 1: ${playername1}</p>
        <p>Player 2: ${thereP2 ? playername2 : "-"}</p>
    </div>`

  console.log(getHTML("div#" + id))
  let modalDiv = getHTML(".div-modal");
  modalDiv.innerHTML = body
}

export {createListenerInMap, createGamesOnMap, infoGame};
