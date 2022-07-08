import {createDockerShip, createShips, SHIPS_NAMES} from "./ship/ships.js";
import {addClickEvent, getHTML} from "../utils/utils.js";
import {sendShips} from "./websocket/sends.js";
import {getShipsOf, isShipsDockEmpty} from "./ship/helper.js";

const setupShipsHost = (stomp) => {
  createDockerShip();
  addClickEvent("#sendShips", () => sendShips(stomp));
};
const viewClientData = ({name, score, nation, user_type}) => {
  const template = document.getElementById("TEMPLATE_PLAYER").content;
  const fragment = document.createDocumentFragment();
  const container = document.getElementById("main-container");

  const card = template.cloneNode(true);
  card
      .querySelector("[data-player]")
      .classList.add(`${user_type.toLowerCase()}-player`);
  card.querySelector(
      "[data-bg-nation]"
  ).src = `assets/icons/border-${nation}.png`;
  card.querySelector(
      "[data-logo-nation]"
  ).src = `assets/icons/select-${nation}.png`;
  card.querySelector("[data-username]").textContent = `${name}(${score})`;
  card.querySelector("#SHIPS_").id = `${user_type}_SHIPS`;
  card.querySelector("#TURN").id = `${user_type}_TURN`;

  fragment.appendChild(card);
  container.appendChild(fragment);
};

const setupTitleGame = (nation) => {
  document.getElementById("GAME_TITLE").textContent = `${nation}'s`;
  /*document.getElementById("GAME_TURN")
  document.getElementById("GRID_VIEW")*/
};

const startGame = (data) => {
  const ships = getShipsOf(data)

  createGridSalvo()
  createMySalvoGrid()

  createShipGrid(ships)
  createOpponentSalvoGrid()

  createSalvoDocker()
}

function createGridSalvo() {
  console.log("creating grid salvo...")
}

function createMySalvoGrid() {
  console.log("creating my salvo in grid...")
}

function createOpponentSalvoGrid() {
  console.log("creating opponent salvo grid")
}

function createSalvoDocker() {
  console.log("creating salvo docker...")
}

const createShipGrid = (ships) => {
  //elimino los barcos para no tener que cargar la pagina otra vez
  if (isShipsDockEmpty()) {
    SHIPS_NAMES.forEach((ship) => getHTML("#" + ship).remove());
  }

  const isVertical = locations => locations[0].substring(1) === locations[1].substring(1)

  //creo los bracos en la grilla
  ships.forEach(({locations, type}) => {
    const pivotLocation = locations[0]
    const orientation = isVertical(locations) ? "vertical" : "horizontal"
    const length = locations.length
    createShips(
        type.toLowerCase(),
        length,
        orientation,
        getHTML("#ships" + pivotLocation),
        true
    );
  })

}

export {setupShipsHost, viewClientData, startGame, setupTitleGame, createShipGrid};
