let params = new URLSearchParams(location.search)
let gp = params.get("gp")
let shipsName = ["carrier","battleship","submarine","destroyer","patrol_boat"]
// creola grilla de salvos 
document.querySelector("#backmenu").addEventListener('click',backmenu)
document.querySelector("#sendShips").addEventListener('click',sendShips)
createGrid(11, document.getElementById('grid_salvoes'), 'salvoes')
viewPlayer(gp)

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
                    position.push(String.fromCharCode(barco.y+i)+(barco.x))
                }
            }
            shipsObjects.push({"typeShip":e,"shipLocations":position})
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
            }else{
                throw new Error(response)
            }
        })
        .catch(error => console.log(error))
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
           return Promise.reject(response.json())
        }
    })
    .then(function(JSON){
        console.log(JSON)
        if (JSON.ships.length != 0) {
            createShipsWeb(JSON)
        }else{
            defaultships()
        }
    })
    .catch(error => error)
    .then(json => console.log(json.error))
}

function createShipsWeb(json){
    //creo los bracos en la grilla
    for(let i = 0; i < json.ships.length;i++){
        let location = json.ships[i].locations[0]
        let orientation = json.ships[i].locations[0].substring(1) == json.ships[i].locations[1].substring(1) ? 'vertical' : 'horizontal'
        let type = json.ships[i].type_Ship
        let tamaño = json.ships[i].locations.length
        createShips(type.toLowerCase(), tamaño, orientation, document.getElementById('ships'+location),true)
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
