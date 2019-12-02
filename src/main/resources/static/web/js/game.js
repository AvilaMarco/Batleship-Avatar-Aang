let params = new URLSearchParams(location.search)
let gp = params.get("gp")
let shipsName = ["carrier","battleship","submarine","destroyer","patrol_boat"]
let playerDataGame1 = {}
let playerDataGame2 = {}
let myturn = null
let turnOpponent = null
let salvoes = {}
let canShoot = true
//setinterval
var intervalGamestard = null
var updateSalvoes = null
//botones dom
let backMenu = document.querySelector("#backmenu")
let backMenu2 = document.querySelector("#backmenu-endGame")
let send_Ships = document.querySelector("#sendShips")
let send_Salvo = document.querySelector("#sendSalvo")
let grid_salvoes = document.querySelector("#grid_salvoes")
let displayText = document.querySelector("#display")
let rematch = document.querySelector("#rematch")
//eventlistener
rematch.addEventListener('click',rematch)
backMenu.addEventListener('click',backmenu)
backMenu2.addEventListener('click',backmenu)
send_Ships.addEventListener('click',sendShips)
send_Salvo.addEventListener('click',sendSalvo)
if (screen.width < 1024){
  document.querySelector("#grid").addEventListener('touchstart',startMov)
  document.querySelector("#grid").addEventListener('touchend',endMov)
}
viewPlayer(gp,true,false)

function rematch(argument) {
 alert("Coming Soon")
}

function endGame(score) {
    document.querySelector(".flex-container").style.opacity = 0.1
    document.querySelector("#modal").classList.remove("d-none")
    document.querySelector("#modal").classList.add("modalAnimation")
    let texto = document.querySelector(".text-endgame")
    let img = document.querySelector(".img-endGame")
    if (score == 1){
      a("empate")
      texto.innerText = "Score +1"
    }else if(score == 3){
      img.src = "assets/img/victory.png"
      texto.innerText = "Score +3"
      a("victorya")
    }else if(score == 0){
      img.src = "assets/img/defeat.png"
      texto.innerText = "Good Game"
      a("perdida")
    }
}

function cargarDatosScreen(json) {
  // variables
  let player1 = document.querySelector("#player1")
  let player2 = document.querySelector("#player2")
  let dock = document.querySelector("#dock")
  let grid_salvoes = document.querySelector("#grid_salvoes")
  let grid = document.querySelector("#grid")

  Array.from(player1.children).forEach(e=>{
    if (e.tagName == "P"){
      e.innerText = playerDataGame1.email
    }else if(e.tagName == "IMG"){
      e.src = "assets/icons/border-"+playerDataGame1.nacion+".png"
    }
  })
  if (playerDataGame2 != null){
    Array.from(player2.children).forEach(e=>{
      if (e.tagName == "P"){
        e.innerText = playerDataGame2.email
      }else if(e.tagName == "IMG"){
        e.src = "assets/icons/border-"+playerDataGame2.nacion+".png"
      }
    })
  }
  a(json)
  // playerDataGame1
  // playerDataGame2
}

function a(argument) {
  console.log(argument)
}

function backmenu(){
  location.assign("/web/games.html");
}

