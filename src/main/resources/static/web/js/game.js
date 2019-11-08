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
//eventlistener
backMenu.addEventListener('click',backmenu)
send_Ships.addEventListener('click',sendShips)
send_Salvo.addEventListener('click',sendSalvo)
viewPlayer(gp)


function dockIsEmpty(){
    if(document.querySelectorAll("#dock .grid-item").length == 0){
        document.querySelector("#dock").appendChild(send_Ships)
        send_Ships.classList.remove("d-none")
    }   
}

function addsalvo(event){
    let celda = event.target;
    let salvo = document.querySelectorAll("#grid_salvoes div[data-salvo]")
    if(salvo.length < 5 && celda.dataset.salvoes == undefined){
        celda.style.background = "green"
        celda.dataset.salvo = true
        console.log(celda)
    }
}

function isGameStart(){
    fetch("/api/gp/"+gp,{method: 'GET'})
    .then(function(response){
        if(response.ok){
            console.log("game start")
            return response.json()
        }else{
           throw new Error(response)
        }
    })
    .then(function(JSON){
        if(JSON.Game_Started){
            console.log("game star 2")
            document.querySelector("#dock").appendChild(send_Salvo)
            send_Salvo.classList.remove("d-none")
            grid_salvoes.classList.remove("d-none")
            document.querySelectorAll("#grid_salvoes div[data-y]").forEach(e=>e.addEventListener('click',addsalvo))
            createSalvoes(JSON)
            intervalGamestard != null ? window.clearInterval(intervalGamestard):null
            // updateSalvoes = window.setInterval(updateSalvoesgrid, 3000);
        }
    })
    .catch(error => console.log(error))
}

function updateSalvoesgrid(){
    fetch('/api/gp/'+gp,{
        method: 'GET',})
    .then(function(response){
        if(response.ok){
            return response.json()
        }else{
           throw new Error(response.text())
        }
    })
    .then(function(JSON){
       // createSalvoes(JSON)
       console.log(JSON)
    })
    .catch(error => console.log(error.message))
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
                // document.querySelectorAll("#grid_salvoes div[data-salvo]").forEach(e=>{
                //     e.style.background = ""})
                // updateSalvoesgrid()
                // return response.json()
            }else{
               throw new Error(response)
            }
        })
        .catch(error => console.log(error.message))
    }else{
        console.log("todavia te quedan disparos")
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
                viewPlayer(gp)
                send_Ships.classList.add("d-none")
                intervalGamestard = window.setInterval(isGameStart, 3000);
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
    createShips('carrier', 5, 'horizontal', document.getElementById('dock'),false)
    createShips('battleship', 4, 'horizontal', document.getElementById('dock'),false)
    createShips('submarine', 3, 'horizontal', document.getElementById('dock'),false)
    createShips('destroyer', 3, 'horizontal', document.getElementById('dock'),false)
    createShips('patrol_boat', 2, 'horizontal', document.getElementById('dock'),false)

}

function viewPlayer(gpid){
//    document.location.replace("/web/games.html?gp="+player_id)
//    let direccion = window.location.href
    fetch('/api/gp/'+gpid,{
        method: 'GET',})
    .then(function(response){
        if(response.ok){
            return response.json()
        }else{
           throw new Error(response.text())
        }
    })
    .then(function(JSON){
        console.log(JSON)
        if (JSON.ships.length != 0) {
            createShipsWeb(JSON)
            isGameStart()
        }else{
            defaultships()
        }
    })
    .catch(error => console.log(error.message))
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
            //pinto los disparos propios
            json.salvoes[i].locations.forEach(e=> {
                let textNode = document.createElement('SPAN')
                textNode.innerText = json.salvoes[i].turn
                document.querySelector("#salvoes"+e).appendChild(textNode)
                if(json.salvoes[i].player == idPlayer && json.salvoes[i].nice_shoot!=null && json.salvoes[i].nice_shoot.includes(e) && document.querySelector("#salvoes"+e).style.background==""){
                    document.querySelector("#salvoes"+e).style.background = "red"
                }else{
                    document.querySelector("#salvoes"+e).style.background = "green"   
                }
                document.querySelector("#salvoes"+e).dataset.salvoes = true
            })
        }else{
            //pinto los disparos del oponente
            json.salvoes[i].locations.forEach(e=> {
            if((json.ships.flatMap(s=>s.locations.map(p=>p))).includes(e) && document.querySelector("#salvoes"+e).style.background==""){
                let textNode = document.createElement('SPAN')
                textNode.innerText = json.salvoes[i].turn
                document.querySelector("#ships"+e).appendChild(textNode)
                document.querySelector("#ships"+e).style.background =  "red"
            }
            })
        }
    }
}