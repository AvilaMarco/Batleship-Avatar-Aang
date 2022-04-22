import { createGrid } from "./game-mjs/grid.js";
import {
  createDockerShip,
  shipOnGrid,
  someShipOnDocker,
} from "./game-mjs/ships.js";
import { addClickEvent, getHTML } from "./utils/utils.js";

/* WEB SOCKET */
let stompClient = null;
/* Player */
let player = {};
let rival = {};
let game = {};

const { gameId, gamePlayerId } = JSON.parse(localStorage.getItem("player"));
const BASE_URL_TOPIC = `/topic/match/${gameId}`;
const BASE_URL_APP = "/app";

addClickEvent("#sendShips", sendShips);

beginGame();

function beginGame() {
  createGrid(9, document.getElementById("grid"), "ships", "gridShips");
  createDockerShip();
  statusGame(gameId);
  //consultar estado del juego
}

function statusGame(gameId) {
  fetch(`/api/match/${gameId}/status`)
    .then((res) => (res.ok ? res.json() : Promise.reject(res.body)))
    .then(async (json) => {
      connectClienSocket(json.game_players);
    });
}

function sendShips() {
  if (someShipOnDocker()) {
    alert("faltan colocar ships");
  }

  stompClient.send(
    `${BASE_URL_APP}/${gameId}`,
    {},
    JSON.stringify(shipOnGrid())
  );
}

//createShips('battleship', 4, 'horizontal', document.getElementById('dock'),false)
/* START GAME - WEB SOCKET*/
function connectClienSocket(game_players) {
  let socket = new SockJS("/the-last-airbender");
  stompClient = Stomp.over(socket);

  stompClient.connect({}, (frame) => {
    console.log("Connected: " + frame);
    if (game_players.length == 2) {
      suscribeGame();
    }
    subscribeShips();
  });
}

function subscribeShips() {
  /* Send Ships */
  stompClient.subscribe(`${BASE_URL_TOPIC}/ships`, ({ body }) => {
    console.log(JSON.parse(body));
  });
}

function startGame() {
  createSalvoGrid();
  createDockerSalvos();
  // decidir si lamar o no a la funcion
  createStartedGame();
}

function createSalvoGrid() {}

function createDockerSalvos() {}

function createStartedGame() {
  // crear barcos
  // crear mis salvos
  // crear salvos del oponente
}

function suscribeGame() {
  /* Send emotes */
  stompClient.subscribe(
    `${BASE_URL_TOPIC}/emote/${rival.gamePlayerId}`,
    ({ body }) => {
      console.log(JSON.parse(body));
    }
  );

  /* Send Salvos */
  stompClient.subscribe(
    `${BASE_URL_TOPIC}/salvos/${rival.gamePlayerId}`,
    ({ body }) => {
      console.log(JSON.parse(body));
    }
  );

  /* Send Rematch */
  stompClient.subscribe(`${BASE_URL_TOPIC}/rematch`, ({ body }) => {
    console.log(JSON.parse(body));
  });
}

let params = new URLSearchParams(location.search);
let gp = params.get("gp");

let playerDataGame1 = {};
let playerDataGame2 = {};
let myturn = null;
let turnOpponent = null;
let salvoes = {};
let canShoot = true;
//setinterval
var intervalGamestard = null;
var updateSalvoes = null;
var updateRematch = null;
var contador = 0;
//botones dom
let backMenu = document.querySelector("#backmenu");
let backMenu2 = document.querySelector("#backmenu-endGame");
let send_Ships = document.querySelector("#sendShips");
let send_Salvo = document.querySelector("#sendSalvo");
let grid_salvoes = document.querySelector("#grid_salvoes");
let displayText = document.querySelector("#display");
let rematch = document.querySelector("#rematch");
//eventlistener
// rematch.addEventListener("click", playAgain);
// backMenu.addEventListener("click", backmenu);
// backMenu2.addEventListener("click", noRematch);
// send_Ships.addEventListener("click", sendShips);
// send_Salvo.addEventListener("click", sendSalvo);
// if (screen.width < 1024) {
//   document.querySelector("#grid").addEventListener("touchstart", startMov);
//   document.querySelector("#grid").addEventListener("touchend", endMov);
// }