function viewPlayer(gpid,isInit,isUpdateSalvo){
  fetch('/api/gp/'+gpid,{
    method: 'GET',
  })
  .then(function(response){
    if(response.ok){
      return response.json()
    }else{
     throw new Error(response.text())
    }
  })
  .then(function(JSON){
    console.log(JSON.Game_Started)
    playerDataGame1 = JSON.gamePlayers.filter(e=>e.id == gp)[0].player
    playerDataGame2 = JSON.gamePlayers.filter(e=>e.id != gp)[0]
    myturn = JSON.my_turn
    turnOpponent = JSON.Opponent_turn
    if (playerDataGame2!=null) {
      playerDataGame2 = playerDataGame2.player
    }
    salvoes = JSON.salvoes
    cargarDatosScreen(JSON)
    if (isInit) {
      if(JSON.ships.length == 0){
        defaultships()
      }else if(JSON.ships.length != 0 && !JSON.Game_Started){
        createShipsWeb(JSON)
        displayText.firstElementChild.innerText = "Wait Opponent..."
      }else if(JSON.Game_Started){
        createShipsWeb(JSON)
        isGameStart(JSON)
      }
    }else if(isUpdateSalvo){
      createSalvoes(JSON)
      a(JSON.Game_Over)
      if (JSON.Game_Over){
        window.clearInterval(updateSalvoes)
        console.log("fin del juego")
        // endGame(JSON.gamePlayers.filter(e=>e.id == gp)[0].score)
      }
    }else if(JSON.Game_Started){
      isGameStart(JSON)
    }
  })
  // .catch(error => console.log(error.message))
}

function isGameStart(JSON){
  document.querySelector("#dock .ships").appendChild(send_Salvo)
  // creola grilla de salvos 
  if (screen.width < 1024){
    createGrid(11, document.getElementById('grid'), 'salvoes','gridSalvos')
    document.querySelectorAll("#gridSalvos div[data-y]").forEach(e=>e.addEventListener('click',addsalvo))
  }else{
    createGrid(11, document.getElementById('grid_salvoes'), 'salvoes','gridSalvos')
    document.querySelectorAll("#grid_salvoes div[data-y]").forEach(e=>e.addEventListener('click',addsalvo))
    grid_salvoes.classList.remove("d-none")
  }
  createSalvoes(JSON)
  document.querySelector("#display").firstElementChild.innerText = "game started";
  setTimeout(function() {activarAnimation("toBot")},2000)
  intervalGamestard != null ? window.clearInterval(intervalGamestard):null

  updateSalvoes = window.setInterval(viewPlayer, 5000,gp,false,true);
  document.querySelector("#gridShips").style.position = "fixed"
  document.querySelector("#gridSalvos").style.position = "fixed"
}

/*FUNCIONES DE ANIMACION DE LA GRILLA*/
let posInit = null;
let posFin = null
function startMov(event) {
  posInit = event.changedTouches[0].screenY
}

function endMov(event) {
  posFin = event.changedTouches[0].screenY
  if ((posFin-posInit)>50){
    activarAnimation("toBot")
  }else if((posInit-posFin)>50){
    activarAnimation("toTop")
  }
}

function activarAnimation(moveTo) {
  let gridSalvo = document.querySelector("#gridSalvos")
  let gridShips = document.querySelector("#gridShips")
  if (gridShips.classList.contains("cero") && !gridSalvo.classList.contains("cero")){
    gridSalvo.classList.remove("Animation-toCeroFromDown","Animation-toUp","Animation-toCeroFromUp","Animation-toDown","cero")
    gridShips.classList.remove("Animation-toCeroFromDown","Animation-toUp","Animation-toCeroFromUp","Animation-toDown","cero")
    if (moveTo == "toTop"){
      gridSalvo.classList.add("Animation-toCeroFromDown")
      gridSalvo.classList.add("cero")
      gridShips.classList.add("Animation-toUp")
    }else if(moveTo == "toBot"){
      gridSalvo.classList.add("Animation-toCeroFromUp")
      gridSalvo.classList.add("cero")
      gridShips.classList.add("Animation-toDown")
    }
  }else if(!gridShips.classList.contains("cero") && gridSalvo.classList.contains("cero")){
    gridSalvo.classList.remove("Animation-toCeroFromDown","Animation-toUp","Animation-toCeroFromUp","Animation-toDown","cero")
    gridShips.classList.remove("Animation-toCeroFromDown","Animation-toUp","Animation-toCeroFromUp","Animation-toDown","cero")
    if (moveTo == "toTop"){
      gridSalvo.classList.add("Animation-toUp")
      gridShips.classList.add("Animation-toCeroFromDown")
      gridShips.classList.add("cero")
    }else if(moveTo == "toBot"){
      gridSalvo.classList.add("Animation-toDown")
      gridShips.classList.add("Animation-toCeroFromUp")
      gridShips.classList.add("cero")
    }
  }
}
/*FUNCIONES SHIPS*/
function defaultships(){
  [5,4,3,3,2].forEach((lengthShip,index)=> createShips(shipsName[index], lengthShip, 'horizontal', document.querySelector('#dock .ships'),false))
}

