//cargar datos
let gamesData = [];
let players = [
  {
    email:"AIR@correo.com",
    scores:[1, 3, 3]
  },
  {
    email:"FIRE@correo.com",
    scores:[1, 0, 3]
  },
  {
    email:"WATER@correo.com",
    scores:[3, 3, 3]
  },
  {
    email:"EARTH@correo.com",
    scores:[1, 1, 1]
  },
  {
    email:"AIR@correo.com",
    scores:[1, 2, 3]
  },
];
let masx,
  masy = false;
let playerData = {
  nation: "FIRE",
  email: "marco@digitalhouse.com",
  name: "racnar1"
}
let recargarMapa = false;
//referencias al DOM
let tablegame = document.querySelector("#game-body");
let tableranking = document.querySelector("#ranked-body");
let modal = document.querySelector("#registre");
let container = document.querySelector(".container");
let modalRegistre = document.querySelector("#modal-registre");
let logout = document.querySelector("#logout");
let verMapa = document.querySelector("#verMapa");
let info = document.querySelector("#info");
//eventlistener
logout.addEventListener("click", logoutFunction);
verMapa.addEventListener("click", viewMapa);
/*crear area clickable para la imagen*/
ubicacionesMap.forEach((area) => {
  const areahtml = document.createElement("area");
  const { id, location, x, y, r } = area;
  areahtml.dataset.location = location;
  areahtml.id = id;
  areahtml.shape = "circle";
  areahtml.alt = "aremap";
  areahtml.coords = `${resizeCoord(x, "width")}, ${resizeCoord(y, "height")}, ${r}`;
  areahtml.addEventListener("click", selectGame);
  document.querySelector("map[name*=mapeo]").appendChild(areahtml);
});


function isMobile(){
  return visualViewport.width < 1024 
  // ToDo: try more test about determinated if a device be or not mobile
  // navigator.userAgentData.mobile
}

function resizeCoord(coord, side){  
  if(isMobile()) return coord
  const currentSize = visualViewport[side] * 0.75
  const referentSize = side === "width" ? 1400 : 800
  const conversionFactor =  currentSize / referentSize;
  return coord * conversionFactor;
}

function resizeCoordWithMargin(coords){
  const [ x, y ] = coords.split(",")
  if(isMobile()) return [ x, y ]

  const marginSizeX = visualViewport.width * 0.125
  const marginSizeY = visualViewport.height * 0.125
  const coordX = parseFloat(x) + marginSizeX
  const coordY = parseFloat(y) + marginSizeY
  return [ coordX, coordY ]
}

function randomNation(){
  const nations = ["FIRE", "EARTH", "AIR", "WATER"]
  const n =Math.round(Math.random() * 3)
  playerData.nation = nations[n]
}

// COSAS
let playerScore = [];

reloadInfo();

function reloadInfo() {
  fetch("/api/games", {
    method: "GET",
  })
    .then((res) => (res.ok ? res.json() : Promise.reject()))
    .then(function (json) {
      gamesData = json.games;
      players = json.player_score;
      playerData = json.player;
      playerScore = json.player_score;
      if (json.player.nation != null) {
        inMenu(json);
      } else {
        chooseNation();
      }
      let games = json.games.filter((e) => e.direction != "00");
      if (games.length != 0) {
        console.log(games);
        crearJuegosMap(games);
      }
    });
}

function setNacionPlayer(nacion) {
  fetch("/api/setNacionPlayer/" + nacion, {
    method: "POST",
  }).then(function (response) {
    if (response.ok) {
      fetch("/api/games", {
        method: "GET",
      })
        .then(function (response) {
          if (response.ok) {
            return response.json();
          }
        })
        .then(function (json) {
          reloadInfo();
        });
    }
  });
}

function chooseNation() {
  document.querySelector("#inicoNacion").classList.remove("hidden");
}

function inMenu(json) {
  document.querySelector("#inicoNacion").classList.add("hidden");
  document
    .querySelector("#player")
    .classList.add("iconTransparent" + json.player.nation);
  document.querySelector("#webGames").classList.remove("hidden");
  document.querySelector("#botonera").classList.remove("hidden");
}

/*funciones para modal y cambiar los datos que muestra*/
function verdatos(elementhtml) {
  console.log(elementhtml);
  if (elementhtml.id == "player") {
    verDatosUser();
  } else if (elementhtml.id == "info") {
    verTutorial();
  } else if (elementhtml.id == "ladder") {
    createTableRanking();
  }
  addmodal();
}