function sendM(n) {
  stompClient.send(`/app/${n}`, {}, JSON.stringify({}));
}

function getPlayerData() {
  const player = localStorage.getItem("player");
  return JSON.parse(player);
}

function cargarDatosScreen(json) {
  // variables
  let player1 = document.querySelector("#player1");
  let player2 = document.querySelector("#player2");
  let dockImg = document.querySelector("#dock > img");
  let grid_salvoes = document.querySelector("#grid_salvoes");
  let grid = document.querySelector("#grid");

  Array.from(player1.children).forEach((e) => {
    if (e.tagName == "P") {
      e.innerText = playerDataGame1.email;
    } else if (e.tagName == "IMG") {
      e.src = "assets/icons/border-" + playerDataGame1.nacion + ".png";
    }
  });
  if (playerDataGame2 != null) {
    Array.from(player2.children).forEach((e) => {
      if (e.tagName == "P") {
        e.innerText = playerDataGame2.email;
      } else if (e.tagName == "IMG") {
        e.src = "assets/icons/border-" + playerDataGame2.nacion + ".png";
      }
    });
  }
  dockImg.src = "assets/icons/borde-" + json.ubicacion + ".png";
  grid_salvoes.classList.add("bgGrid-" + json.ubicacion);
  grid.classList.add("bgGrid-" + json.ubicacion);
} /*

/*
/*
viewPlayer(gp, true, false);

function playAgain() {
  reMatchGame(true);
}

function reMatchGame(valor) {
  fetch("/api/rematch/" + gp + "/" + valor, {
    method: "POST",
  }).then(function (response) {
    if (response.ok) {
      if (valor) {
        updateRematch = window.setInterval(
          viewPlayer,
          3000,
          gp,
          false,
          false,
          true
        );
      } else {
        location.assign("/web/games.html");
      }
    } else {
      throw new Error(response.text());
    }
  });
}

function endGame(score) {
  document.querySelector(".flex-container").style.display = "none";
  document.querySelector("#modal").classList.remove("d-none");
  document.querySelector("#modal").classList.add("modalAnimation");
  document
    .querySelector("#modal")
    .addEventListener("animationend", function () {
      document.querySelector("#modal").classList.remove("modalAnimation");
    });
  let texto = document.querySelector(".text-endgame");
  let img = document.querySelector(".img-endGame");
  if (score == 1) {
    texto.innerText = "Score +1";
  } else if (score == 3) {
    img.src = "assets/img/victory.png";
    texto.innerText = "Score +3";
  } else if (score == 0) {
    img.src = "assets/img/defeat.png";
    texto.innerText = "Good Game";
  }
}

//

function noRematch() {
  reMatchGame(false);
}

function backmenu() {
  location.assign("/web/games.html");
}

function viewPlayer(gpid, isInit, isUpdateSalvo, isRematch) {
  fetch("/api/gp/" + gpid, {
    method: "GET",
  })
    .then(function (response) {
      if (response.ok) {
        return response.json();
      } else {
        throw new Error(response.text());
      }
    })
    .then(function (JSON) {
      console.log(JSON.Game_Started);
      playerDataGame1 = JSON.gamePlayers.filter((e) => e.id == gp)[0].player;
      playerDataGame2 = JSON.gamePlayers.filter((e) => e.id != gp)[0];
      myturn = JSON.my_turn;
      turnOpponent = JSON.Opponent_turn;
      if (playerDataGame2 != null) {
        playerDataGame2 = playerDataGame2.player;
      }
      salvoes = JSON.salvoes;
      cargarDatosScreen(JSON);

      // configuracion de revancha
      if (contador != 10 && isRematch) {
        if (JSON.Opponent_rematch != null && JSON.my_rematch != null) {
          if (JSON.Opponent_rematch && JSON.my_rematch) {
            if (playerDataGame1.id < playerDataGame2.id) {
              fetch(
                "/api/games/" +
                  JSON.ubicacion +
                  "/" +
                  JSON.direccion +
                  "/" +
                  true,
                {
                  method: "POST",
                }
              )
                .then(function (response) {
                  if (response.ok) {
                    return response.json();
                  }
                })
                .then(function (JSON_nice) {
                  setNewGame(JSON_nice.gameid, JSON_nice.gpid);
                });
            } else if (JSON.new_game != null) {
              unirse_a_game(JSON.new_game);
            }
          } else {
            alert("the opponent did not accept, you will return to the menu");
            setTimeout(function () {
              location.assign("/web/games.html");
            }, 4000);
          }
        } else {
          contador++;
          return;
        }
      } else if (contador == 10) {
        alert(
          "there is no response from the opponent, you will return to the menu"
        );
        setTimeout(function () {
          location.assign("/web/games.html");
        }, 4000);
      }

      if (isInit) {
        if (JSON.ships.length == 0) {
          defaultships();
        } else if (JSON.ships.length != 0 && !JSON.Game_Started) {
          createShipsWeb(JSON);
          displayText.firstElementChild.innerText = "Wait Opponent...";
        } else if (JSON.Game_Started) {
          createShipsWeb(JSON);
          isGameStart(JSON);
        }
      } else if (isUpdateSalvo) {
        if (JSON.Game_Over && JSON.gamePlayers.some((e) => e.Score != null)) {
          window.clearInterval(updateSalvoes);
          console.log("fin del juego");
          endGame(JSON.gamePlayers.filter((e) => e.id == gp)[0].Score);
        } else {
          createSalvoes(JSON);
        }
      } else if (JSON.Game_Started) {
        isGameStart(JSON);
      }
    });
}

function setNewGame(gameid, gpid) {
  fetch("/api/newGameId/" + gameid + "/" + gp, {
    method: "POST",
  })
    .then(function (response) {
      if (response.ok) {
        return response.json();
      }
    })
    .then(function (json) {
      window.location.href = "/web/game.html?gp=" + gpid;
    });
}

function unirse_a_game(gameid) {
  fetch("/api/game/" + gameid + "/players", {
    method: "POST",
  })
    .then(function (response) {
      if (response.ok) {
        return response.json();
      }
    })
    .then(function (json) {
      location.assign("/web/game.html?gp=" + json.gpid);
      console.log(json.gpid);
    });
}

function isGameStart(JSON) {
  document.querySelector("#dock .ships").appendChild(send_Salvo);
  // creola grilla de salvos
  if (screen.width < 1024) {
    createGrid(11, document.getElementById("grid"), "salvoes", "gridSalvos");
    document
      .querySelectorAll("#gridSalvos div[data-y]")
      .forEach((e) => e.addEventListener("click", addsalvo));
    document.querySelector("#gridShips").style.position = "fixed";
    document.querySelector("#gridSalvos").style.position = "fixed";
  } else {
    createGrid(
      11,
      document.getElementById("grid_salvoes"),
      "salvoes",
      "gridSalvos"
    );
    document
      .querySelectorAll("#grid_salvoes div[data-y]")
      .forEach((e) => e.addEventListener("click", addsalvo));
    grid_salvoes.classList.remove("d-none");
  }
  createSalvoes(JSON);
  document.querySelector("#display").firstElementChild.innerText =
    "game started";
  document.querySelector("#seeE").classList.remove("d-none");
  setTimeout(function () {
    activarAnimation("toBot");
  }, 2000);
  intervalGamestard != null ? window.clearInterval(intervalGamestard) : null;

  updateSalvoes = window.setInterval(viewPlayer, 3000, gp, false, true);
}

/*FUNCIONES EMOTES*/ /*
function sendEmote(texto) {
  let texotFetch;
  if (texto.tagName) {
    texotFetch = texto.innerText;
  } else {
    texotFetch = texto;
  }
  fetch("/api/emote/" + gp + "/" + texotFetch, {
    method: "POST",
  }).then(function (response) {
    if (response.ok) {
      if (texotFetch[0] == "e") {
        document
          .querySelector("#player1 .box-emotes p")
          .classList.add("d-none");
        document
          .querySelector("#player1 .box-emotes img")
          .classList.remove("d-none");
        document.querySelector("#player1 .box-emotes img").src =
          "assets/emotes/" + texotFetch + ".png";
      } else {
        document
          .querySelector("#player1 .box-emotes p")
          .classList.remove("d-none");
        document
          .querySelector("#player1 .box-emotes img")
          .classList.add("d-none");
        document.querySelector("#player1 .box-emotes p").innerText = texotFetch;
      }
    }
  });
}

function verEmotes() {
  document.querySelector("#seeE").classList.add("d-none");
  document.querySelector("#hideE").classList.remove("d-none");
  let diplayEmotes = document.querySelector(".box-emotes-dock");
  diplayEmotes.classList.remove("d-none");
}

function ocultarEmotes() {
  document.querySelector("#seeE").classList.remove("d-none");
  document.querySelector("#hideE").classList.add("d-none");
  let diplayEmotes = document.querySelector(".box-emotes-dock");
  diplayEmotes.classList.add("d-none");
} */ /* 5, 4, 3, 1, 1

/*FUNCIONES DE ANIMACION DE LA GRILLA*/ /*
/*
let posInit = null;
let posFin = null;

function startMov(event) {
  posInit = event.changedTouches[0].screenY;
}

function endMov(event) {
  posFin = event.changedTouches[0].screenY;
  if (posFin - posInit > 50) {
    activarAnimation("toBot");
  } else if (posInit - posFin > 50) {
    activarAnimation("toTop");
  }
}

function activarAnimation(moveTo) {
  let gridSalvo = document.querySelector("#gridSalvos");
  let gridShips = document.querySelector("#gridShips");
  if (
    gridShips.classList.contains("cero") &&
    !gridSalvo.classList.contains("cero")
  ) {
    gridSalvo.classList.remove(
      "Animation-toCeroFromDown",
      "Animation-toUp",
      "Animation-toCeroFromUp",
      "Animation-toDown",
      "cero"
    );
    gridShips.classList.remove(
      "Animation-toCeroFromDown",
      "Animation-toUp",
      "Animation-toCeroFromUp",
      "Animation-toDown",
      "cero"
    );
    if (moveTo == "toTop") {
      gridSalvo.classList.add("Animation-toCeroFromDown");
      gridSalvo.classList.add("cero");
      gridShips.classList.add("Animation-toUp");
    } else if (moveTo == "toBot") {
      gridSalvo.classList.add("Animation-toCeroFromUp");
      gridSalvo.classList.add("cero");
      gridShips.classList.add("Animation-toDown");
    }
  } else if (
    !gridShips.classList.contains("cero") &&
    gridSalvo.classList.contains("cero")
  ) {
    gridSalvo.classList.remove(
      "Animation-toCeroFromDown",
      "Animation-toUp",
      "Animation-toCeroFromUp",
      "Animation-toDown",
      "cero"
    );
    gridShips.classList.remove(
      "Animation-toCeroFromDown",
      "Animation-toUp",
      "Animation-toCeroFromUp",
      "Animation-toDown",
      "cero"
    );
    if (moveTo == "toTop") {
      gridSalvo.classList.add("Animation-toUp");
      gridShips.classList.add("Animation-toCeroFromDown");
      gridShips.classList.add("cero");
    } else if (moveTo == "toBot") {
      gridSalvo.classList.add("Animation-toDown");
      gridShips.classList.add("Animation-toCeroFromUp");
      gridShips.classList.add("cero");
    }
  }
}
*/ /*
/*FUNCIONES SHIPS*/ /*
function defaultships() {
  [5, 4, 3, 3, 2].forEach((lengthShip, index) =>
    createShips(
      shipsName[index],
      lengthShip,
      "horizontal",
      document.querySelector("#dock .ships"),
      false
    )
  );
}



function sendShips() {
  let shipsObjects = [];
  if (
    shipsName.every(
      (e) => document.querySelector("#" + e).dataset.y != undefined
    )
  ) {
    shipsName.forEach((e) => {
      let barco = document.querySelector("#" + e).dataset;
      let position = [];
      for (var i = 0; i < parseInt(barco.length); i++) {
        if (barco.orientation == "horizontal") {
          position.push(barco.y + (parseInt(barco.x) + i));
        } else {
          position.push(
            String.fromCharCode(barco.y.charCodeAt(0) + i) + barco.x
          );
        }
      }
      shipsObjects.push({ typeShip: e.toUpperCase(), shipLocations: position });
    });
    fetch("/api/games/players/" + gp + "/ships", {
      method: "POST",
      body: JSON.stringify(shipsObjects),
      headers: {
        "Content-Type": "application/json",
      },
    }).then(function (response) {
      if (response.ok) {
        viewPlayer(gp, true, false);
        send_Ships.classList.add("d-none");
        displayText.firstElementChild.innerText = "Wait Opponent...";
        if (screen.width < 1024) {
          document.querySelector("#gridShips").style.position = "fixed";
        }
        intervalGamestard = window.setInterval(
          viewPlayer,
          3000,
          gp,
          false,
          false
        );
      } else {
        throw new Error(response.text());
      }
    });
  } else {
    alert("faltan colocar ships");
  }
}

function createShipsWeb(json) {
  //elimino los barcos para no tener que cargar la pagina otra vez
  if (
    shipsName.every((e) => document.querySelector("#" + e)) &&
    shipsName.every(
      (e) => document.querySelector("#" + e).dataset.y != undefined
    )
  ) {
    shipsName.forEach((e) => {
      document
        .querySelector("#" + e)
        .parentNode.removeChild(document.querySelector("#" + e));
    });
  }

  //creo los bracos en la grilla
  for (let i = 0; i < json.ships.length; i++) {
    let location = json.ships[i].locations[0];
    let orientation =
      json.ships[i].locations[0].substring(1) ==
      json.ships[i].locations[1].substring(1)
        ? "vertical"
        : "horizontal";
    let type = json.ships[i].type_Ship;
    let tamaño = json.ships[i].locations.length;
    createShips(
      type.toLowerCase(),
      tamaño,
      orientation,
      document.getElementById("ships" + location),
      true
    );
  }
} */ /*

/*FUNCIONES SALVO*/