//me fijo si quedan barcos en el dock
function dockIsEmpty(){
    if(document.querySelectorAll("#dock .grid-item").length == 0){
        document.querySelector("#dock .ships").appendChild(send_Ships)
        send_Ships.classList.remove("d-none")
    }   
}

function sendShips(){
  let shipsObjects = []
  if (shipsName.every(e=>document.querySelector("#"+e).dataset.y != undefined)) {
    shipsName.forEach(e=>{
      let barco = document.querySelector("#"+e).dataset
      let position = []
      for (var i = 0; i < parseInt(barco.length); i++) {
        if (barco.orientation == "horizontal") {
          position.push(barco.y+(parseInt(barco.x)+i))
        }else{
          position.push(String.fromCharCode(barco.y.charCodeAt(0)+i)+(barco.x))
        }
      }
      shipsObjects.push({"typeShip":e.toUpperCase(),"shipLocations":position})
    })
    console.log(JSON.stringify(shipsObjects))
    fetch('/api/games/players/'+gp+'/ships',{
        method:'POST',
        body:JSON.stringify(shipsObjects),
        headers:{
            'Content-Type': 'application/json'
        }
    })
    .then(function(response){
        if(response.ok){
          console.log("good fetch")
          viewPlayer(gp,true,false)
          send_Ships.classList.add("d-none")
          displayText.firstElementChild.innerText = "Wait Opponent..."
          document.querySelector("#gridShips").style.position = "fixed"
          document.querySelector("#gridSalvos").style.position = "fixed"
          intervalGamestard = window.setInterval(viewPlayer, 3000,gp,false,false);
        }else{
            throw new Error(response.text())
        }
    })
    .catch(error => console.log(error.message))
  }else{
      console.log("faltan colocar ships")
  }  
}

function createShipsWeb(json){
  //elimino los barcos para no tener que cargar la pagina otra vez
  if (shipsName.every(e=>document.querySelector("#"+e)) && shipsName.every(e=>document.querySelector("#"+e).dataset.y != undefined)) {
    shipsName.forEach(e=>{
        document.querySelector("#"+e).parentNode.removeChild(document.querySelector("#"+e))
    })
  }

  //creo los bracos en la grilla
  for(let i = 0; i < json.ships.length;i++){
    let location = json.ships[i].locations[0]
    let orientation = json.ships[i].locations[0].substring(1) == json.ships[i].locations[1].substring(1) ? 'vertical' : 'horizontal'
    let type = json.ships[i].type_Ship
    let tamaño = json.ships[i].locations.length
    createShips(type.toLowerCase(), tamaño, orientation, document.getElementById('ships'+location),true)
  }
}

/*FUNCIONES SALVO*/
function createSalvoDock(numero) {
  for (var i = 0; i < numero; i++) {
    let div = document.createElement("div")
    div.classList.add(playerDataGame1.nacion)
    div.classList.add("salvo")
    document.querySelector(".municion").appendChild(div)
  }
}

