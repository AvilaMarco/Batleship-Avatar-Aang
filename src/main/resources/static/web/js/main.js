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
  areahtml.coords = `${x}, ${y}, ${r}`;
  areahtml.addEventListener("click", selectGame);
  document.querySelector("map[name*=mapeo]").appendChild(areahtml);
});

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
      let areahtml = document.querySelector("#" + e.direction);
      let div = document.createElement("div");
      div.classList.add("selectMap");
      div.dataset.game = "true";
      if (e.game_players.length == 2) {
        div.dataset.gpid1 = e.game_players[0].id;
        div.dataset.playerid1 = e.game_players[0].player.id;
        div.dataset.gpid2 = e.game_players[1].id;
        div.dataset.playerid2 = e.game_players[1].player.id;
        div.dataset.playername1 = e.game_players[0].player.email;
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
        div.dataset.playername1 = e.game_players[0].player.email;
        div.dataset.playerid1 = e.game_players[0].player.id;
        if (e.game_players.some((f) => f.player.id == playerData.id)) {
          div.dataset.name = "Enter";
          div.classList.add("SelectEnter");
        } else {
          div.classList.add("selectJoin");
          div.dataset.name = "Join";
        }
      }
      div.addEventListener("click", selectGame);
      div.dataset.gameid = e.id;
      if (recargarMapa && screen.width < 1024) {
        div.style.top = parseInt(areahtml.coords.split(",")[1]) + 113 + "px";
        div.style.left = parseInt(areahtml.coords.split(",")[0]) + 109 + "px";
      } else if (screen.width < 1024) {
        div.style.top = parseInt(areahtml.coords.split(",")[1]) - 37 + "px";
        div.style.left = parseInt(areahtml.coords.split(",")[0]) - 41 + "px";
      } else {
        div.style.top = parseInt(areahtml.coords.split(",")[1]) + 47 + "px";
        div.style.left = parseInt(areahtml.coords.split(",")[0]) + 166 + "px";
      }
      div.dataset.location = areahtml.dataset.location;
      div.id = areahtml.id;
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
  const { id, dataset, coords, style } = event.target;
  let div = document.createElement("div");
  div.dataset.name = "selectGame";
  div.classList.add("selectMap");
  div.addEventListener("click", removeSelect);
  if (dataset.game == "true") {
    div.classList.add("selectGameCreate");
    div.style.top = style.top;
    div.style.left = style.left;
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
    const width = screen.width * (screen.width > 1024 ? 0.107 : 0.09);
    const height = screen.height * (screen.width > 1024 ? 0.09 : 0.05);
    div.style.top = parseInt(coords.split(",")[1]) + height + "px";
    div.style.left = parseInt(coords.split(",")[0]) + width + "px";

    document.querySelectorAll("div [name*='dataGame']").forEach((e) => {
      e.classList.remove("hidden");
      if (!(e.innerText == "Info")) {
        e.innerText = "Create";
      }
    });
  }

  div.dataset.location = dataset.location;
  div.dataset.id = id;
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
  if (
    document.querySelector("div[data-game*='true']") != null &&
    !document.querySelector("div[data-game*='true']").dataset.move
  ) {
    document.querySelectorAll("div[data-game*='true']").forEach((e) => {
      if (screen.width < 1024) {
        e.style.top = parseInt(e.style.top.split("px")[0]) + 150 + "px";
        e.style.left = parseInt(e.style.left.split("px")[0]) + 150 + "px";
      } else {
        e.style.top = parseInt(e.style.top.split("px")[0]) + "px";
        e.style.left = parseInt(e.style.left.split("px")[0]) + "px";
      }
      e.dataset.move = true;
    });
  }
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



// function selectGameCreate(event) {
//     if (document.querySelector("div[data-name*='selectGame']")!=null) {
//         document.querySelector("#pivotMap").removeChild(document.querySelector("div[data-name*='selectGame']"))
//     }
//     let click = event.target
//     document.querySelectorAll("div[data-name*='game']").forEach(e=>e.classList.remove("selectGame"))
//     if (click.name == "texto" || click.name == "texto2"){
//         click.parentNode.classList.add("selectGame")
//     }else{
//         click.classList.add("selectGame")
//     }
//     console.log(event.target)
// }
// let datosDelJuego = document.querySelector("div#"+document.querySelector("div[data-name*='selectGame']").dataset.id)
// console.log(datosDelJuego)
// console.log(event.innerText)

