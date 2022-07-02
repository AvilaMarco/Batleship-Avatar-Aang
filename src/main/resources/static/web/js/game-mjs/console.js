const dockConsole = document.querySelector("#console p");

const updateConsole = (text, isError = false) => {
  dockConsole.classList.remove("typing-anim");
  setTimeout(() => {
    dockConsole.classList.add("typing-anim");
    dockConsole.innerText = text;
  }, 200);
  console.trace("where");
  return isError;
};

export { updateConsole };
