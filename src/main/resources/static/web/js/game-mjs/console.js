const dockConsole = document.querySelector("#console p");

const updateConsole = (text) => {
  dockConsole.classList.remove("typing-anim");
  setTimeout(() => {
    dockConsole.classList.add("typing-anim");
    dockConsole.innerText = text;
  }, 200);
};

export { updateConsole };
