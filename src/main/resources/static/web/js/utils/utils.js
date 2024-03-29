function getHTML(query) {
  return document.querySelector(query);
}

function getAllHTML(query) {
  return document.querySelectorAll(query);
}

function isMobile() {
  return visualViewport.width < 1024;
  // ToDo: try more test about determinated if a device be or not mobile
  // navigator.userAgentData.mobile
}

function resizeCoord(coord, side) {
  if (isMobile()) return coord;
  const currentSize = visualViewport[side] * 0.75;
  const referentSize = side === "width" ? 1400 : 800;
  const conversionFactor = currentSize / referentSize;
  return coord * conversionFactor;
}

function resizeCoordWithMargin(coords) {
  const [x, y] = coords.split(",");
  if (isMobile()) return [x, y];

  const marginSizeX = visualViewport.width * 0.125;
  const marginSizeY = visualViewport.height * 0.125;
  const coordX = parseFloat(x) + marginSizeX;
  const coordY = parseFloat(y) + marginSizeY;
  return [coordX, coordY];
}

function addClickEvent(query, action) {
  getHTML(query).addEventListener("click", action);
}

const getItemStorage = name => JSON.parse(localStorage.getItem(name));

const list = (n) => new Array(n).fill(0).map((_, i) => i + 1);

const gameSelected = () => getHTML("div[data-name*='selectGame']");

export {
  getHTML,
  getAllHTML,
  isMobile,
  resizeCoord,
  resizeCoordWithMargin,
  gameSelected,
  addClickEvent,
  list,
  getItemStorage
};
