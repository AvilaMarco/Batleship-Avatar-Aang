//cargar datos
let gamesData = [];
let players = [];
let masx,masy = false
let playerData = {}
let recargarMapa = false;
//referencias al DOM
let tablegame = document.querySelector("#game-body");
let tableranking = document.querySelector("#ranked-body");
let modal = document.querySelector("#registre");
let container = document.querySelector(".container");
let modalRegistre = document.querySelector("#modal-registre");
let logout = document.querySelector("#logout")
let verMapa = document.querySelector("#verMapa")
let info = document.querySelector("#info")
//eventlistener
logout.addEventListener('click',logoutFunction)
verMapa.addEventListener('click',viewMapa)
/*crear area clickable para la imagen*/
ubicacionesMap.forEach(area=>{
    let areahtml = document.createElement("area");
    areahtml.dataset.location = area.location
    areahtml.id = area.id
    areahtml.shape = "circle"
    areahtml.alt = "aremap"
    areahtml.addEventListener("click",selectGame)
    if (screen.width > 1024){
        areahtml.coords = parseInt(area.x)*0.69+","+parseInt(area.y)*0.725+","+area.r
    }else{
        areahtml.coords = area.x+","+area.y+","+area.r
    }
    document.querySelector("map[name*=mapeo]").appendChild(areahtml)
})
if (screen.width > 1024) {
    document.querySelector(".full_screen").classList.remove("d-none")
    document.querySelector(".full_screen-btn").classList.remove("d-none")
}
function full_screen(){
    document.querySelector(".full_screen").classList.add("d-none")
    document.querySelector(".full_screen-btn").classList.add("d-none")
}
reloadInfo()
function reloadInfo() {
    fetch('/api/games',{
        method: 'GET',
    })
    .then(function(response){if(response.ok){return response.json()}
    })
    .then(function(json){
        gamesData = json.games
        players = json.playerScore;
        playerData = json.player;
        playerScore = json.playerScore
        if(json.player.nacion != null){
            inMenu(json)
        }else{
            chooseNation()
        }
        let games = json.games.filter(e=>e.direccion!="00")
        if (games.length!=0){
            console.log(games)
            crearJuegosMap(games)
        }
    });
}

function setNacionPlayer(nacion) {
    fetch('/api/setNacionPlayer/'+nacion,{
        method: 'POST',
    }).then(function(response){
        if(response.ok){
            fetch('/api/games',{
                method: 'GET',
            })
            .then(function(response){if(response.ok){return response.json()}
            })
            .then(function(json){
                reloadInfo()
            });
        }
    })
}
    

function chooseNation() {
    document.querySelector("#inicoNacion").classList.remove("d-none")
}
function inMenu(json) {
    document.querySelector("#inicoNacion").classList.add("d-none")
    document.querySelector("#Player").classList.add("iconTransparent"+json.player.nacion) 
    document.querySelector("#webGames").classList.remove("d-none")
    document.querySelector("#botonera").classList.remove("d-none")
}


/*funciones para modal y cambiar los datos que muestra*/
function verdatos(elementhtml) {
   console.log(elementhtml)
   addmodal()
   if (elementhtml.id=="Player"){
    verDatosUser()
   }else if(elementhtml.id=="info"){
    verTutorial()
   }else if(elementhtml.id=="ladder"){
    createTableRanking()
   }
}
function nomodal(){
    document.querySelector("#modal").classList.add("modalAnimationout")
    document.querySelector("#modal").classList.add("d-none")
    document.querySelector("#modal").classList.remove("modalAnimation")
    document.querySelector("#container").style.opacity = 1;
}

function addmodal(){
    document.querySelector("#modal").classList.remove("d-none")
    document.querySelector("#modal").classList.remove("modalAnimationout")
    document.querySelector("#container").style.opacity = 0.2
    document.querySelector("#modal").classList.remove("d-none")
    document.querySelector("#modal").classList.add("modalAnimation")
}

