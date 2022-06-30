import { getAllHTML, getHTML } from "../../utils/utils.js";
import { GRID_SIZE } from "../CONSTANTS.js";
import { updateConsole } from "../console.js";

const checkBusyCells = (ship, cell) => {
  getAllHTML(`.${ship.id}-busy-cell`).forEach((cell) => {
    cell.classList.remove(`${ship.id}-busy-cell`);
  });

  const { x, y } = cell.dataset;
  let id = getMainCellId(cell, x, y);
  let codeY = y.charCodeAt() - 64;
  let codeX = parseInt(x);

  for (let i = 0; i < ship.dataset.length; i++) {
    let row = String.fromCharCode(codeY + 64 + (!isHorizontal(ship) ? i : 0));
    let col = codeX + (isHorizontal(ship) ? i : 0);
    const cell = getHTML(`#${id}${row}${col}`);
    cell.classList.add(`${ship.id}-busy-cell`);
  }
};

const isShipOffBounds = (endTarget, ship) => {
  const { x, y } = endTarget.dataset;
  const codeY = y.charCodeAt() - 64;
  const codeX = parseInt(x);

  const lengthShip = parseInt(ship.dataset.length);
  const id = getMainCellId(endTarget, x, y);

  if (isHorizontal(ship)) {
    if (lengthShip + codeX > GRID_SIZE)
      return updateConsole("movement not allowed");
    for (let i = 1; i < lengthShip; i++) {
      let cellId = `${id}${y}${codeX + i}`;
      if (cellIsFull(cellId)) return updateConsole("careful");
    }
  } else {
    if (lengthShip + codeY > GRID_SIZE)
      return updateConsole("movement not allowed");
    for (let i = 1; i < lengthShip; i++) {
      let cellId = `${id}${String.fromCharCode(y.charCodeAt() + i)}${codeX}`;
      if (cellIsFull(cellId)) return updateConsole("careful");
    }
  }
};

function isHorizontal(ship) {
  return ship.dataset.orientation === "horizontal";
}

function cellIsFull(cellId) {
  return getHTML("#" + cellId).className.search(/busy-cell/) !== -1;
}

function getMainCellId(cell, x, y) {
  return cell.id.match(new RegExp(`[^${y}|^${x}]`, "g")).join("");
}

export { checkBusyCells, isShipOffBounds };