function nomodal() {
  document.querySelector("#modal").classList.add("modalAnimationout");
  document.querySelector("#modal").classList.add("hidden");
  document.querySelector("#modal").classList.remove("modalAnimation");
  document.querySelector("#container").style.opacity = 1;
}

function addmodal() {
  document.querySelector("#modal").classList.remove("hidden");
  document.querySelector("#modal").classList.remove("modalAnimationout");
  document.querySelector("#container").style.opacity = 0.2;
  document.querySelector("#modal").classList.remove("hidden");
  document.querySelector("#modal").classList.add("modalAnimation");
}

function cleanModal(){
  let modalDiv = document.querySelector(".div-modal");
  modalDiv.innerHTML = "";
  modalDiv.classList.remove("bg-" + playerData.nation);
  modalDiv.classList.remove("bg-solid-" + playerData.nation);
}

function verDatosUser() {
  

  let modalDiv = document.querySelector(".div-modal");
  cleanModal()
  randomNation()
  modalDiv.classList.add("bg-" + playerData.nation);
  modalDiv.classList.add("bg-solid-" + playerData.nation);
  
  const side = playerData.nation == "WATER" || playerData.nation == "EARTH" ? "left" : "right"
  const userDataHTML = `
    <div class="datosUser datosUser-${side} bg-color-${playerData.nation}">
      <p class="text-userName">${playerData.email}</p>
      <div class="divText">
        <p>${playerData.name}</p>
        <p>${playerData.nation}</p>
      </div>
      <table class="table-user">
        ${tableUser(playerData.id)}
      </table>
    </div>
  `

  modalDiv.innerHTML = userDataHTML;
}

function tableUser(user) {
  console.log(playerScore);
  let datos = playerScore.filter((e) => e.id == user)[0]?.scores || [];
  console.log(datos);
  let table = `
        <thead>
            <tr>
                <th>Score</th>
                <th>Wins</th>
                <th>Loses</th>
                <th>Tied</th>
                <th>win Rate</th>
            </tr>
        </thead>
        <tbody>
    `;
  let win = 0,
    tied = 0,
    lose = 0,
    total = 0;
  let winRate = "-";
  datos.forEach((e) => {
    total += e;
    switch (e) {
      case 3:
        win += 1;
        break;
      case 1:
        tied += 1;
        break;
      case 0:
        lose += 1;
        break;
    }
  });
  if (datos.length != 0) {
    winRate = parseInt((win * 100) / datos.length);
  }
  table += `
    <tr>
        <td>${total}</td>
        <td>${win}</td>
        <td>${lose}</td>
        <td>${tied}</td>
        <td>${winRate}%</td>
    </tr>
    </tbody>
    `;
  return table;
}

function verTutorial(argument) {
  let modalDiv = document.querySelector(".div-modal");
  modalDiv.innerHTML = "";
  modalDiv.classList.remove("bg" + playerData.nation);

  let text = document.createElement("P");
  text.innerText = "Tutorial Coming Soon";
  text.classList.add("tutorial-text")  
  
  modalDiv.appendChild(text);
}

/* funciones relacionadas con los juegos, unirse, crear, volver*/
function crearJuegosMap(games) {
  Array.from(document.querySelector("#pivotMap").children).forEach((e) =>
    e.remove()
  );
  games.forEach((e) => {
    if (e.game_players[0].Score == null) {
      const { id, coords, dataset } = document.querySelector("#" + e.direction);
      const [ width, height ] = resizeCoordWithMargin(coords)
      const div = document.createElement("div");
      div.id = id;
      div.classList.add("selectMap");
      div.style.left  = width + "px";
      div.style.top   = height + "px";
      div.dataset.game = "true";
      div.dataset.gameid = e.id;
      div.dataset.location = dataset.location;

      div.addEventListener("click", selectGame);
      if (e.game_players.length == 2) {

        div.dataset.gpid1 = e.game_players[0].id;
        div.dataset.playerid1 = e.game_players[0].player.id;
        div.dataset.playername1 = e.game_players[0].player.email;

        div.dataset.gpid2 = e.game_players[1].id;
        div.dataset.playerid2 = e.game_players[1].player.id;
        div.dataset.playername2 = e.game_players[1].player.email;

        if (e.game_players.some((f) => f.player.id == playerData.id)) {
          div.dataset.name = "Enter";
          div.classList.add("SelectEnter");
        } else {
          div.classList.add("SelectInGame");
          div.dataset.name = "InGame";
        }
      } else {

        div.dataset.gpid1 = e.game_players[0].id;
        div.dataset.playerid1 = e.game_players[0].player.id;
        div.dataset.playername1 = e.game_players[0].player.email;
        
        if (e.game_players.some((f) => f.player.id == playerData.id)) {
          div.dataset.name = "Enter";
          div.classList.add("SelectEnter");
        } else {
          div.classList.add("selectJoin");
          div.dataset.name = "Join";
        }
      }

      document.querySelector("#pivotMap").appendChild(div);
    }
  });
}

