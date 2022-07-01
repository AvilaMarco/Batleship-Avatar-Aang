import {getHTML} from "../../utils/utils.js";
import {updateConsole} from "../console.js";
import {GRID_SIZE} from "../CONSTANTS.js";
import {checkBusyCells, isHorizontal, isShipOffBounds} from "./validations.js";

//event to allow the ship rotation
const rotateShips = (shipType) => {
  getHTML(`#${shipType}`).addEventListener("click", (ev) => {
    updateConsole("");
    console.log(ev.target);
    if (!ev.target.classList.contains("grip")) return;

    const ship = ev.target.parentNode;
    const cell = ship.parentElement;

    if (!cell.classList.contains("grid-cell")) return;

    ship.dataset.orientation = isHorizontal(ship) ? "vertical" : "horizontal"
    if (isShipOffBounds(cell, ship)) {
      return ship.dataset.orientation = isHorizontal(ship) ? "vertical" : "horizontal"
    }

    ship.style.transform = isHorizontal(ship) ? "rotate(90deg)" : "rotate(360deg)";

    checkBusyCells(ship, cell);

  });
};

export {rotateShips};
