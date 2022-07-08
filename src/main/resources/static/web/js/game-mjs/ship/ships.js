import {getHTML} from "../../utils/utils.js";

import {rotateShips} from "./rotate_ship.js";
import {checkBusyCells, isShipOffBounds} from "./validations.js";
import {updateConsole} from "../console.js";
/*creates the ships with the ability of been placed in the grid. 
It requires a shipType, that is, the id by wich the ship will be recongnized;
the amount of cells the ship is going to occupy in the grid;
a parent where the ship will be appended to;
and a boolean that specifies whether the ship can be moved or not.
*/
const SHIPS_NAMES = [
  "carrier"
  // "battleship",
  // "submarine",
  // "destroyer",
  // "patrol_boat",
];

const createShips = (shipType, length, orientation, parent, isStatic) => {
  let ship = document.createElement("DIV");
  let grip = document.createElement("DIV");
  let content = document.createElement("DIV");

  ship.classList.add("grid-item");
  ship.dataset.length = length;
  ship.dataset.orientation = orientation;
  ship.id = shipType;

  if (orientation === "vertical") {
    ship.style.transform = "rotate(90deg)";
  }

  resizeShip();

  window.addEventListener("resize", () => {
    resizeShip();
  });

  if (!isStatic) {
    grip.classList.add("grip");
    ship.draggable = "true";
    // Events drag
    ship.addEventListener("dragstart", dragShip);
    // Events drag mobile
    ship.addEventListener("touchmove", touchShip);
    ship.addEventListener("touchend", touchShipEnd);

    ship.appendChild(grip);
  }

  content.classList.add("grid-item-content");
  ship.appendChild(content);

  parent.appendChild(ship);

  if (!isStatic) {
    rotateShips(shipType);
  } else {
    checkBusyCells(ship, parent);
  }

  function resizeShip() {
    if (window.innerWidth >= 768) {
      ship.style.width = `${length * 45}px`;
      ship.style.height = "45px";
    } else if (window.innerWidth >= 576) {
      ship.style.width = `${length * 35}px`;
      ship.style.height = "35px";
    } else {
      ship.style.width = `${length * 30}px`;
      ship.style.height = "30px";
    }
  }

  //event to allow the ship beeing dragged
  function dragShip(ev) {
    // ev.preventDefault();
    // if (!ev.target.classList.contains("grip")) return;
    ev.dataTransfer.setData("ship", ev.target.id);
  }

  //event to allow the ship beeing dragged on touch devices
  function touchShip(ev) {
    // make the element draggable by giving it an absolute position and modifying the x and y coordinates
    ship.classList.add("fixed");
    const {pageX, pageY} = ev.targetTouches[0];
    // Place element where the finger is
    ship.style.left = pageX - 25 + "px";
    ship.style.top = pageY - 25 + "px";
    ev.preventDefault();
  }

  function touchShipEnd(ev) {
    // hide the draggable element, or the elementFromPoint won't find what's underneath
    ship.style.left = "-1000px";
    ship.style.top = "-1000px";

    // find the element on the last draggable position
    let endTarget = document.elementFromPoint(
        ev.changedTouches[0].pageX,
        ev.changedTouches[0].pageY
    );

    // position it relative again and remove the inline styles that aren't needed anymore
    ship.classList.remove("fixed");
    ship.style.left = "";
    ship.style.top = "";

    // put the draggable into it's new home
    if (endTarget.classList.contains("cell-position")) {
      endTarget = endTarget.parentElement;
    }

    if (!endTarget.classList.contains("grid-cell"))
      return updateConsole("movement not allowed");

    if (isShipOffBounds(endTarget, ship)) return;

    const {x, y} = endTarget.dataset;
    endTarget.appendChild(ship);
    ship.dataset.x = x;
    ship.dataset.y = y;

    checkBusyCells(ship, endTarget);

    /*dockIsEmpty();*/
  }
};

function createDockerShip() {
  // const shipsLength = [4, 4, 3, 3, 2];

  const shipsLength = [4];
  shipsLength.forEach((lengthShip, i) =>
      createShips(
          SHIPS_NAMES[i],
          lengthShip,
          "horizontal",
          getHTML("#ships"),
          false
      )
  );
}

export {createShips, createDockerShip, SHIPS_NAMES};