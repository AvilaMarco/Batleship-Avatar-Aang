//cargar datos
let games = [];
let players = [];
fetch('http://localhost:8080/api/games',{
    method: 'GET',
    }).then(function(response){if(response.ok){return response.json()}
    }).then(function(json){
    games = json.games;
    players = json.playerScore;
    runweb();
});
//referencias al DOM
let tablegame = document.querySelector("#game-body");
let tableranking = document.querySelector("#ranked-body");
let modal = document.querySelector("#registre");
let container = document.querySelector(".container");
let modalRegistre = document.querySelector("#modal-registre");
//botones eventlistener
modal.addEventListener('click',addmodal);
document.querySelector("#btn-login").addEventListener('click',accessWeb)
document.querySelector("#btn-logout").addEventListener('click',accessWeb)

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

//runweb
function runweb(){
    createTableRanking()
    createTableGames();
}
//LOGIN
function registre(){
    let firstName = document.querySelector("input[name*=firstName]").value
    let lastName = document.querySelector("input[name*=lastName]").value
    let username = document.querySelector("input[name*=Username]").value
    let Email = document.querySelector("input[name*=Email]").value
    let password = document.querySelector("input[name*=password]").value
    let formData = new FormData();
        formData.append("firstName",firstName)
        formData.append("lastName",lastName)
        formData.append("email",Email)
        formData.append("password",password)
        formData.append("username",username)
    fetch('/api/players',{
        method:'POST',
        body:formData
    })
    .then(function(response){if(response.ok){console.log("registre")}})
}

