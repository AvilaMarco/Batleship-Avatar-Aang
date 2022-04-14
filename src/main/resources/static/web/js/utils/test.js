function randomNation(playerData) {
  const nations = ["FIRE", "EARTH", "AIR", "WATER"];
  const n = Math.round(Math.random() * 3);
  playerData.nation = nations[n];
}

export { randomNation };
