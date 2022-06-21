import {getItemStorage} from "../../utils/utils.js";

const params = new URLSearchParams(location.search);
const playerRoute = params.get("player-id");
const {gameId, email} = getItemStorage("player-" + playerRoute)

let TOKEN = getItemStorage("user-token")
const HEADER = {login: email, 'Authorization': TOKEN}

export {gameId, TOKEN, HEADER}