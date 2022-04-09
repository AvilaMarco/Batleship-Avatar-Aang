function createTableRanking(players) {
  players.forEach((e) => {
    e.total = 0;
    e.Won = 0;
    e.Lost = 0;
    e.Tied = 0;
  });

  players.forEach((player) =>
    player.scores.forEach((score) => {
      player.total += score;
      if (score == 3) player.Won++;
      else if (score == 1) player.Tied++;
      else if (score == 0) player.Lost++;
    })
  );

  players.sort((a, b) => b.total - a.total);

  let body = ``;
  body += `
      <table class="table">
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
      `;
  for (var i = 0; i < players.length; i++) {
    if (players[i].scores.length != 0) {
      body += `<tr>
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
          </tr>`;
    }
  }
  body += `</tbody></table>`;
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

function verDatosUser({ id, name, email, nation }) {
  let modalDiv = document.querySelector(".div-modal");
  modalDiv.classList.add("bg-" + nation);
  modalDiv.classList.add("bg-solid-" + nation);
  const side = nation == "WATER" || nation == "EARTH" ? "left" : "right";
  const userDataHTML = `
      <div class="datosUser datosUser-${side} bg-color-${nation}">
        <p class="text-userName">${email}</p>
        <div class="divText">
          <p>${name}</p>
          <p>${nation}</p>
        </div>
        <table class="table-user">
          ${tableUser(id)}
        </table>
      </div>
    `;

  modalDiv.innerHTML = userDataHTML;
}

/* UTILS */
function tableUser(user) {
  console.log(playerScore);
  let datos = playerScore.filter((e) => e.id == user)[0]?.scores || [];
  console.log(datos);
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
  let win = 0,
    tied = 0,
    lose = 0,
    total = 0;
  let winRate = "-";
  datos.forEach((e) => {
    total += e;
    switch (e) {
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
  });
  if (datos.length != 0) {
    winRate = parseInt((win * 100) / datos.length);
  }
  table += `
      <tr>
          <td>${total}</td>
          <td>${win}</td>
          <td>${lose}</td>
          <td>${tied}</td>
          <td>${winRate}%</td>
      </tr>
      </tbody>
      `;
  return table;
}

export { createTableRanking, verDatosUser, verTutorial };
