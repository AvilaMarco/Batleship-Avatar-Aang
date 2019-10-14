let direccion = window.location.href
let games = [];
let table = document.querySelector("#tableGames");
let title = document.querySelector("#duelo")
createGrid(11, document.getElementById('grid_salvoes'), 'salvoes')

fetch('http://localhost:8080/api/games',{
	method: 'GET',
	}).then(function(response){if(response.ok){return response.json()}
	}).then(function(JSON){
	games = JSON;
    runWeb();
});

function runWeb(){
    for(let i = 0;i<games.length;i++){
        table.innerHTML +=`
        <tr>
            <td>
                <p>${games[i].id}</p>
            </td>
            <td>
                <p>${games[i].created}</p>
            </td>
            <td>
                <p>${games[i].gameplayer[0].player.id}</p>
            </td>
            <td>
                <p>${games[i].gameplayer[1].player.id}</p>
            </td>
        </tr>`
    }

    gameView(querysUrl(direccion).gp)
}

function querysUrl(search) {
  var obj = {};
  var reg = /(?:[?&]([^?&#=]+)(?:=([^&#]*))?)(?:#.*)?/g;

  search.replace(reg, function(match, param, val) {
    obj[decodeURIComponent(param)] = val === undefined ? "" : decodeURIComponent(val);
  });

  return obj;
}

function gameView(gamePlayer_Id){
fetch('http://localhost:8080/api/gp/'+gamePlayer_Id,{
	method: 'GET',
	}).then(function(response){if(response.ok){return response.json()}
	}).then(function(JSON){
	createShipsWeb(JSON)
});
}

function createShipsWeb(json){
    title.innerHTML +=`
    <span>${json.gamePlayers[0].email}(you)</span> vs <span>${json.gamePlayers[1].email}</span>
    `
    //creo los bracos en la grilla
    for(let i = 0; i < json.ships.length;i++){
        let location = json.ships[i].locations[0]
        let orientation = json.ships[i].locations[0].substring(1) == json.ships[i].locations[1].substring(1) ? 'vertical' : 'horizontal'
        let type = json.ships[i].type_Ship
        let tamaño = json.ships[i].locations.length
        createShips(type, tamaño, orientation, document.getElementById('ships'+location),true)
    }

    //pinto los disparos propios
    let idPlayer = json.gamePlayers[0].id
    for(let i = 0; i < json.salvoes.length; i++){
        if(json.salvoes[i].player == idPlayer){
            json.salvoes[i].locations.forEach(e=> document.querySelector("#salvoes"+e).style.background = "green")
        }else{
            json.salvoes[i].locations.forEach(e=> document.querySelector("#ships"+e).style.background =  "red")
        }
    }

//    //pinto los disparos del oponente
//    for(let i = 0; i < json.salvoes_opponent.length; i++){
//        json.salvoes_opponent[i].locations.forEach(e=> document.querySelector("#ships"+e).style.background =  "red")
//        }
}

