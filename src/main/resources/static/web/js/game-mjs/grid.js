import { GRID_SIZE } from "../game-mjs/CONSTANTS.js";

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
      cell.addEventListener("drop", (event) => dropShip(event));
      //cell.addEventListener('touchmove', function(event) {dropShip(event)})
      cell.addEventListener("dragover", (event) => allowDrop(event));
      //cell.addEventListener('touchend',function(event) {allowDrop(event)})

      //if j is equal to 0, the cells belongs to the first colummn, so the letter is added as text node
      if (j === 1) {
        let cellPositionCol = document.createElement("DIV");
        cellPositionCol.classList.add("cell-position", "cell-position-col");
        let textNode = document.createElement("SPAN");
        textNode.innerText = i;
        cellPositionCol.appendChild(textNode);
        cell.appendChild(cellPositionCol);
      }

      //if i is equal to 0, the cells belongs to the first row, so the number is added as text node
      if (i === size - 1) {
        let cellPositionRow = document.createElement("DIV");
        cellPositionRow.classList.add("cell-position", "cell-position-row");
        let textNode = document.createElement("SPAN");
        textNode.innerText = String.fromCharCode(j + 64);
        cellPositionRow.appendChild(textNode);
        cell.appendChild(cellPositionRow);
      }

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
    /*document.querySelector("#display p").innerText = "movement not allowed";*/
    return;
  }
  //variables where the data of the ship beeing dragged is stored
  let data = ev.dataTransfer.getData("ship");
  let ship = document.getElementById(data);
  //variables where the data of the targeted cell is stored
  let cell = ev.target;
  let y = cell.dataset.y.charCodeAt() - 64;
  let x = parseInt(cell.dataset.x);

  //Before the ship is dropped to a cell, checks if the length of the ship exceed the grid width,
  //If true, the drop event is aborted.
  if (ship.dataset.orientation === "horizontal") {
    if (parseInt(ship.dataset.length) + x > GRID_SIZE) {
      /*document.querySelector("#display p").innerText = "movement not allowed";*/
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
        /*document.querySelector("#display p").innerText = "careful";*/
        return;
      }
    }
  } else {
    if (parseInt(ship.dataset.length) + y > GRID_SIZE) {
      /*document.querySelector("#display p").innerText = "movement not allowed";*/
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
        /*document.querySelector("#display p").innerText = "careful";*/
        return;
      }
    }
  }
  //Else:
  //the ship takes the position data of the targeted cell
  ship.dataset.y = String.fromCharCode(y + 64);
  ship.dataset.x = x;
  //the ship is added to the cell
  ev.target.appendChild(ship);
  /*dockIsEmpty();*/

  //checkBusyCells(ship, ev.target);
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
