import {postToken} from "../utils/payload.js";

function createGame(nation, location, playerData, token) {
  fetch(`/api/match/games/${nation}/${location}`, postToken(token))
      .then((res) => (res.ok ? res.json() : Promise.reject(res.json())))
      .then((json) => {
        goGame(json, playerData)
      })
      .catch((error) => error)
      .then((json) => console.log(json));
}

function enterGame({playerid1, gpid1, gpid2, gameid}, playerData) {
  const isPlayer1 = playerid1 === playerData.id;
  const gpid = isPlayer1 ? gpid1 : gpid2;
  goGame({game_id: gameid, game_player_id: gpid}, playerData)
}

function joinGame(gameid, playerData, token) {
  fetch(`/api/match/games/${gameid}`, postToken(token))
      .then((res) => (res.ok ? res.json() : Promise.reject(res.json())))
      .then((json) => {
        goGame(json, playerData)
      })
      .catch(function (error) {
        console.log("Hubo un problema con la petición Fetch:" + error.message);
      });
}

function watchGame() {
  alert("Coming soon spectator mode");
}

export {createGame, enterGame, joinGame, watchGame}

// UTILS

function goGame({game_id, game_player_id}, playerData) {
  playerData.gamePlayerId = game_player_id;
  playerData.gameId = game_id;
  saveUserData(playerData);
  goWebGame(playerData.id);
}

function saveUserData(playerData) {
  let userData = JSON.stringify(playerData);
  localStorage.setItem("player-" + playerData.id, userData);
}

function goWebGame(playerId) {
  location.assign(`/web/game.html?player-id=${playerId}`);
}

