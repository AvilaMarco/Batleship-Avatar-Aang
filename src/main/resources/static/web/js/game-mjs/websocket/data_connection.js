import {getItemStorage} from "../../utils/utils.js";

const params = new URLSearchParams(location.search);
const playerRoute = parseInt(params.get("player-id"));
const {gameId, email} = getItemStorage("player-" + playerRoute)
// const { gameId, email } = { gameId: 1, email: "asds" };

let TOKEN = getItemStorage("user-token");
const HEADER = {login: email, Authorization: TOKEN};

export {gameId, TOKEN, HEADER, playerRoute};
