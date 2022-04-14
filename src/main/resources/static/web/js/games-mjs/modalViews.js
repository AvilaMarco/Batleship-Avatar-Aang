function createTableRanking(players) {

  players.sort((a, b) => b.stats.score - a.stats.score);

  let body = ``;
  body += `
    <div class="table-game">
      <table class="table">
          <thead class="">
              <tr>
                  <th>Player</th>
                  <th>Score</th>
                  <th>Win</th>
                  <th>Tied</th>
                  <th>Lose</th>
              </tr>
          </thead>
          <tbody id="ranked-body">
      `;
  players.forEach(player => {
    body += `<tr>
      <td>
          <p>${player.email}</p>
      </td>
      <td>
          <p>${player.stats.score}</p>
      </td>
      <td>
          <p>${player.stats.won}</p>
      </td>
      <td>
          <p>${player.stats.tied}</p>
      </td>
      <td>
          <p>${player.stats.lost}</p>
      </td>
    </tr>`;
  })
  body += `</tbody></table></div>`;
  let modalDiv = document.querySelector(".div-modal");
  modalDiv.innerHTML += body;
}

function verTutorial() {
  let modalDiv = document.querySelector(".div-modal");

  let text = document.createElement("P");
  text.innerText = "Tutorial Coming Soon";
  text.classList.add("tutorial-text");

  modalDiv.appendChild(text);
}

function verDatosUser({name, email, nation, stats}) {
  let modalDiv = document.querySelector(".div-modal");
  modalDiv.classList.add("bg-" + nation);
  modalDiv.classList.add("bg-solid-" + nation);
  const side = nation == "WATER" || nation == "EARTH" ? "left" : "right";
  modalDiv.innerHTML = `
      <div class="datosUser datosUser-${side} bg-color-${nation}">
        <p class="text-userName">${email}</p>
        <div class="divText">
          <p>${name}</p>
          <p>${nation}</p>
        </div>
        <table class="table-user">
          ${tableUser(stats)}
        </table>
      </div>
    `;
}

/* UTILS */
function tableUser(stats) {
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
  table += `
      <tr>
          <td>${stats.score}</td>
          <td>${stats.won}</td>
          <td>${stats.tied}</td>
          <td>${stats.lost}</td>
          <td>${stats.win_rate}%</td>
      </tr>
      </tbody>
      `;
  return table;
}

export {createTableRanking, verDatosUser, verTutorial};