function accessWeb(event){
    let username = document.querySelector("input[name*=user-name]").value
    let password = document.querySelector("input[name*=user-password]").value
    let formData = new FormData();
        formData.append("password",password)
        formData.append("username",username)
    if (event.target.name == "login") {
        fetch('/api/login',{
            method:'POST',
            body:formData
        })
        .then(function(response){
            if(response.ok){
                console.log("nicelog")
                createTableGames()
                let creategame = document.querySelector("button[name*=CreateGame]")
                //variar botenes dependiendo si hay un usuario
                document.querySelector("button[name*=logout]").classList.remove("d-none")
                document.querySelector("button[name*=login]").classList.add("d-none")
                creategame.classList.remove("d-none")
                creategame.addEventListener('click',createGame)
                document.querySelector("button[name*=registre]").classList.add("d-none")
            }
        })
    }else if(event.target.name == "logout"){
        fetch('/api/logout',{
        method:'POST',
        })
        .then(function(response){
            if(response.ok){
                console.log("nice")
                document.querySelector("button[name*=logout]").classList.add("d-none")
                document.querySelector("button[name*=login]").classList.remove("d-none")
                document.querySelector("button[name*=CreateGame]").classList.add("d-none")
                document.querySelector("button[name*=registre]").classList.remove("d-none")
            }
        })
    }
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
    fetch('http://localhost:8080/api/games',{
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
            </td>
            <td>
                <p>finish</p>
            </td>`
            if (player != "guest") {
                if (player.id == games[i].gameplayers[0].player.id) {
                tabla +=`
                <td>
                    <button data-gameid="games[i].id">enter game</button>
                </td>
                <td>
                    <p>${(games[i].gameplayers.length==2?games[i].gameplayers[1].player.id : "non-rival")}</p>
                </td>`
                }else{
                    tabla +=`
                <td>
                    <p>${games[i].gameplayers[0].player.id}</p>
                </td>
                <td>
                    <p>${(games[i].gameplayers.length==2?games[i].gameplayers[1].player.id : "non-rival")}</p>
                    <button>join game</button>
                </td>
            </tr>`
                }
            }else{
                tabla +=`
                <td>
                    <p>${games[i].gameplayers[0].player.id}</p>
                </td>
                <td>
                    <p>${(games[i].gameplayers.length==2?games[i].gameplayers[1].player.id : "non-rival")}</p>
                    <button>join game</button>
                </td>
            </tr>`
            }
    }
tablegame.innerHTML = tabla
    });
}
            // <td>
            //     <button data-gameid="${games[i].id}" data-players="${games[i].gameplayers.map(e=>e.player.id)}" class="join-created">join / enter</button>
            // </td>
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
        // location.assign(location.href+"?gp="+JSON.gpid);
    })
    .catch(error => error)
    .then(json => console.log(json))
}
//TABLE SALVO

//NAV SALVO





// obtener querys de el link de la pagina
// let params = new URLSearchParams(location.search)
// let gp = params.get("gp")
var gp = querysUrl(window.location.href).gp
//creo la grila

// let webJuegos = document.querySelector(".juegos")
// let webRanking = document.querySelector(".ranking")
// let webPlayer = document.querySelector(".player")
// let webAccess = document.querySelector(".access")
// let webRegistre = document.querySelector(".registre")
//botones
//event listener
// btnGames.addEventListener('click',cambiarPage);
// btnRank.addEventListener('click',cambiarPage);
// btnPlayer.addEventListener('click',cambiarPage);
// btnAccess.addEventListener('click',cambiarPage);
// btnLogout.addEventListener('click',accessWeb);
// btnRegistre.addEventListener('click',cambiarPage);
// //registro
// document.querySelector("#form").addEventListener('submit',accessWeb)
// document.querySelector("#registre").addEventListener('submit',registre);

function joinGame(gameid){
    fetch('/api/game/'+gameid+'/players',{
    method: 'POST',
    }).then(function(response){if(response.ok){return response.json()}
    }).then(function(JSON){
        console.log(JSON.gpid)
    });
}

function enterGame(event){
    fetch('http://localhost:8080/api/games',{
    method: 'GET',
    }).then(function(response){if(response.ok){return response.json()}
    }).then(function(JSON){
        if (JSON.player != "guest") {
            let game = JSON.games.filter(e=>e.id == event.target.dataset.gameid); 
            if (event.target.dataset.players.includes((""+JSON.player.id))) {
                let gp = game[0].gameplayers.filter(e=>e.player.id == JSON.player.id)
                location.assign(location.href+"?gp="+gp[0].id);
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

function viewPlayer(){
//    document.location.replace("/web/games.html?gp="+player_id)
//    let direccion = window.location.href
    fetch('/api/gp/'+gp,{
    	method: 'GET',})
    .then(function(response){
        if(response.ok){
            return response.json()
        }else{
           return Promise.reject(response.json())
        }
    })
    .then(function(JSON){
    	console.log(JSON)
    	createShipsWeb(JSON)
    })
    .catch(error => error)
    .then(json => console.log(json.error))

}





function querysUrl(search) {
  var obj = {};
  var reg = /(?:[?&]([^?&#=]+)(?:=([^&#]*))?)(?:#.*)?/g;

  search.replace(reg, function(match, param, val) {
    obj[decodeURIComponent(param)] = val === undefined ? "" : decodeURIComponent(val);
  });

  return obj;
}

function createShipsWeb(json){
    //creo los bracos en la grilla
    for(let i = 0; i < json.ships.length;i++){
        let location = json.ships[i].locations[0]
        let orientation = json.ships[i].locations[0].substring(1) == json.ships[i].locations[1].substring(1) ? 'vertical' : 'horizontal'
        let type = json.ships[i].type_Ship
        let tamaño = json.ships[i].locations.length
        createShips(type, tamaño, orientation, document.getElementById('ships'+location),true)
    }

    let idPlayer = json.gamePlayers.filter(e=>e.id == gp)[0].player.id
    for(let i = 0; i < json.salvoes.length; i++){
        if(json.salvoes[i].player == idPlayer){
            //pinto los disparos propios
            json.salvoes[i].locations.forEach(e=> document.querySelector("#salvoes"+e).style.background = "green")
        }else{
            //pinto los disparos del oponente
            json.salvoes[i].locations.forEach(e=> {
            if((json.ships.flatMap(s=>s.locations.map(p=>p))).includes(e)){

            document.querySelector("#ships"+e).style.background =  "red"
            }
            })
        }
    }
}