//cargar datos
let games = [];
let players = [];
fetch('/api/games',{
    method: 'GET',
    }).then(function(response){if(response.ok){return response.json()}
    }).then(function(json){
    games = json.games;
    players = json.playerScore;
    if(json.player != "guest"){
        botoneslogin()
    }
    runweb();
});
//referencias al DOM
let tablegame = document.querySelector("#game-body");
let tableranking = document.querySelector("#ranked-body");
let modal = document.querySelector("#registre");
let container = document.querySelector(".container");
let modalRegistre = document.querySelector("#modal-registre");
let logout = document.querySelector("#logout")
let verMapa = document.querySelector("#verMapa")
let play = document.querySelector("#play")
let info = document.querySelector("#info")
//botones eventlistener
// modal.addEventListener('click',addmodal);
logout.addEventListener('click',logoutFunction)
verMapa.addEventListener('click',viewMapa)

/*CONFIGURACION MAPA PARA ELEGIR EL LUGAR DE JUEGO*/
// let img = document.createElement("img");
//     img.src = "mapa-game-mobile.png"
//     img.usemap = "#mapeo"
//     img.id = "mapa"
// let areasmap = document.createElement("map");
//     areasmap.name = "mapeo"
// ubicacionesMap.forEach(area=>{
//     let areahtml = document.createElement("area");
//     areahtml.dataset.location = area.location
//     areahtml.id = area.id
//     areahtml.shape = "circle"
//     areahtml.alt = "aremap"
//     areahtml.addEventListener("click",selectGame)
//     areahtml.coords = area.x+","+area.y+","+area.r
//     areasmap.appendChild(areahtml)
// })
// document.querySelector("#mapa-div").appendChild(areasmap)
// document.querySelector("#mapa-div").appendChild(img)

//ANDANDO 
// let divPadre = document.createElement("div")
ubicacionesMap.forEach(area=>{
    let areahtml = document.createElement("area");
    areahtml.dataset.location = area.location
    areahtml.id = area.id
    areahtml.shape = "circle"
    areahtml.alt = "aremap"
    areahtml.title = "hola"
    areahtml.addEventListener("click",selectGame)
    areahtml.addEventListener("mouseup",positionmouse)
    areahtml.coords = area.x+","+area.y+","+area.r
    document.querySelector("map[name*=mapeo]").appendChild(areahtml)
    let div = document.createElement("div")
    div.style.top = parseInt(area.y)-30+"px"
    div.style.left = parseInt(area.x)-35+"px"
    div.style.width = "70px"
    div.style.height = "70px"
    div.classList.add(area.location)
    div.classList.add("position")
    div.style.zIndex = 99
    document.querySelector("#pivotMap").appendChild(div)
})
// document.querySelector("#mapa-div").appendChild(divPadre)
// let div = document.createElement("div")
// div.style.position = "absolute"
// div.style.top = "64px"
// div.style.left = "640px"
// div.style.width = "50px"
// div.style.height = "50px"
// div.style.zIndex = 99
// div.classList.add("agua")
// crear divs vacios con las coordenadas de las "area" para luego agregar logos de nacion
function positionmouse(event) {
    console.log(event)
    // body...
}

function selectGame(event) {
    let area = event.target
    console.log(area)
    // let div = document.createElement("div")
    // div.style.position = "sticky"
    // div.style.top = area.coords.split(",")[0]+"px"
    // div.style.left = area.coords.split(",")[1]+"px"
    // div.style.width = "70px"
    // div.style.height = "70px"
    // div.classList.add(area.dataset.location)
    // div.style.zIndex = 99
    // document.querySelector("#mapa-div").appendChild(div)
}
function nomodal(){
    modalRegistre.classList.remove("modal")
    modalRegistre.style.display = "none";
    container.style.opacity = 1;
}

function addmodal(){
    container.style.opacity = 0.01
    modalRegistre.style.display = "block";
    modalRegistre.classList.add("modal")
    document.querySelector("#btn-close").addEventListener('click',nomodal)
    document.querySelector("#btn-registre").addEventListener('click',registre)
}
function viewMapa(event)
{
    if (event.target.innerText == "Mapa") {
        event.target.innerText = "Menu"
        document.querySelector("#mapabg").classList.add("d-none")
        toggleBTN()    
    }else{
       event.target.innerText = "Mapa"
        document.querySelector("#mapabg").classList.remove("d-none")
        toggleBTN()     
    }
}
function toggleBTN()
{
   logout.classList.toggle("d-none")
   play.classList.toggle("d-none")
   info.classList.toggle("d-none")
}
//runweb
function runweb(){
    createTableRanking()
    createTableGames();
}
//LOGOUT
function logoutFunction()
{
    fetch('/api/logout',{
    method:'POST',
    })
    .then(function(response){
        location.assign("/");
    })
}

