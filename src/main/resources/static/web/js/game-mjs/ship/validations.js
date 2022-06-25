const checkBusyCells = (ship, cell) => {
  let id = cell.id
    .match(new RegExp(`[^${cell.dataset.y}|^${cell.dataset.x}]`, "g"))
    .join("");
  let y = cell.dataset.y.charCodeAt() - 64;
  let x = parseInt(cell.dataset.x);

  document.querySelectorAll(`.${ship.id}-busy-cell`).forEach((cell) => {
    cell.classList.remove(`${ship.id}-busy-cell`);
  });

  for (let i = 0; i < ship.dataset.length; i++) {
    if (ship.dataset.orientation === "horizontal") {
      document
        .querySelector(`#${id}${String.fromCharCode(y + 64)}${x + i}`)
        .classList.add(`${ship.id}-busy-cell`);
    } else {
      document
        .querySelector(`#${id}${String.fromCharCode(y + 64 + i)}${x}`)
        .classList.add(`${ship.id}-busy-cell`);
    }
  }
};

export { checkBusyCells };
