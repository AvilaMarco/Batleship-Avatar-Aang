import { createDockerShip } from "./ship/ships.js";
import { addClickEvent } from "../utils/utils.js";
import { sendShips } from "./websocket/sends.js";

const setupShipsHost = (stomp) => {
  createDockerShip();
  addClickEvent("#sendShips", () => sendShips(stomp));
};
const viewClientData = ({ name, score, nation, user_type }) => {
  const template = document.getElementById("TEMPLATE_PLAYER").content;
  const fragment = document.createDocumentFragment();
  const container = document.getElementById("main-container");

  const playerCard = template.cloneNode(true);
  playerCard
    .querySelector("[data-player]")
    .classList.add(`${user_type.toLowerCase()}-player`);
  playerCard.querySelector(
    "[data-bg-nation]"
  ).src = `assets/icons/border-${nation}.png`;
  playerCard.querySelector(
    "[data-logo-nation]"
  ).src = `assets/icons/select-${nation}.png`;
  playerCard.querySelector("[data-username]").textContent = `${name}(${score})`;
  playerCard.querySelector("#SHIPS_").id = `${user_type}_SHIPS`;
  playerCard.querySelector("#TURN").id = `${user_type}_TURN`;

  fragment.appendChild(playerCard);
  container.appendChild(fragment);
  console.log(fragment);
};

const setupTitleGame = (nation) => {
  document.getElementById("GAME_TITLE").textContent = `${nation}'s Nation`;
  /*document.getElementById("GAME_TURN")
  document.getElementById("GRID_VIEW")*/
};

function startGame() {
  createSalvoGrid();
  createDockerSalvos();
  // decidir si lamar o no a la funcion
  createStartedGame();
}

function createSalvoGrid() {}

function createDockerSalvos() {}

function createStartedGame() {
  // crear barcos
  // crear mis salvos
  // crear salvos del oponente
}

export { setupShipsHost, viewClientData, startGame, setupTitleGame };
