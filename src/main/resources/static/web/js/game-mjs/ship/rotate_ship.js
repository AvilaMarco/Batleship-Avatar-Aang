import { getHTML } from "../../utils/utils.js";
import { updateConsole } from "../console.js";
import { GRID_SIZE } from "../CONSTANTS.js";
import { checkBusyCells } from "./validations.js";

//event to allow the ship rotation
const rotateShips = (shipType) => {
  getHTML(`#${shipType}`).addEventListener("click", function (ev) {
    updateConsole("");
    console.log(ev.target);
    if (!ev.target.classList.contains("grip")) return;

    let ship = ev.target.parentNode;
    let orientation = ship.dataset.orientation;
    let cell = ship.parentElement.classList.contains("grid-cell")
      ? ship.parentElement
      : null;

    if (cell != null) {
      if (orientation === "horizontal") {
        if (
          parseInt(ship.dataset.length) + (cell.dataset.y.charCodeAt() - 64) >
          GRID_SIZE
        ) {
          updateConsole("careful");
          return;
        }

        for (let i = 1; i < ship.dataset.length; i++) {
          let id = cell.id
            .match(new RegExp(`[^${cell.dataset.y}|^${cell.dataset.x}]`, "g"))
            .join("");
          let cellId = `${id}${String.fromCharCode(
            cell.dataset.y.charCodeAt() + i
          )}${cell.dataset.x}`;
          if (
            document.getElementById(cellId).className.search(/busy-cell/) !== -1
          ) {
            updateConsole("careful");
            return;
          }
        }
      } else {
        if (
          parseInt(ship.dataset.length) + parseInt(cell.dataset.x) >
          GRID_SIZE
        ) {
          updateConsole("careful");
          return;
        }

        for (let i = 1; i < ship.dataset.length; i++) {
          let id = cell.id
            .match(new RegExp(`[^${cell.dataset.y}|^${cell.dataset.x}]`, "g"))
            .join("");
          let cellId = `${id}${cell.dataset.y}${parseInt(cell.dataset.x) + i}`;
          if (
            document.getElementById(cellId).className.search(/busy-cell/) !== -1
          ) {
            updateConsole("careful");
            return;
          }
        }
      }
    }

    if (orientation == "horizontal") {
      ship.dataset.orientation = "vertical";
      ship.style.transform = "rotate(90deg)";
    } else {
      ship.dataset.orientation = "horizontal";
      ship.style.transform = "rotate(360deg)";
    }
    if (cell != null) {
      checkBusyCells(ship, cell);
    }
  });
};

export { rotateShips };
