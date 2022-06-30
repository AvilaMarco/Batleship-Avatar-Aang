import { GRID_SIZE } from "../game-mjs/CONSTANTS.js";
import { updateConsole } from "./console.js";
import { isShipOffBounds } from "./ship/validations.js";
/*creates the grid structure. It requires a size, an element 
where the grid will be attached to and an id to recognized it. 
*/
export const createGrid = function (size, element, id, idGrid) {
  let wrapper = document.createElement("DIV"); //container of the grid
  wrapper.classList.add("grid-wrapper");
  wrapper.id = idGrid;
  if (id === "ships") {
    wrapper.classList.add("cero");
  }

  //the first loop creates the rows of the grid
  for (let i = 1; i < size; i++) {
    let row = document.createElement("DIV");
    row.classList.add("grid-row");
    row.id = `${id}-grid-row${i}`;
    wrapper.appendChild(row); // appends the row created in each itaration to the container

    //the second loop creates the amount of cells needed given the size of the grid for every row
    for (let j = 1; j < size; j++) {
      let cell = document.createElement("DIV");
      cell.classList.add("grid-cell");

      //if j and i are greater than 0, the drop event is activated
      cell.id = `${id}${String.fromCharCode(i - 1 + 65)}${j}`;
      cell.dataset.y = String.fromCharCode(i - 1 + 65);
      cell.dataset.x = j;
      // drag and drop - desktop
      cell.addEventListener("dragover", (event) => allowDrop(event));
      cell.addEventListener("drop", (event) => dropShip(event));
      // drag and drop - mobile
      // cell.addEventListener("touchmove", (event) => allowDrop(event));
      // cell.addEventListener("touchend", (event) => dropShip(event));

      //if j is equal to 0, the cells belongs to the first colummn, so the letter is added as text node
      if (j === 1) {
        let cellPositionCol = document.createElement("DIV");
        cellPositionCol.classList.add("cell-position", "cell-position-col");
        let textNode = document.createElement("SPAN");
        textNode.innerText = i;
        cellPositionCol.appendChild(textNode);
        cell.appendChild(cellPositionCol);

        if (i === 1) cell.classList.add("corner-top-left");
        if (i === size - 1) cell.classList.add("corner-bottom-left");
      }

      //if i is equal to 0, the cells belongs to the first row, so the number is added as text node
      if (i === size - 1) {
        let cellPositionRow = document.createElement("DIV");
        cellPositionRow.classList.add("cell-position", "cell-position-row");
        let textNode = document.createElement("SPAN");
        textNode.innerText = String.fromCharCode(j + 64);
        cellPositionRow.appendChild(textNode);
        cell.appendChild(cellPositionRow);

        if (j === size - 1) cell.classList.add("corner-bottom-rigth");
      }

      if (i === 1 && j === size - 1) cell.classList.add("corner-top-rigth");

      row.appendChild(cell);
    }
  }

  element.appendChild(wrapper);
};

// createGrid(9, document.getElementById("grid"), "ships", "gridShips");
//createShips('carrier', 5, 'horizontal', document.getElementById('shipsA1'),true)
//Event to allow the drop event.
function allowDrop(ev) {
  // dockIsEmpty()
  ev.preventDefault();
}

//Event to manage what happen when a ship is dropped
function dropShip(ev) {
  ev.preventDefault();
  /*document.querySelector("#display p").innerText = "";*/
  //checks if the targeted element is a cell
  if (!ev.target.classList.contains("grid-cell")) {
    return updateConsole("movement not allowed");
  }
  //variables where the data of the ship beeing dragged is stored
  let data = ev.dataTransfer.getData("ship");
  let ship = document.getElementById(data);
  //variables where the data of the targeted cell is stored
  let cell = ev.target;

  //Before the ship is dropped to a cell, checks if the length of the ship exceed the grid width,
  //If true, the drop event is aborted.
  console.log(isShipOffBounds(cell, ship));

  //Else:
  //the ship takes the position data of the targeted cell
  const { x, y } = cell.dataset;
  ship.dataset.y = y;
  ship.dataset.x = x;
  //the ship is added to the cell
  cell.appendChild(ship);
  /*dockIsEmpty();*/

  checkBusyCells(ship, cell);
}

function checkBusyCells(ship, cell) {
  let id = cell.id
    .match(new RegExp(`[^${cell.dataset.y}|^${cell.dataset.x}]`, "g"))
    .join("");
  let y = cell.dataset.y.charCodeAt() - 64;
  let x = parseInt(cell.dataset.x);

  document.querySelectorAll(`.${ship.id}-busy-cell`).forEach((cell) => {
    cell.classList.remove(`${ship.id}-busy-cell`);
  });

  for (let i = 0; i < ship.dataset.length; i++) {
    if (ship.dataset.orientation === "horizontal") {
      document
        .querySelector(`#${id}${String.fromCharCode(y + 64)}${x + i}`)
        .classList.add(`${ship.id}-busy-cell`);
    } else {
      document
        .querySelector(`#${id}${String.fromCharCode(y + 64 + i)}${x}`)
        .classList.add(`${ship.id}-busy-cell`);
    }
  }
}