function selectGame(event) {
  if (document.querySelector("div[data-name*='selectGame']") != null) {
    document
      .querySelector("#pivotMap")
      .removeChild(document.querySelector("div[data-name*='selectGame']"));
  }

  const { id, dataset, coords } = event.target;
  const [ width, height ] = resizeCoordWithMargin(coords)
  let div = document.createElement("div");
  div.dataset.id = id;
  div.classList.add("selectMap");
  div.style.left  = width + "px";
  div.style.top   = height + "px";
  div.dataset.name = "selectGame";
  div.dataset.location = dataset.location;
  div.addEventListener("click", removeSelect);

  if (dataset.game == "true") {
    div.classList.add("selectGameCreate");
    document.querySelectorAll("div [name*='dataGame']").forEach((e) => {
      e.classList.remove("hidden");
      if (!(e.innerText == "Info") && !(dataset.playerid1 == playerData.id)) {
        e.innerText = dataset.name;
      } else if (
        !(e.innerText == "Info") &&
        dataset.playerid1 == playerData.id
      ) {
        e.innerText = "Enter";
      }
    });
  } else {
    div.classList.add("selectCreate");

    document.querySelectorAll("div [name*='dataGame']").forEach((e) => {
      e.classList.remove("hidden");
      if (!(e.innerText == "Info")) {
        e.innerText = "Create";
      }
    });
  }

  
  document.querySelector("#pivotMap").appendChild(div);
  if (
    document.querySelector(
      "div#" + document.querySelector("div[data-name*=selectGame]").dataset.id
    ) == undefined
  ) {
    document.querySelector("#infoGame").classList.add("hidden");
  }
  console.log(div);
}

function removeSelect(event) {
  document.querySelector("#pivotMap").removeChild(event.target);
  document
    .querySelectorAll("div [name*='dataGame']")
    .forEach((e) => e.classList.add("hidden"));
}

function infoGame() {
  addmodal();
  let modalDiv = document.querySelector(".div-modal");
  modalDiv.innerHTML = "";
  modalDiv.classList.remove("bg" + playerData.nation);
  let select = document.querySelector(
    "div#" + document.querySelector("div[data-name*=selectGame]").dataset.id
  );
  let div = document.createElement("div");
  div.classList.add("div-infoGame");
  let title = document.createElement("H1");
  title.style.margin = "0";
  let textoPlayer1 = document.createElement("P");
  let textoPlayer2 = document.createElement("P");
  let divStatus = document.createElement("div");
  divStatus.classList.add("divStatus");
  let textoId = document.createElement("P");
  let textoStatus = document.createElement("P");

  title.innerText = "Nacion de " + select.dataset.location;
  textoId.innerText = "Game Id: " + select.dataset.gameid;
  textoPlayer1.innerText = "Player 1: " + select.dataset.playername1;
  if (select.dataset.playername2 != undefined) {
    textoPlayer2.innerText = "Player 2: " + select.dataset.playername2;
    textoStatus.innerText = "Status: In Game";
  } else {
    textoPlayer2.innerText = "Player 2: -";
    textoStatus.innerText = "Status: Create";
  }

  divStatus.appendChild(textoId);
  divStatus.appendChild(textoStatus);
  div.appendChild(title);
  div.appendChild(divStatus);
  div.appendChild(textoPlayer1);
  div.appendChild(textoPlayer2);
  modalDiv.appendChild(div);
}