// let datos = document.querySelector("div[data-name*='selectGame']")
// let datosjoingame = document.querySelector("div.selectGame")
// let isPlayerCreator = false
// let game;
// if (null != datosjoingame) {
//     game = gamesData.filter(e=>e.id==datosjoingame.dataset.gameid)[0]
//    isPlayerCreator = gamesData.filter(e=>e.id==datosjoingame.dataset.gameid)[0].gameplayers.some(e=>e.player.id==playerData.id)
// }else{
//     isPlayerCreator = false
// }
// if (datos!=null && datosjoingame==null && !isPlayerCreator) {
//     // crear juegos
//     crearjuego(datos.dataset.location,datos.id)
// }else if(datos==null && datosjoingame!=null && !isPlayerCreator){
//     // unirse a juego
//     joinGame(parseInt(datosjoingame.dataset.gameid))
// }else if(isPlayerCreator && datos==null && datosjoingame!=null){
//     // volver al juego
//     console.log(parseInt(datosjoingame.dataset.gameid))
//     let gp = game.gameplayers.filter(e=>e.player.id == playerData.id?e.id:null)
//     if (gp!=null){
//         console.log(gp)
//         location.href = "/web/game.html?gp="+gp[0].id
//         // location.assign("/web/game.html?gp="+gp[0].id);
//     }else{
//         // modo espectador proximamente
//         alert("no perteneces al juego")
//     }
// }
// else{
//     console.log("elegir una ubicacion en el mapa")
// }
// let divselect = document.querySelector("div[data-name*='selectGame']")
// let y,x = null
// if (divselect!=null){
//     y = parseInt(divselect.style.top.split("px")[0])
//     x = parseInt(divselect.style.left.split("px")[0])
// }
// let marginMap = document.querySelector("#mapa").style
// if (event.target.innerText == "Map") {
//     event.target.innerText = "Menu"
//     document.querySelectorAll("button[name*='botonesMenu']").forEach(e=>e.classList.add("hidden"))
//     document.querySelector("#mapabg").classList.add("hidden")
//     if (divselect!=null) {
//         document.querySelector("#mapa").style.marginLeft = "0"
//         document.querySelector("#mapa").style.marginTop = "0"
//         if (masx) {
//             divselect.style.left = x - 150 + "px"
//         }else if(masy){
//             divselect.style.top = y - 150 + "px"
//         }
//     }
// }else{
//    event.target.innerText = "Map"
//     document.querySelector("#mapabg").classList.remove("hidden")
//     document.querySelectorAll("button[name*='botonesMenu']").forEach(e=>e.classList.remove("hidden"))
//     if (divselect!=null) {
//         if (y < 100) {
//             divselect.style.top = y + 150 + "px"
//             marginMap.marginTop = "150px"
//             masy = true
//         }else if (x < 100) {
//             divselect.style.left = x + 150 + "px"
//             marginMap.marginLeft = "150px"
//             masx = true
//         }
//     }
// }
//runweb
// function runweb(json){
// document.querySelector("#Player").classList.add("iconTransparent"+json.player.nacion)
// crear si existen juegos en el mapa
// mapaGames()
// createTableRanking()
// createTableGames();
// }
// function enterGame(event){
//     fetch('/api/games',{
//     method: 'GET',
//     }).then(function(response){if(response.ok){return response.json()}
//     }).then(function(JSON){
//         if (JSON.player != "guest") {
//             let game = JSON.games.filter(e=>e.id == event.target.dataset.gameid);
//             if (event.target.dataset.players.includes((""+JSON.player.id))) {
//                 let gp = game[0].gameplayers.filter(e=>e.player.id == JSON.player.id)
//                 location.assign("/web/game.html?gp="+gp[0].id);
//                 console.log(gp[0].id)
//             }else if (game[0].gameplayers.length == 1) {
//                 joinGame(game[0].id)
//             }else{
//                 alert("you can't join the game")
//             }
//         }else{
//             alert("Login to join or create a game")
//         }
//     });
// }
//TABLE GAMES
// function createTableGames(){
//     let player;
//     fetch('/api/games',{
//     method: 'GET',
//     }).then(function(response){if(response.ok){return response.json()}
//     }).then(function(json){
//     games = json.games;
//     player = json.player
//     tablegame.innerHTML = "";
//     let tabla = "";
//     for(let i = 0;i<games.length;i++){
//         tabla +=`<tr>
//         <td>
//             <p>${games[i].id}</p>
//         </td>
//         <td>
//             <p>${games[i].created}</p>
//         </td>`
//         if (games[i].gameplayers[0].Score != null) {
//             tabla +=`<td><p>Finish</p></td>`
//         }else{
//             tabla +=`<td><p>in Game</p></td>`
//         }
//         if (player != "guest") {
//             if (games[i].gameplayers.map(e=>e.player.id).includes(player.id) && games[i].gameplayers[0].Score == null) {
//                tabla +=`
//                 <td>
//                     <button id="entergame" data-players="${games[i].gameplayers.map(e=>e.player.id)}" data-gameid="${games[i].id}">enter game</button>
//                 </td>
//                 <td>
//                     <p>${(games[i].gameplayers.length==2?
//                         (games[i].gameplayers[1].player.id == player.id?games[i].gameplayers[0].player.id:games[i].gameplayers[1].player.id) : "non-rival")}</p>
//                 </td>`
//             }else if(games[i].gameplayers.length==1){
//                 tabla +=`
//                 <td>
//                    <p>${games[i].gameplayers[0].player.id}</p>
//                 </td>
//                 <td>
//                     <button id="joingame" data-players="${games[i].gameplayers.map(e=>e.player.id)}" data-gameid="${games[i].id}">join game</button>
//                 </td>`
//             }else{
//                 tabla +=`
//                 <td>
//                    <p>${games[i].gameplayers[0].player.id}</p>
//                 </td>
//                 <td>
//                     <p>${games[i].gameplayers[1].player.id}</p>
//                 </td>`
//             }
//         }else{
//             tabla +=`
//                 <td>
//                    <p>${games[i].gameplayers[0].player.id}</p>
//                 </td>
//                 <td>
//                     <p>${(games[i].gameplayers.length==2?games[i].gameplayers[1].player.id : "non-rival")}</p>
//                 </td>`
//         }