//TABLE RANKED
function createTableRanking(){
    players.forEach(e=>{e.total=0;e.Won=0;e.Lost=0;e.Tied=0})
    for(let i=0; i < players.length; i++){
        for(let j = 0; j < players[i].scores.length; j++){
            players[i].total+=players[i].scores[j]
            switch(players[i].scores[j]){
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
    players.sort(function(a, b) {return b.total - a.total;});
    tableranking.innerHTML = ""
    for (var i = 0; i < players.length; i++) {
        if (players[i].scores.length != 0) {
            tableranking.innerHTML+=`<tr>
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
        </tr>`
        }
    }
}
//TABLE GAMES
function createTableGames(){
    let player;
    fetch('/api/games',{
    method: 'GET',
    }).then(function(response){if(response.ok){return response.json()}
    }).then(function(json){
    games = json.games;
    player = json.player
    tablegame.innerHTML = "";
    let tabla = "";
    for(let i = 0;i<games.length;i++){
        tabla +=`<tr>
        <td>
            <p>${games[i].id}</p>
        </td>
        <td>
            <p>${games[i].created}</p>
        </td>`
        if (games[i].gameplayers[0].Score != null) {
            tabla +=`<td><p>Finish</p></td>`
        }else{
            tabla +=`<td><p>in Game</p></td>`
        }
        if (player != "guest") {
            if (games[i].gameplayers.map(e=>e.player.id).includes(player.id) && games[i].gameplayers[0].Score == null) {
               tabla +=`
                <td>
                    <button id="entergame" data-players="${games[i].gameplayers.map(e=>e.player.id)}" data-gameid="${games[i].id}">enter game</button>
                </td>
                <td>
                    <p>${(games[i].gameplayers.length==2?
                        (games[i].gameplayers[1].player.id == player.id?games[i].gameplayers[0].player.id:games[i].gameplayers[1].player.id) : "non-rival")}</p>
                </td>`
            }else if(games[i].gameplayers.length==1){
                tabla +=`
                <td>
                   <p>${games[i].gameplayers[0].player.id}</p> 
                </td>
                <td>
                    <button id="joingame" data-players="${games[i].gameplayers.map(e=>e.player.id)}" data-gameid="${games[i].id}">join game</button>
                </td>`
            }else{
                tabla +=`
                <td>
                   <p>${games[i].gameplayers[0].player.id}</p> 
                </td>
                <td>
                    <p>${games[i].gameplayers[1].player.id}</p>
                </td>`
            }
        }else{
            tabla +=`
                <td>
                   <p>${games[i].gameplayers[0].player.id}</p> 
                </td>
                <td>
                    <p>${(games[i].gameplayers.length==2?games[i].gameplayers[1].player.id : "non-rival")}</p>
                </td>`
        }

    }    
tablegame.innerHTML = tabla
    document.querySelectorAll('#entergame').forEach(e=>e.addEventListener('click',enterGame))
    document.querySelectorAll('#joingame').forEach(e=>e.addEventListener('click',joinGame))
    });
}

function createGame(){
    fetch('/api/games',{
        method:'POST'
    })
    .then(function(response){
        if(response.ok){
            createTableGames()
            return response.json()
        }else{
            throw new Error(response.json());
        }
    })
    .then(function(JSON){
        console.log("entrar al juego")
        console.log(JSON.gpid)
        location.assign("/web/game.html?gp="+JSON.gpid);
    })
    .catch(error => error)
    .then(json => console.log(json))
}

function joinGame(event){
    fetch('/api/game/'+event.target.dataset.gameid+'/players',{
    method: 'POST',
    }).then(function(response){if(response.ok){return response.json()}
    }).then(function(json){
        location.assign("/web/game.html?gp="+json.gpid);
        console.log(json.gpid)
    }).catch(function(error) {
  console.log('Hubo un problema con la petición Fetch:' + error.message);
});
}

function enterGame(event){
    fetch('/api/games',{
    method: 'GET',
    }).then(function(response){if(response.ok){return response.json()}
    }).then(function(JSON){
        if (JSON.player != "guest") {
            let game = JSON.games.filter(e=>e.id == event.target.dataset.gameid); 
            if (event.target.dataset.players.includes((""+JSON.player.id))) {
                let gp = game[0].gameplayers.filter(e=>e.player.id == JSON.player.id)
                location.assign("/web/game.html?gp="+gp[0].id);
                console.log(gp[0].id)
            }else if (game[0].gameplayers.length == 1) {
                joinGame(game[0].id)
            }else{
                alert("you can't join the game")
            }
        }else{
            alert("Login to join or create a game")
        }
    });
}