function verDatosUser() {
    let modalDiv = document.querySelector(".div-modal")
        modalDiv.innerHTML = "" 
        modalDiv.classList.remove("bg"+playerData.nacion)
        modalDiv.classList.add("bg"+playerData.nacion)
        modalDiv.style.height = "80vh"
    let div = document.createElement("div")
        div.classList.add("datosUser")
        if (playerData.nacion == "agua" || playerData.nacion == "tierra") {
            div.classList.add("datosUser-left")
        }else{
            div.classList.add("datosUser-right")
        }
        div.classList.add("bg-color-"+playerData.nacion)
        div.style.color = "white"
        let userName = document.createElement("P")
            userName.innerText = playerData.email
            userName.classList.add("text-userName")
        let divText = document.createElement("div")
            divText.classList.add("divText")
            let nacion = document.createElement("P")
                nacion.innerText = playerData.nacion
            let name = document.createElement("P")
                name.innerText = playerData.name
            divText.appendChild(name)
            divText.appendChild(nacion)
        let table = document.createElement("TABLE")
            console.log(playerData.id)
            console.log(tableUser(playerData.id))
            table.classList.add("table-user")
            table.innerHTML +=tableUser(playerData.id)
    div.appendChild(userName)
    div.appendChild(divText)
    div.appendChild(table)
    modalDiv.appendChild(div) 
}

