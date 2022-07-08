import {playerRoute} from "../websocket/data_connection.js";
import {getHTML, list} from "../../utils/utils.js";
import {SHIPS_NAMES} from "./ships.js";

const getShipsOf = data =>
    data.game_players
        .filter(gp => gp.player.id === playerRoute)
        .flatMap(gp => gp.ships)

const getShipsOnGrid = () =>
    SHIPS_NAMES.map((ship) => {
      const {length, orientation, x, y} = getHTML("#" + ship).dataset;
      const position = list(parseInt(length)).map((_, i) =>
          orientation === "horizontal"
              ? y + (parseInt(x) + i)
              : String.fromCharCode(y.charCodeAt(0) + i) + x
      );
      return {
        type: ship.toUpperCase(),
        locations: position,
      };
    });

const isShipsDockEmpty = () =>
    SHIPS_NAMES.some((ship) => getHTML("#" + ship).dataset.y === undefined);

export {getShipsOf, getShipsOnGrid, isShipsDockEmpty}