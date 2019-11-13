let params = new URLSearchParams(location.search)
let gp = params.get("gp")
let shipsName = ["carrier","battleship","submarine","destroyer","patrol_boat"]
//setinterval
var intervalGamestard = null
var updateSalvoes = null
// creola grilla de salvos 
createGrid(11, document.getElementById('grid_salvoes'), 'salvoes')
//botones dom
let backMenu = document.querySelector("#backmenu")
let send_Ships = document.querySelector("#sendShips")
let send_Salvo = document.querySelector("#sendSalvo")
let grid_salvoes = document.querySelector("#grid_salvoes")
let displayText = document.querySelector("#display")
//eventlistener
backMenu.addEventListener('click',backmenu)
send_Ships.addEventListener('click',sendShips)
send_Salvo.addEventListener('click',sendSalvo)
viewPlayer(gp,true,false)


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

function backmenu(){
  location.assign("/web/games.html");
}

function defaultships(){
  [5,4,3,3,2].forEach((lengthShip,index)=> createShips(shipsName[index], lengthShip, 'horizontal', document.querySelector('#dock .ships'),false))
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
    }else if(JSON.Game_Started){
      isGameStart(JSON)
    }
  })
  .catch(error => console.log(error.message))
}

function isGameStart(JSON){
  document.querySelector("#dock .ships").appendChild(send_Salvo)
  send_Salvo.classList.remove("d-none")
  grid_salvoes.classList.remove("d-none")
  document.querySelectorAll("#grid_salvoes div[data-y]").forEach(e=>e.addEventListener('click',addsalvo))
  createSalvoes(JSON)
  displayText.firstElementChild.innerText = "game started"
  intervalGamestard != null ? window.clearInterval(intervalGamestard):null
  document.querySelector("H1").innerText = "player 1 vs player 2"
  document.querySelector("H2").innerText = "turno player 1 vs turn player 2"
  // updateSalvoes = window.setInterval(updateSalvoesgrid, 3000);
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

function createSalvoes(json){
  let idPlayer = json.gamePlayers.filter(e=>e.id == gp)[0].player.id
  for(let i = 0; i < json.salvoes.length; i++){
    if(json.salvoes[i].player == idPlayer){
      //pinto los disparos propios document.querySelector("#salvoes"+e).style.background==""
      json.salvoes[i].locations.forEach(e=> {
        if (document.querySelector("#salvoes"+e).style.background=="") {
          let textNode = document.createElement('SPAN')
          textNode.innerText = json.salvoes[i].turn
          document.querySelector("#salvoes"+e).appendChild(textNode)
          if(json.salvoes[i].player == idPlayer && json.salvoes[i].nice_shoot!=null && json.salvoes[i].nice_shoot.includes(e)){
            document.querySelector("#salvoes"+e).style.background = "red"
          }else{
            document.querySelector("#salvoes"+e).style.background = "green"   
          }
          document.querySelector("#salvoes"+e).dataset.salvoes = true
        }
      })
    }else{
      //pinto los disparos del oponente
      json.salvoes[i].locations.forEach(e=> {
      if((json.ships.flatMap(s=>s.locations.map(p=>p))).includes(e) && document.querySelector("#ships"+e).style.background==""){
        let textNode = document.createElement('SPAN')
        textNode.innerText = json.salvoes[i].turn
        document.querySelector("#ships"+e).appendChild(textNode)
        document.querySelector("#ships"+e).style.background =  "red"
      }
      })
    }
  }
}

function addsalvo(event){
    let celda = event.target;
    let salvo = document.querySelectorAll("#grid_salvoes div[data-salvo]")
    if (celda.dataset.salvoes == undefined) {
      if(salvo.length < 5 && celda.dataset.salvo == undefined){
        celda.style.background = "green"
        celda.dataset.salvo = true
        console.log(celda)
      }else if(celda.dataset.salvo){
        celda.style.background = ""
        celda.removeAttribute("data-salvo")
        displayText.firstElementChild.innerText = "you can shoot"
      }else{
        console.log("mal")
        displayText.firstElementChild.innerText = "you have no shots left"
      }
    }
}

function sendSalvo(){
    let auxsalvo = document.querySelectorAll("#grid_salvoes div[data-salvo]")
    if(auxsalvo.length == 5){
        let salvo = []
        auxsalvo.forEach(p=>salvo.push(p.dataset.y+p.dataset.x))
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
                document.querySelectorAll("#grid_salvoes div[data-salvo]").forEach(e=>{
                    e.style.background = ""})
                document.querySelectorAll("#grid_salvoes div[data-salvo]").forEach(e=>e.removeAttribute("data-salvo"))
                viewPlayer(gp,false,true)
                // return response.json()
            }else{
              // displayText.firstElementChild.innerText = "you can shoot"
              //console.log(response.text())
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