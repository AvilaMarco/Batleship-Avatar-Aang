import {getAllHTML, getHTML} from "../utils/utils.js";

function chooseNation() {
  getHTML("#inicoNacion").classList.remove("hidden");
}

function inMenu(nation) {
  getHTML("#inicoNacion").classList.add("hidden");
  getHTML("#player").classList.add("logo-" + nation);
  getHTML("#webGames").classList.remove("hidden");
  getHTML("#botonera").classList.remove("hidden");
}

function toggleView() {
  getHTML("#back").classList.toggle("hidden");
  getHTML("#reload").classList.toggle("hidden");
  getHTML("#mapabg").classList.toggle("hidden");
  getAllHTML("button[name*='botonesMenu']")
      .forEach((e) => e.classList.toggle("hidden"));
  getHTML("#manageJoin").classList.add("hidden");
  getHTML("#infoGame").classList.add("hidden");
}

export {chooseNation, inMenu, toggleView}
/*
ToDo: delete
function viewMenu() {

  getHTML("#back").classList.add("hidden");
  getHTML("#reload").classList.add("hidden");
  getHTML("#mapabg").classList.remove("hidden");
  getAllHTML("button[name*='botonesMenu']")
      .forEach((e) => e.classList.remove("hidden"));
  getAllHTML("div [name*='dataGame']")
      .forEach((e) => e.classList.add("hidden"));
}

function viewMapa() {
  getHTML("#back").classList.remove("hidden");
  getHTML("#reload").classList.remove("hidden");
  getHTML("#mapabg").classList.add("hidden");
  getAllHTML("button[name*='botonesMenu']")
      .forEach((e) => e.classList.add("hidden"));
}*/