function addsalvo(event){
    let celda = event.target;
    let salvoInDock = document.querySelector(".municion")
    if (celda.dataset.salvoes == undefined) {
      if(salvoInDock.childElementCount != 0 && celda.dataset.salvo == undefined){
        celda.classList.add(playerDataGame1.nacion)
        celda.dataset.salvo = true
        salvoInDock.firstElementChild.remove()
        console.log(playerDataGame1)
      }else if(celda.dataset.salvo){
        celda.classList.remove(playerDataGame1.nacion)
        let shoot = document.createElement("div")
            shoot.classList.add("salvo")
            shoot.classList.add(playerDataGame1.nacion)
        document.querySelector(".municion").appendChild(shoot)
        celda.removeAttribute("data-salvo")
        displayText.firstElementChild.innerText = "you can shoot"
      }else{
        console.log("mal")
        displayText.firstElementChild.innerText = "you have no shots left"
      }
    }
    if (salvoInDock.childElementCount == 0) {
      send_Salvo.classList.remove("d-none")
    }else{
      send_Salvo.classList.add("d-none")
    }
}

function sendSalvo(){
    let salvoInDock = document.querySelector(".municion")
    if(salvoInDock.childElementCount == 0){
        let salvo = []
        if (screen.width < 1024){
          document.querySelectorAll("#grid div[data-salvo]").forEach(p=>salvo.push(p.dataset.y+p.dataset.x))
        }else{
          document.querySelectorAll("#grid_salvoes div[data-salvo]").forEach(p=>salvo.push(p.dataset.y+p.dataset.x))
        }
        console.log(JSON.stringify(salvo))
        fetch('/api/games/players/'+gp+'/salvos',{
            method:'POST',
            body:JSON.stringify(salvo),
            headers:{
                'Content-Type': 'application/json'
            }
        })
        .then(function(response){
            if(response.ok){
                console.log("good fetch")
                if (screen.width < 1024) {
                  document.querySelectorAll("#grid div[data-salvo]").forEach(e=>{
                    e.classList.remove(playerDataGame1.nacion)
                    e.removeAttribute("data-salvo")
                  })
                }else{
                  document.querySelectorAll("#grid_salvoes div[data-salvo]").forEach(e=>{
                    e.classList.remove(playerDataGame1.nacion)
                    e.removeAttribute("data-salvo")
                  })
                }
                viewPlayer(gp,false,true)
                canShoot = true
            }else{
              return Promise.reject(response.json())
            }
        }).then()
        .catch(error => error).then(x => {
          document.querySelector("#display").firstElementChild.innerText = x.error
        })
    }else{
        console.log("todavia te quedan disparos")
    }
}

function createSalvoes(json){
  let idPlayer = json.gamePlayers.filter(e=>e.id == gp)[0].player.id
  let ultimoTiro = null
  let ultimoTurno = null
  for(let i = 0; i < json.salvoes.length; i++){
    if(json.salvoes[i].player == idPlayer){
      //pinto los disparos propios
      json.salvoes[i].locations.forEach(e=> {
        if (document.querySelector("#salvoes"+e).style.background=="") {
          if(json.salvoes[i].player == idPlayer && json.salvoes[i].nice_shoot!=null && json.salvoes[i].nice_shoot.includes(e)){
            document.querySelector("#salvoes"+e).classList.add("niceShoot")
          }else{
            document.querySelector("#salvoes"+e).classList.add("failShoot")   
          }
          document.querySelector("#salvoes"+e).dataset.salvoes = true
        }
      })
    }else{
      if (ultimoTurno < json.salvoes[i].turn){
        ultimoTiro = json.salvoes[i]
        ultimoTurno = json.salvoes[i].turn
      }
      //pinto los disparos del oponente
      json.salvoes[i].locations.forEach(e=> {
      if((json.ships.flatMap(s=>s.locations.map(p=>p))).includes(e) && document.querySelector("#ships"+e).style.background==""){
        document.querySelector("#ships"+e).classList.add("niceShoot")
      }
      })
    }
  }
  if (myturn <= turnOpponent && canShoot){
    if (ultimoTiro == null || ultimoTiro.ships_dead == null){
      createSalvoDock(5)
      canShoot = false
      send_Salvo.classList.add("d-none")
    }else{
      send_Salvo.classList.add("d-none")
      canShoot = false
      createSalvoDock((5-ultimoTiro.ships_dead.length))
    }
  }
  
}