/*
function createSalvoDock(numero) {
  for (var i = 0; i < numero; i++) {
    let div = document.createElement("div");
    div.classList.add(playerDataGame1.nacion);
    div.classList.add("salvo");
    document.querySelector(".municion").appendChild(div);
  }
}

function addsalvo(event) {
  let celda = event.target;
  let salvoInDock = document.querySelector(".municion");
  if (celda.dataset.salvoes == undefined) {
    if (
      salvoInDock.childElementCount != 0 &&
      celda.dataset.salvo == undefined
    ) {
      celda.classList.add(playerDataGame1.nacion);
      celda.dataset.salvo = true;
      salvoInDock.firstElementChild.remove();
    } else if (celda.dataset.salvo) {
      celda.classList.remove(playerDataGame1.nacion);
      let shoot = document.createElement("div");
      shoot.classList.add("salvo");
      shoot.classList.add(playerDataGame1.nacion);
      document.querySelector(".municion").appendChild(shoot);
      celda.removeAttribute("data-salvo");
      displayText.firstElementChild.innerText = "you can shoot";
    } else {
      displayText.firstElementChild.innerText = "you have no shots left";
    }
  }
  if (salvoInDock.childElementCount == 0) {
    send_Salvo.classList.remove("d-none");
  } else {
    send_Salvo.classList.add("d-none");
  }
}

function sendSalvo() {
  let salvoInDock = document.querySelector(".municion");
  if (salvoInDock.childElementCount == 0) {
    let salvo = [];
    if (screen.width < 1024) {
      document
        .querySelectorAll("#grid div[data-salvo]")
        .forEach((p) => salvo.push(p.dataset.y + p.dataset.x));
    } else {
      document
        .querySelectorAll("#grid_salvoes div[data-salvo]")
        .forEach((p) => salvo.push(p.dataset.y + p.dataset.x));
    }
    console.log(JSON.stringify(salvo));
    fetch("/api/games/players/" + gp + "/salvos", {
      method: "POST",
      body: JSON.stringify(salvo),
      headers: {
        "Content-Type": "application/json",
      },
    }).then(function (response) {
      if (response.ok) {
        if (screen.width < 1024) {
          document.querySelectorAll("#grid div[data-salvo]").forEach((e) => {
            e.classList.remove(playerDataGame1.nacion);
            e.removeAttribute("data-salvo");
          });
        } else {
          document
            .querySelectorAll("#grid_salvoes div[data-salvo]")
            .forEach((e) => {
              e.classList.remove(playerDataGame1.nacion);
              e.removeAttribute("data-salvo");
            });
        }
        viewPlayer(gp, false, true);
        canShoot = true;
      } else {
        return Promise.reject(response.json());
      }
    });
  }
}

function createSalvoes(json) {
  // creo y actualizo los emotes
  if (json.my_emote != null) {
    if (json.my_emote[0] == "e") {
      document.querySelector("#player1 .box-emotes p").classList.add("d-none");
      document
        .querySelector("#player1 .box-emotes img")
        .classList.remove("d-none");
      document.querySelector("#player1 .box-emotes img").src =
        "assets/emotes/" + json.my_emote + ".png";
    } else {
      document
        .querySelector("#player1 .box-emotes p")
        .classList.remove("d-none");
      document
        .querySelector("#player1 .box-emotes img")
        .classList.add("d-none");
      document.querySelector("#player1 .box-emotes p").innerText =
        json.my_emote;
    }
  }
  if (json.Opponent_emote != null) {
    if (json.Opponent_emote[0] == "e") {
      document.querySelector("#player2 .box-emotes p").classList.add("d-none");
      document
        .querySelector("#player2 .box-emotes img")
        .classList.remove("d-none");
      document.querySelector("#player2 .box-emotes img").src =
        "assets/emotes/" + json.Opponent_emote + ".png";
    } else {
      document
        .querySelector("#player2 .box-emotes p")
        .classList.remove("d-none");
      document
        .querySelector("#player2 .box-emotes img")
        .classList.add("d-none");
      document.querySelector("#player2 .box-emotes p").innerText =
        json.Opponent_emote;
    }
  }

  let idPlayer = json.gamePlayers.filter((e) => e.id == gp)[0].player.id;
  let ultimoTiro = null;
  let ultimoTurno = null;
  for (let i = 0; i < json.salvoes.length; i++) {
    if (json.salvoes[i].player == idPlayer) {
      //pinto los disparos propios
      json.salvoes[i].locations.forEach((e) => {
        if (document.querySelector("#salvoes" + e).style.background == "") {
          if (
            json.salvoes[i].player == idPlayer &&
            json.salvoes[i].nice_shoot != null &&
            json.salvoes[i].nice_shoot.includes(e)
          ) {
            document.querySelector("#salvoes" + e).classList.add("niceShoot");
          } else {
            document.querySelector("#salvoes" + e).classList.add("failShoot");
          }
          document.querySelector("#salvoes" + e).dataset.salvoes = true;
        }
      });
    } else {
      if (ultimoTurno < json.salvoes[i].turn) {
        ultimoTiro = json.salvoes[i];
        ultimoTurno = json.salvoes[i].turn;
      }
      //pinto los disparos del oponente
      json.salvoes[i].locations.forEach((e) => {
        if (
          json.ships.flatMap((s) => s.locations.map((p) => p)).includes(e) &&
          document.querySelector("#ships" + e).style.background == ""
        ) {
          document.querySelector("#ships" + e).classList.add("niceShoot");
        }
      });
    }
  }
  if (myturn <= turnOpponent && canShoot) {
    if (ultimoTiro == null || ultimoTiro.ships_dead == null) {
      createSalvoDock(5);
      canShoot = false;
      send_Salvo.classList.add("d-none");
    } else {
      send_Salvo.classList.add("d-none");
      canShoot = false;
      createSalvoDock(5 - ultimoTiro.ships_dead.length);
    }
  }
}
*/