function enterGame(event) {
  let a = document.querySelector(
    "div#" + document.querySelector("div[data-name*='selectGame']").dataset.id
  );
  if (event.innerText == "Create") {
    let data = document.querySelector("div[data-name*=selectGame]");
    crearjuego(data.dataset.location, data.dataset.id);
  } else if (event.innerText == "Enter") {
    const isPlayer1 = a.dataset.playerid1 == playerData.id;
    playerData.gamePlayerId = isPlayer1 ? a.dataset.gpid1 : a.dataset.gpid2;
    playerData.gameId = a.dataset.gameId;
    saveUserData();
    goGame();
  } else if (event.innerText == "Join") {
    joinGame(a.dataset.gameid);
  } else if (event.innerText == "InGame") {
    alert("Coming soon spectator mode");
  }
}

function joinGame(gameid) {
  fetch(`/api/match/games/${gameid}`, { method: "POST" })
    .then((res) => (res.ok ? res.json() : Promise.reject(res.json())))
    .then((json) => {
      playerData.gamePlayerId = json.game_player_id;
      playerData.gameId = json.game_id;
      saveUserData();
      goGame();
    })
    .catch(function (error) {
      console.log("Hubo un problema con la petición Fetch:" + error.message);
    });
}

function crearjuego(location, direction) {
  fetch(`/api/match/games/${location}/${direction}`, { method: "POST" })
    .then((res) => (res.ok ? res.json() : Promise.reject(res.json())))
    .then((json) => {
      playerData.gamePlayerId = json.game_player_id;
      playerData.gameId = json.game_id;
      saveUserData();
      goGame();
    })
    .catch((error) => error)
    .then((json) => console.log(json));
}

function viewMenu() {
  document.querySelector("#mapabg").classList.remove("hidden");
  document.querySelector("#back").classList.add("hidden");
  document.querySelector("#reload").classList.add("hidden");
  document
    .querySelectorAll("button[name*='botonesMenu']")
    .forEach((e) => e.classList.remove("hidden"));
  document
    .querySelectorAll("div [name*='dataGame']")
    .forEach((e) => e.classList.add("hidden"));
}

function viewMapa(event) {
  let back = document.querySelector("#back");
  back.classList.remove("hidden");
  back.addEventListener("click", viewMenu);

  let reload = document.querySelector("#reload");
  reload.classList.remove("hidden");
  reload.addEventListener("click", function () {
    recargarMapa = true;
    reloadInfo();
    reload.classList.add("Animation-reload");
    setTimeout(function () {
      document.querySelector("#reload").classList.remove("Animation-reload");
    }, 1100);
  });

  document.querySelector("#mapabg").classList.add("hidden");
  document
    .querySelectorAll("button[name*='botonesMenu']")
    .forEach((e) => e.classList.add("hidden"));

}

//LOGOUT
function logoutFunction() {
  fetch("/api/logout", { method: "POST" }).then(() => location.assign("/"));
}

//TABLE RANKED
function createTableRanking() {
  players.forEach((e) => {
    e.total = 0;
    e.Won = 0;
    e.Lost = 0;
    e.Tied = 0;
  });
  for (let i = 0; i < players.length; i++) {
    for (let j = 0; j < players[i].scores.length; j++) {
      players[i].total += players[i].scores[j];
      switch (players[i].scores[j]) {
        case 3:
          players[i].Won += 1;
          break;
        case 1:
          players[i].Tied += 1;
          break;
        case 0:
          players[i].Lost += 1;
          break;
      }
    }
  }
  players.sort(function (a, b) {
    return b.total - a.total;
  });
  let modalDiv = document.querySelector(".div-modal");
  cleanModal()
  let body = ``;
  body += `
    <table class="table theadBlack">
        <thead class="">
            <tr>
                <th>Player</th>
                <th>Score</th>
                <th>Win</th>
                <th>Lose</th>
                <th>Tied</th>
            </tr>
        </thead>
        <tbody id="ranked-body">
    `;
  for (var i = 0; i < players.length; i++) {
    if (players[i].scores.length != 0) {
      body += `<tr>
            <td>
                <p>${players[i].email}</p>
            </td>
            <td>
                <p>${players[i].total}</p>
            </td>
            <td>
                <p>${players[i].Won}</p>
            </td>
            <td>
                <p>${players[i].Lost}</p>
            </td>
            <td>
                <p>${players[i].Tied}</p>
            </td>
        </tr>`;
    }
  }
  body += `</tbody></table>`;
  modalDiv.innerHTML += body;
}

function saveUserData() {
  let userData = JSON.stringify(playerData);
  localStorage.setItem("player", userData);
}

function goGame() {
  location.assign("/web/game.html");
}