//     }
// tablegame.innerHTML = tabla
//     document.querySelectorAll('#entergame').forEach(e=>e.addEventListener('click',enterGame))
//     document.querySelectorAll('#joingame').forEach(e=>e.addEventListener('click',joinGame))
//     });
// }

// function createGame(){
//     fetch('/api/games',{
//         method:'POST'
//     })
//     .then(function(response){
//         if(response.ok){
//             createTableGames()
//             return response.json()
//         }else{
//             throw new Error(response.json());
//         }
//     })
//     .then(function(JSON){
//         console.log("entrar al juego")
//         console.log(JSON.gpid)
//         location.assign("/web/game.html?gp="+JSON.gpid);
//     })
//     .catch(error => error)
//     .then(json => console.log(json))
// // }

// function verDatosUser() {
//   let modalDiv = document.querySelector(".div-modal");
//   modalDiv.innerHTML = "";
//   modalDiv.classList.remove("bg-" + playerData.nation);
//   modalDiv.classList.add("bg-" + playerData.nation);

//   randomNation()
//   let div = document.createElement("div");
//   div.classList.add("datosUser");
//   div.classList.add("bg-color-" + playerData.nation);
//   div.style.color = "white";
//   const side = playerData.nation == "agua" || playerData.nation == "tierra" ? "left" : "right"
//   div.classList.add("datosUser-"+side);

//   let userName = document.createElement("P");
//   userName.innerText = playerData.email;
//   userName.classList.add("text-userName");

//   let divText = document.createElement("div");
//   divText.classList.add("divText");

//   let nation = document.createElement("P");
//   nation.innerText = playerData.nation + "natin";

//   let name = document.createElement("P");
//   name.innerText = playerData.name;
//   divText.appendChild(name);
//   divText.appendChild(nation);

//   let table = document.createElement("TABLE");
//   console.log(playerData.id);
//   console.log(tableUser(playerData.id));
//   table.classList.add("table-user");
//   table.innerHTML += tableUser(playerData.id);
//   div.appendChild(userName);
//   div.appendChild(divText);
//   div.appendChild(table);
//   modalDiv.appendChild(div);

//   const userDataHTML = `
//     <div class="datosUser datosUser-${side} bg-color-${playerData.nation}">
//       <p class="text-userName">${playerData.email}</p>
//       <div class="divText">
//         <p>${playerData.name}</p>
//         <p>${playerData.nation}</p>
//       </div>
//       <table class="table-user">
//         ${tableUser(playerData.id)}
//       </table>
//     </div>
//   `

//   modalDiv.innerHTML = userDataHTML;
// }