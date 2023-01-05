document.querySelector("#registry").addEventListener("click", registre);
document.querySelector("#login").addEventListener("click", login);
document.querySelector(".eye").addEventListener("click", toglePassword);
let contador = 2;
let interruptor = false;
let img1 = document.querySelector("#mobile");
let img2 = document.querySelector("#mobile2");
if (screen.width < 1024) {
  setInterval(carousel, 5000);
}

function carousel() {
  interruptor = !interruptor;
  if (interruptor) {
    img1.addEventListener("animationend", reset);
    img1.classList.add("carruselOut");
    img2.classList.add("carruselentry");
  } else {
    img2.addEventListener("animationend", reset);
    img2.classList.add("carruselOut");
    img1.classList.add("carruselentry");
  }
  contador++;
  contador == 5 ? (contador = 1) : null;
}

function switchAudio(event) {
  let audio = document.querySelector("#audioMain");
  if (event.alt == "true") {
    audio.pause();
    event.src = "../img/no-audio.png";
    event.alt = "false";
  } else {
    audio.play();
    event.src = "../img/audio.png";
    event.alt = "true";
  }
}

function nextStep(clase) {
  document.querySelector(".registre").classList.remove("registreaux");
  document.querySelectorAll(".menu").forEach((e) => e.classList.add("d-none"));
  document
      .querySelectorAll(".access")
      .forEach((e) => e.classList.remove("d-none"));
  document
      .querySelectorAll("." + clase)
      .forEach((e) => e.classList.remove("d-none"));
  document.querySelector("#back").classList.remove("d-none");
}

function toMenu() {
  document.querySelector(".registre").classList.add("registreaux");
  document
      .querySelectorAll(".menu")
      .forEach((e) => e.classList.remove("d-none"));
  document
      .querySelectorAll(".access")
      .forEach((e) => e.classList.add("d-none"));
  document
      .querySelectorAll(".registro")
      .forEach((e) => e.classList.add("d-none"));
  document.querySelectorAll(".login").forEach((e) => e.classList.add("d-none"));
  document.querySelector("#back").classList.add("d-none");
}

function reset() {
  if (interruptor) {
    img1.classList.remove("carruselOut");
    img1.classList.add("left");
    img1.setAttribute("src", "img/mobile" + contador + ".png");
    img2.classList.remove("carruselentry");
    img2.classList.remove("left");
  } else {
    img2.classList.remove("carruselOut");
    img2.classList.add("left");
    img2.setAttribute("src", "img/mobile" + contador + ".png");
    img1.classList.remove("carruselentry");
    img1.classList.remove("left");
  }
  contador == 5 ? (contador = 1) : null;
}

function registre() {
  let name = getHTML("#name").value;
  let email = getHTML("#email").value;
  let password = getHTML("#password").value;

  if (name === "" || email === "" || password === "") {
    alert("Faltan Completar Algunos Campos");
  }

  const signInPlayer = {name, email, password};
  const headers = {
    "Content-Type": "application/json",
  };

  fetch("/api/players", {
    method: "POST",
    body: JSON.stringify(signInPlayer),
    headers,
  })
      .then((res) => (res.ok ? res.json() : Promise.reject(res.json())))
      .then(() => login())
      .catch((error) => error)
      .then((json) => {
        if (json != undefined) return displayError(json);
      })
      .then(() => cleanForm());
}

function login() {
  let email = getHTML("#email").value;
  let password = getHTML("#password").value;
  const headers = {
    "Content-Type": "application/json",
  };
  const user = {email, password};

  fetch("/api/players/login", {
    method: "POST",
    body: JSON.stringify(user),
    headers,
  })
      .then((res) => {
            console.log(res)
            return res.ok ? res.json() : Promise.reject(res.json())
          }
      )
      .then((json) => {
        cleanForm()
        saveToken(json);
        redirect();
      })
      .catch((error) => {
        error.then(json => displayError(json))
      })
}

function toglePassword(event) {
  let text = event.target.innerText;
  if (text === "Ver") {
    document.querySelector("input[name*=password]").type = "text";
    event.target.innerText = "Ocultar";
  } else {
    document.querySelector("input[name*=password]").type = "password";
    event.target.innerText = "Ver";
  }
}

function getHTML(query) {
  return document.querySelector(query);
}

function displayError({error, message, error_fields = []}) {
  return Swal.fire({
    title: error,
    text: message,
    icon: "error",
    showDenyButton: true,
    confirmButtonText: "See more...",
    denyButtonText: `Cancel`,
  }).then(({isConfirmed}) =>
      isConfirmed ? displayErrorHTML(error, error_fields) : Promise.resolve()
  );
}

function displayErrorHTML(name, error_fields) {
  return Swal.fire({
    title: name,
    html: errorFieldsHTML(error_fields),
    confirmButtonText: "Ok",
  });
}

function errorFieldsHTML(error_fields) {
  console.log(error_fields);
  const keys = Object.keys(error_fields);
  const mapError = (e) => e.map((val) => `<p>${val}</p>`);
  const errors = keys.map(
      (title) => `
    <div>
      <h2> ${title} </h2>
      ${mapError(error_fields[title])}
    </div>
  `
  );

  return `<div> ${errors} </div>`;
}

function cleanForm() {
  const inputs = ["name", "email", "password"];
  inputs.forEach((e) => (getHTML("#" + e).value = ""));
}

function redirect() {
  //location.assign(url.split("8080")[1]);
  location.assign("/web/games.html")
}

function saveToken({token}) {
  localStorage.setItem(`user-token`, JSON.stringify(token))
}