function tableUser(user) {
    console.log(playerScore)
    let datos = playerScore.filter(e=>e.id==user)[0].scores
    console.log(datos)
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
    `
    let win = 0,tied = 0,lose = 0,total = 0
    let winRate = "-"
    datos.forEach(e=>{
        total +=e
        switch(e){
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
    })
    if (datos.length != 0){
        winRate = parseInt(win*100/datos.length)
    }
    table +=`
    <tr>
        <td>${total}</td>
        <td>${win}</td>
        <td>${lose}</td>
        <td>${tied}</td>
        <td>${winRate}%</td>
    </tr>
    </tbody>
    `
    return table
}
function verTutorial(argument) {
    let modalDiv = document.querySelector(".div-modal")
        modalDiv.innerHTML = ""
        modalDiv.classList.remove("bg"+playerData.nacion)
    let img = document.createElement("IMG")
        img.classList.add("verTutorial")
        img.src = "assets/img/bg1.jpg"
    let text = document.createElement("P")
        text.innerText = "Tutorial Coming Soon"
        text.style.color = "black"
        text.style.position = "fixed"
        text.style.top = "19%"
        text.style.left = "35%"
        text.style.background = "white"
        text.style.borderRadius = "10px"
    modalDiv.appendChild(img)
    modalDiv.appendChild(text)
}
/* funciones relacionadas con los juegos, unirse, crear, volver*/
function crearJuegosMap(games) {
    Array.from(document.querySelector("#pivotMap").children).forEach(e=>e.remove())
    games.forEach(e=>{
        if (e.gameplayers[0].Score == null) {
            let areahtml = document.querySelector("#"+e.direccion)
            let div = document.createElement("div")
                div.classList.add("selectMap")
                div.dataset.game = "true"
                if(e.gameplayers.length == 2){
                    div.dataset.gpid1 = e.gameplayers[0].id
                    div.dataset.playerid1 = e.gameplayers[0].player.id
                    div.dataset.gpid2 = e.gameplayers[1].id
                    div.dataset.playerid2 = e.gameplayers[1].player.id
                    div.dataset.playername1 = e.gameplayers[0].player.email
                    div.dataset.playername2 = e.gameplayers[1].player.email
                    if (e.gameplayers.some(f=>f.player.id==playerData.id)) {
                        div.dataset.name = "Enter"
                        div.classList.add("SelectEnter")
                    }else{
                        div.classList.add("SelectInGame")
                        div.dataset.name = "InGame"
                    }
                }else{
                    div.dataset.gpid1 = e.gameplayers[0].id
                    div.dataset.playername1 = e.gameplayers[0].player.email
                    div.dataset.playerid1 = e.gameplayers[0].player.id
                    if(e.gameplayers.some(f=>f.player.id==playerData.id)){
                        div.dataset.name = "Enter"
                        div.classList.add("SelectEnter")
                    }else{
                        div.classList.add("selectJoin")
                        div.dataset.name = "Join" 
                    }
                }
                div.addEventListener('click',selectGame)
                div.dataset.gameid = e.id
                if (recargarMapa && screen.width < 1024){
                    div.style.top = parseInt(areahtml.coords.split(",")[1])+113+"px"
                    div.style.left = parseInt(areahtml.coords.split(",")[0])+109+"px"
                }else if(screen.width < 1024){
                    div.style.top = parseInt(areahtml.coords.split(",")[1])-37+"px"
                    div.style.left = parseInt(areahtml.coords.split(",")[0])-41+"px"
                }else{
                    div.style.top = parseInt(areahtml.coords.split(",")[1])+47+"px"
                    div.style.left = parseInt(areahtml.coords.split(",")[0])+166+"px"
                }
                div.dataset.location = areahtml.dataset.location
                div.id = areahtml.id
            document.querySelector("#pivotMap").appendChild(div) 
        }
    })
}

function selectGame(event) {
    if (document.querySelector("div[data-name*='selectGame']")!=null) {
        document.querySelector("#pivotMap").removeChild(document.querySelector("div[data-name*='selectGame']"))
    }
    let click = event.target;
    let div = document.createElement("div")
        div.dataset.name = "selectGame"
        div.classList.add("selectMap")
        div.addEventListener('click',removeSelect)
    if (click.dataset.game == "true") {
        div.classList.add("selectGameCreate")
        div.style.top = click.style.top
        div.style.left = click.style.left 
        div.dataset.location = click.dataset.location
        div.dataset.id = click.id
        document.querySelectorAll("div [name*='dataGame']").forEach(e=>{
            e.classList.remove("d-none")
            if (!(e.innerText=="Info") && !(click.dataset.playerid1 == playerData.id)){
                e.innerText = click.dataset.name
            }else if(!(e.innerText=="Info") && click.dataset.playerid1 == playerData.id){
                e.innerText = "Enter"
            }
        })
    }else{
        div.classList.add("selectCreate")
        if (screen.width > 1024) {
            div.style.top = parseInt(click.coords.split(",")[1])+51+"px"
            div.style.left = parseInt(click.coords.split(",")[0])+164+"px"
        }else{
            div.style.top = parseInt(click.coords.split(",")[1])+110+"px"
            div.style.left = parseInt(click.coords.split(",")[0])+109+"px"
        }
        div.dataset.location = click.dataset.location
        div.dataset.id = click.id
        document.querySelectorAll("div [name*='dataGame']").forEach(e=>{
            e.classList.remove("d-none")
            if (!(e.innerText=="Info")){
                e.innerText = 'Create'
            }
        })
    }
    document.querySelector("#pivotMap").appendChild(div)
    if (document.querySelector("div#"+document.querySelector("div[data-name*=selectGame]").dataset.id) == undefined){
        document.querySelector("#infoGame").classList.add("d-none")
    }
    console.log(div)
}

function removeSelect(event) {
   document.querySelector("#pivotMap").removeChild(event.target)
   document.querySelectorAll("div [name*='dataGame']").forEach(e=>e.classList.add("d-none"))
}

function infoGame() {
    addmodal()
    let modalDiv = document.querySelector(".div-modal")
        modalDiv.innerHTML = ""
        modalDiv.classList.remove("bg"+playerData.nacion)
    let select = document.querySelector("div#"+document.querySelector("div[data-name*=selectGame]").dataset.id)
    let div = document.createElement("div")
        div.classList.add("div-infoGame")
        let title = document.createElement("H1")
            title.style.margin = "0"
        let textoPlayer1 = document.createElement("P")
        let textoPlayer2 = document.createElement("P")
        let divStatus = document.createElement("div")
            divStatus.classList.add("divStatus")
        let textoId = document.createElement("P")
        let textoStatus = document.createElement("P")

        title.innerText = "Nacion de "+select.dataset.location
        textoId.innerText = "Game Id: "+select.dataset.gameid
        textoPlayer1.innerText = "Player 1: "+select.dataset.playername1
        if (select.dataset.playername2 != undefined){
            textoPlayer2.innerText = "Player 2: "+select.dataset.playername2
            textoStatus.innerText = "Status: In Game"
        }else{
            textoPlayer2.innerText = "Player 2: -"
            textoStatus.innerText = "Status: Create"
        }

        divStatus.appendChild(textoId)
        divStatus.appendChild(textoStatus)
        div.appendChild(title)
        div.appendChild(divStatus)
        div.appendChild(textoPlayer1)
        div.appendChild(textoPlayer2)
    modalDiv.appendChild(div)
}

function enterGame(event) {
    let datosDelJuego = document.querySelector("div#"+document.querySelector("div[data-name*='selectGame']").dataset.id)
    if (event.innerText == "Create"){
        let data = document.querySelector("div[data-name*=selectGame]")
        crearjuego(data.dataset.location,data.dataset.id)
    }else if(event.innerText == "Enter"){
        if (datosDelJuego.dataset.playerid1 == playerData.id){
            window.location.href = "/web/game.html?gp="+datosDelJuego.dataset.gpid1
        }else if(datosDelJuego.dataset.playerid2 == playerData.id){
            window.location.href = "/web/game.html?gp="+datosDelJuego.dataset.gpid2
        }
    }
    else if (event.innerText == "Join"){
        joinGame(datosDelJuego.dataset.gameid)
    }else if(event.innerText == "InGame"){
        alert("Coming soon spectator mode")
    }
}

function joinGame(gameid){
    fetch('/api/game/'+gameid+'/players',{
    method: 'POST',
    }).then(function(response){if(response.ok){return response.json()}
    }).then(function(json){
        location.assign("/web/game.html?gp="+json.gpid);
        console.log(json.gpid)
    }).catch(function(error) {
  console.log('Hubo un problema con la petición Fetch:' + error.message);
});
}

function crearjuego(location,ubicacion) {
    fetch('/api/games/'+location+'/'+ubicacion+'/'+false,{
        method:'POST'
    })
    .then(function(response){
        if(response.ok){
            return response.json()
        }else{
            throw new Error(response.json());
        }
    })
    .then(function(JSON){
        window.location.href = "/web/game.html?gp="+JSON.gpid
    })
    .catch(error => error)
    .then(json => console.log(json))
}

function viewMenu() {
    document.querySelector("#mapabg").classList.remove("d-none")
    document.querySelector("#back").classList.add("d-none")
    document.querySelector("#reload").classList.add("d-none")
    document.querySelectorAll("button[name*='botonesMenu']").forEach(e=>e.classList.remove("d-none"))
    document.querySelectorAll("div [name*='dataGame']").forEach(e=>e.classList.add("d-none"))
}

function viewMapa(event)
{
    let back = document.querySelector("#back")
        back.classList.remove("d-none")
        back.addEventListener('click',viewMenu)
    let reload = document.querySelector("#reload")
        reload.classList.remove("d-none")
        reload.addEventListener('click',function(){
            recargarMapa = true
            reloadInfo();
            reload.classList.add("Animation-reload")
            setTimeout(function() {document.querySelector("#reload").classList.remove("Animation-reload")},1100)
        })
    let mapa = document.querySelector("#mapa").classList.add("marginMap")
    document.querySelector("#mapabg").classList.add("d-none")
    document.querySelectorAll("button[name*='botonesMenu']").forEach(e=>e.classList.add("d-none"))
    if (document.querySelector("div[data-game*='true']") != null && !document.querySelector("div[data-game*='true']").dataset.move) {
        document.querySelectorAll("div[data-game*='true']").forEach(e=>{
        if (screen.width < 1024){
            e.style.top = parseInt(e.style.top.split("px")[0])+150+"px"
            e.style.left = parseInt(e.style.left.split("px")[0])+150+"px"
        }else{
            e.style.top = parseInt(e.style.top.split("px")[0])+"px"
            e.style.left = parseInt(e.style.left.split("px")[0])+"px"
        }
        e.dataset.move = true
        })
    }
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
    let modalDiv = document.querySelector(".div-modal")
    modalDiv.innerHTML = ""
    modalDiv.classList.remove("bg"+playerData.nacion)
    let body = ``
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
    `
    for (var i = 0; i < players.length; i++) {
        if (players[i].scores.length != 0) {
            body +=`<tr>
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
    body +=`</tbody></table>`
    modalDiv.innerHTML += body
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
    //     document.querySelectorAll("button[name*='botonesMenu']").forEach(e=>e.classList.add("d-none"))
    //     document.querySelector("#mapabg").classList.add("d-none")
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
    //     document.querySelector("#mapabg").classList.remove("d-none")
    //     document.querySelectorAll("button[name*='botonesMenu']").forEach(e=>e.classList.remove("d-none"))
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
// }

