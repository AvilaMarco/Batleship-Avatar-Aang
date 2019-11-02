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
    .then(function(response){
        if(response.ok){
            console.log("registre")
            accessWeb(null,username,password)
        }
    })
}

function botoneslogin()
{
    let creategame = document.querySelector("button[name*=CreateGame]")
    //variar botenes dependiendo si hay un usuario
    document.querySelector("button[name*=logout]").classList.remove("d-none")
    document.querySelector("button[name*=login]").classList.add("d-none")
    creategame.classList.remove("d-none")
    creategame.addEventListener('click',createGame)
    document.querySelector("button[name*=registre]").classList.add("d-none")
    document.querySelector("input[name*=user-name]").classList.add("d-none")
    document.querySelector("input[name*=user-password]").classList.add("d-none")
}

function accessWeb(event,usernameparam,passwordparam){
    let username = document.querySelector("input[name*=user-name]").value
    let password = document.querySelector("input[name*=user-password]").value
    if (usernameparam != null && passwordparam != null) {
        username = usernameparam
        password =passwordparam
    }
    let formData = new FormData();
        formData.append("user-name",username)
        formData.append("user-password",password)
    if (event.target.name == "login") {
        fetch('/api/login',{
            method:'POST',
            body:formData
        })
        .then(function(response){
            if(response.ok){
                console.log("nicelog")
                createTableGames()
                botoneslogin()
            }
        })
    }else if(event.target.name == "logout"){
        fetch('/api/logout',{
        method:'POST',
        })
        .then(function(response){
            if(response.ok){
                console.log("nice")
                document.querySelector("input[name*=user-name]").classList.remove("d-none")
                document.querySelector("input[name*=user-password]").classList.remove("d-none")
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
        </td>
        <td>
            <p>finish</p>
        </td>`
        if (player != "guest") {
            if (games[i].gameplayers.map(e=>e.player.id).includes(player.id)) {
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

// function querysUrl(search) {
//   var obj = {};
//   var reg = /(?:[?&]([^?&#=]+)(?:=([^&#]*))?)(?:#.*)?/g;

//   search.replace(reg, function(match, param, val) {
//     obj[decodeURIComponent(param)] = val === undefined ? "" : decodeURIComponent(val);
//   });

//   return obj;
// }

